package com.example.sunrise.Utils;


import com.example.sunrise.DocObjs.BeachInfo;
import com.google.firebase.firestore.GeoPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLParser {

    public static final String[] VALID_TIMES = new String[]{"600", "900", "1200", "1500", "1800", "2100"};

    // xml dom doc
    private Document xmlDoc;

    // common information between days
    private GeoPoint location;
    private String date;
    private String sunrise;
    private String sunset;


    public XMLParser(GeoPoint location, String xmlString){
        this.location = location;

        // convert string to dom doc
        DocumentBuilderFactory parser = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder documentBuilder = parser.newDocumentBuilder();
            xmlDoc = documentBuilder.parse(new InputSource(new StringReader(xmlString)));
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }

    public BeachInfo[] getInfoFromXML(){

        if (xmlDoc != null){

            // return structure
            BeachInfo[] beachInfoObjs = new BeachInfo[VALID_TIMES.length * 7];
            int beachIndex = 0;

            // each day info elem
            NodeList weatherElems = xmlDoc.getElementsByTagName("weather");

            for (int n = 0; n < weatherElems.getLength(); n++) {
                Element dayElem = (Element) weatherElems.item(n);
                List<String> lowTides = new ArrayList<>();
                List<String> highTides = new ArrayList<>();
                setCommonInfo(dayElem, lowTides, highTides);

                NodeList hourlyElems = dayElem.getElementsByTagName("hourly");

                for (int i = 0; i < hourlyElems.getLength(); i++) {
                    Element hourlyElem = (Element) hourlyElems.item(i);
                    String hour = hourlyElem.getElementsByTagName("time").item(0).getTextContent();

                    // valid hour
                    if (isPossibleHour(hour)) {

                        // temperature
                        int temperature = Integer.parseInt(hourlyElem.getElementsByTagName("tempC")
                                .item(0).getTextContent());

                        // precipitation
                        float precipitation = Float.parseFloat(hourlyElem.getElementsByTagName("precipMM")
                                .item(0).getTextContent());

                        // wave size
                        float waveSize = Float.parseFloat(hourlyElem.getElementsByTagName("sigHeight_m")
                            .item(0).getTextContent());

                        // water temperature
                        int waterTemp = Integer.parseInt(hourlyElem.getElementsByTagName("waterTemp_C")
                            .item(0).getTextContent());

                        // wind
                        String windVel = hourlyElem.getElementsByTagName("windspeedKmph")
                                .item(0).getTextContent();
                        String windDir = hourlyElem.getElementsByTagName("winddir16Point")
                                .item(0).getTextContent();
                        String wind = windVel + "km/h " + windDir;

                        // swell
                        String swellPeriod = hourlyElem.getElementsByTagName("swellPeriod_secs")
                                .item(0).getTextContent();
                        String swellDir = hourlyElem.getElementsByTagName("swellDir16Point")
                                .item(0).getTextContent();
                        String swell = swellPeriod + "s " + swellDir;

                        // create and add beachinfo obj
                        BeachInfo beachInfoObj = new BeachInfo(location, date, hour, temperature, precipitation,
                                wind, waveSize, swell, waterTemp, sunrise, sunset, highTides, lowTides);
                        beachInfoObjs[beachIndex] = beachInfoObj;
                        beachIndex++;
                    }
                }
            }

            return beachInfoObjs;
        }

        return null;
    }

    private void setCommonInfo(Element dayElem, List<String> lowTides, List<String> highTides){
        // date and sunrise and sunset
        date = dayElem.getElementsByTagName("date").item(0).getTextContent();
        sunrise = dayElem.getElementsByTagName("sunrise").item(0).getTextContent();
        sunset = dayElem.getElementsByTagName("sunset").item(0).getTextContent();

        // tides
        NodeList tideElems = dayElem.getElementsByTagName("tide_data");

        for (int i = 0; i < tideElems.getLength(); i++){
            Element tideElem = (Element) tideElems.item(i);
            String tideHour = tideElem.getElementsByTagName("tideTime").item(0).getTextContent();
            String tideType = tideElem.getElementsByTagName("tide_type").item(0).getTextContent();

            if (lowTides.size() == 2 && highTides.size() == 2)
                break;

            // insert tide hour to the correct data structure
            if (tideType.equals("LOW"))
                lowTides.add(tideHour);

            else
                highTides.add(tideHour);
        }
    }

    private boolean isPossibleHour(String hour){
        for (String validHour : VALID_TIMES){
            if (validHour.equals(hour))
                return true;
        }

        return false;
    }
}
