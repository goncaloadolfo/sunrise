package com.example.sunrise.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sunrise.DocObjs.BeachInfo;
import com.example.sunrise.R;
import com.google.firebase.firestore.GeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * task to get information from world weather online
 */
public class WWOCom extends AsyncTask<Void, Void, Void> {

    private final static String WWO_URL = "http://api.worldweatheronline.com/premium/v1/marine.ashx?"
            + "format=xml&key=825887e35a4a446d93271322192105&tide=yes";
    public final static int SUCCESS_STATUS_CODE = 200;
    private GeoPoint beachCoords;
    private int statusCode = -1;
    private AppCompatActivity activity;
    private ArrayList<String> datesNeeded;

    public WWOCom(GeoPoint beachCoords, AppCompatActivity activity, ArrayList<String> datesNeeded) {
        super();
        this.beachCoords = beachCoords;
        this.activity = activity;
        this.datesNeeded = datesNeeded;
    }

    protected Void doInBackground(Void... params) {
        try {
            // connection
            URL urlObj = new URL(WWO_URL + "&q=" + beachCoords.getLatitude() + "," + beachCoords.getLongitude());
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozzila/5.0");

            // get response
            statusCode = con.getResponseCode();

            // 200 status code
            if (statusCode == SUCCESS_STATUS_CODE) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer response = new StringBuffer();

                // read response
                String line;
                while ((line = br.readLine()) != null)
                    response.append(line);
                br.close();

                // write necessary beach info to cloud firestore
                BeachInfo[] info = new XMLParser(beachCoords, response.toString()).getInfoFromXML();
                ArrayList<BeachInfo> infoNeeded = new ArrayList<>();

                if (info != null) {

                    for (BeachInfo beachInfo : info){
                        if (datesNeeded.contains(beachInfo.getDate()))
                            infoNeeded.add(beachInfo);
                    }

                    FirebaseCom.addBeachInfo(infoNeeded.toArray(new BeachInfo[infoNeeded.size()]), activity);
                }
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (statusCode != SUCCESS_STATUS_CODE) {
            Context context = activity.getApplicationContext();
            Toast.makeText(context, context.getString(R.string.wwoProbs),
                    Toast.LENGTH_LONG).show();

            // try again in 3 seconds
            try{
                Thread.sleep(3000);
                new WWOCom(beachCoords, activity, datesNeeded).execute();
            }

            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
