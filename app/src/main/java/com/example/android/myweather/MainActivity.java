package com.example.android.myweather;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner mySpinner = (Spinner) findViewById(R.id.spinner1);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.cities));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);
        mySpinner.setOnItemSelectedListener(this);


    }

    private class WeatherInfo extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);

                int data = reader.read();
                String apiDetails = "";
                char current;

                while (data != -1) {
                    current = (char) data;
                    apiDetails += current;
                    data = reader.read();
                }

                return apiDetails;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        TextView tempTextView = (TextView) findViewById(R.id.temperature);
        TextView descriptionTextView = (TextView) findViewById(R.id.description);
        TextView tempHighTextView = (TextView) findViewById(R.id.high);
        TextView tempLowTextView = (TextView) findViewById(R.id.low);
        TextView windTextView = (TextView) findViewById(R.id.wind);

        WeatherInfo weatherInfo = new WeatherInfo();
        String city = parent.getItemAtPosition(position).toString();

        Float kelvin = 273.15f;
        Float kmh = 3.6f;
        Float tempMaxFloat = 0.0f;
        String tempMaxFloat2 = "";
        Float windFloat = 0.0f;
        String windFloat2 = "";
        Float tempMinFloat = 0.0f;
        String tempMinFloat2 = "";
        Float tempFloat = 0.0f;
        String tempFloat2 = "";
        JSONObject tempDetails = null;
        String weatherApiDetails = "";
        JSONObject jsonObject = null;
        String weather = "";
        JSONArray array = null;
        JSONObject arrayObject = null;
        String icon = "";
        String description = "";
        String temperature = "";
        String temp_max = "";
        String temp_min = "";
        JSONObject windDetails = null;
        String wind = "";
        String iconDetails = "";
        String url = "";

        try {
            weatherApiDetails = weatherInfo.execute(
                    "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=1be934535714fb54f628cd20a7b01a3c").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObject = new JSONObject(weatherApiDetails);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            weather = jsonObject.getString("weather");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            tempDetails = jsonObject.getJSONObject("main");
            temperature = tempDetails.getString("temp");
            temp_max = tempDetails.getString("temp_max");
            temp_min = tempDetails.getString("temp_min");

            tempMaxFloat = Float.parseFloat(temp_max);
            tempMaxFloat = tempMaxFloat - kelvin;
            tempMaxFloat2 = new DecimalFormat("##").format(tempMaxFloat);

            tempMinFloat = Float.parseFloat(temp_min);
            tempMinFloat = tempMinFloat - kelvin;
            tempMinFloat2 = new DecimalFormat("##").format(tempMinFloat);

            tempFloat = Float.parseFloat(temperature);
            tempFloat = tempFloat - kelvin;
            tempFloat2 = new DecimalFormat("##").format(tempFloat);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            windDetails = jsonObject.getJSONObject("wind");
            wind = windDetails.getString("speed");
            windFloat = Float.parseFloat(wind);
            windFloat = windFloat * kmh;
            windFloat2 = new DecimalFormat("##").format(windFloat);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Get all weather details
        try {
            array = new JSONArray(weather);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                arrayObject = array.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                icon = arrayObject.getString("icon");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                description = arrayObject.getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.i("Icon: ", icon);
        url = "http://openweathermap.org/img/w/" + icon + ".png";
        ImageView myFirstImage = (ImageView) findViewById(R.id.image);
        myFirstImage.setTag(url);
        new DownloadImagesTask().execute(myFirstImage);

        final String DEGREE = "\u00b0";
        tempFloat2 = tempFloat2 + DEGREE + "C";
        tempMaxFloat2 = "High: " + tempMaxFloat2 + DEGREE + "C";
        tempMinFloat2 = "Low: " + tempMinFloat2 + DEGREE + "C";
        windFloat2 = "Wind: " + windFloat2 + "km/h";

        tempTextView.setText(tempFloat2);
        descriptionTextView.setText(description);
        tempHighTextView.setText(tempMaxFloat2);
        tempLowTextView.setText(tempMinFloat2);
        windTextView.setText(windFloat2);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

        ImageView imageView = null;

        @Override
        protected Bitmap doInBackground(ImageView... imageViews) {
            this.imageView = imageViews[0];
            return download_Image((String)imageView.getTag());
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }

        private Bitmap download_Image(String url) {

            Bitmap bmp =null;
            try{
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp)
                    return bmp;

            }catch(Exception e){

            }
            return bmp;
        }
    }
}

