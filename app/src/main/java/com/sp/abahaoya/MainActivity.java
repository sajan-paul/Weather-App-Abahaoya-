package com.sp.abahaoya;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

    public class MainActivity extends AppCompatActivity {

        private EditText cityName;
        private TextView weatherResult;
        private final String API_KEY = "e93f482cad90eb1657b4595e6e25c588";
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            cityName = findViewById(R.id.cityName);
            weatherResult = findViewById(R.id.weatherResult);
            Button btnGetWeather = findViewById(R.id.getWeather);

            btnGetWeather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String city = cityName.getText().toString();
                    if (!city.isEmpty()) {
                        new FetchWeatherTask().execute(city);
                    } else {
                        weatherResult.setText("Please enter a city name.");
                    }
                }
            });
        }

        private class FetchWeatherTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String cityName = params[0];
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + "&units=metric";

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    return result.toString();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);

                        // Check if the response contains a "cod" key for error handling
                        if (jsonObject.has("cod")) {
                            String code = jsonObject.getString("cod");

                            if (code.equals("404")) {
                                // If city not found, show Toast message
                                Toast.makeText(MainActivity.this, "City not found. Please try again.", Toast.LENGTH_LONG).show();
                                weatherResult.setText(""); // Clear any previous data
                                return;
                            }
                        }

                        // Extract weather details if city is valid
                        JSONObject main = jsonObject.getJSONObject("main");
                        String temperature = main.getString("temp");
                        String humidity = main.getString("humidity");
                        String pressure = main.getString("pressure");

                        String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

                        JSONObject wind = jsonObject.getJSONObject("wind");
                        String windSpeed = wind.getString("speed");
                        String windDirection = wind.has("deg") ? wind.getString("deg") + "°" : "N/A";

                        JSONObject sys = jsonObject.getJSONObject("sys");
                        String country = sys.getString("country");
                        String cityName = jsonObject.getString("name");

                        weatherResult.setText(
                                "City: " + cityName + ", " + country + "\n" +
                                        "Temperature: " + temperature + "°C\n" +
                                        "Description: " + description + "\n" +
                                        "Humidity: " + humidity + "%\n" +
                                        "Pressure: " + pressure + " hPa\n" +
                                        "Wind Speed: " + windSpeed + " m/s\n" +
                                        "Wind Direction: " + windDirection
                        );

                    } catch (Exception e) {
                        weatherResult.setText("Error cannot find data");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error cannot find data", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

