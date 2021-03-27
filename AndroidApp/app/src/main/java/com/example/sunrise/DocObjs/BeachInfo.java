package com.example.sunrise.DocObjs;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;


public class BeachInfo {

    private GeoPoint location;
    private String date;
    private String hour;
    private int temperature;
    private float precipitation;
    private String wind;
    private float waveSize;
    private String swell;
    private int waterTemperature;
    private String sunrise;
    private String sunset;
    private List<String> highTides;
    private List<String> lowTides;

    public BeachInfo(){}

    public BeachInfo(GeoPoint location, String date, String hour, int temperature, float precipitation,
                     String wind, float waveSize, String swell, int waterTemperature, String sunrise,
                     String sunset, List<String> highTides, List<String> lowTides){
        this.location = location;
        this.date = date;
        this.hour = hour;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.wind = wind;
        this.waveSize = waveSize;
        this.swell = swell;
        this.waterTemperature = waterTemperature;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.highTides = highTides;
        this.lowTides = lowTides;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    public int getTemperature() {
        return temperature;
    }

    public float getPrecipitation() {
        return precipitation;
    }

    public String getWind() {
        return wind;
    }

    public float getWaveSize() {
        return waveSize;
    }

    public String getSwell() {
        return swell;
    }

    public int getWaterTemperature() {
        return waterTemperature;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public List<String> getHighTides() {
        return highTides;
    }

    public List<String> getLowTides() {
        return lowTides;
    }
}
