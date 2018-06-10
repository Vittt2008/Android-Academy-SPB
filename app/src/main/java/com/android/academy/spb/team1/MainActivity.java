package com.android.academy.spb.team1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.academy.spb.team1.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String API_KEY = "111b4406c7757474f9e4ba2bee689f93";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final double LATITUDE = 59.9059;
    private static final double LONGITUDE = 30.5130;

    public static final String LOCALE = Locale.getDefault().getLanguage();

    private final OkHttpClient client = new OkHttpClient();

    private CurrentWeather currentWeather;

    private Location lastKnownLocation;

    private ImageView iconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iconImageView = findViewById(R.id.iconImageView);
        getForecast();
    }

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getLocality()).append("\n");
                result.append(address.getCountryName());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    private void getForecast() {
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        if (!isNetworkAvailable()) {
            alertUserAboutError();
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkLocationPermission()) {
            requestLocation();
        }

        if (locationManager.getBestProvider(new Criteria(), true) != null) {
            lastKnownLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
        }

        String forecastURL = String.format(
                Locale.US,
                "https://api.darksky.net/forecast/%s/%f,%f?lang=%s&units=si",
                API_KEY, LATITUDE, LONGITUDE, LOCALE
        );

        Request request = new Request.Builder()
                .url(forecastURL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                alertUserAboutError();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    if (responseBody == null) {
                        throw new IOException("Empty response body");
                    }

                    String cityName = getString(R.string.city_default);

                    if (lastKnownLocation != null) {
                        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                        List<Address> addresses;
                        try {
                            addresses = gcd.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
                            if (addresses.size() > 0) {
                                System.out.println(addresses.get(0).getLocality());
                                cityName = addresses.get(0).getLocality();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    currentWeather = getCurrentDetails(responseBody.string());
                    currentWeather.setLocationLabel(cityName);
                    Log.v(TAG, currentWeather.toString());
                    binding.setWeather(new CurrentWeather(
                            currentWeather.getLocationLabel(),
                            currentWeather.getIcon(),
                            currentWeather.getTime(),
                            currentWeather.getTemperature(),
                            currentWeather.getHumidity(),
                            currentWeather.getPrecipChance(),
                            currentWeather.getSummary(),
                            currentWeather.getTimezone()
                    ));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Integer iconId = currentWeather.getIconId();
                            if (iconId != null && iconImageView != null) {
                                iconImageView.setImageDrawable(getResources().getDrawable(iconId));
                            }
                        }
                    });


                } catch (IOException e) {
                    alertUserAboutError();
                }
            }
        });
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocation() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    private CurrentWeather getCurrentDetails(String jsonAsString) throws IOException {
        try {
            JSONObject jsonObject = new JSONObject(jsonAsString);
            CurrentWeather currentWeather = new CurrentWeather();

            currentWeather.setHumidity(jsonObject.getJSONObject("currently").getDouble("humidity"));
            currentWeather.setIcon(jsonObject.getJSONObject("currently").getString("icon"));
            currentWeather.setLocationLabel(getAddress(LATITUDE, LONGITUDE));
            currentWeather.setPrecipChance(jsonObject.getJSONObject("currently").getDouble("precipProbability"));
            currentWeather.setSummary(jsonObject.getJSONObject("currently").getString("summary"));
            currentWeather.setTime(jsonObject.getJSONObject("currently").getLong("time"));
            currentWeather.setTemperature(jsonObject.getJSONObject("currently").getDouble("temperature"));
            currentWeather.setTimezone(jsonObject.getString("timezone"));

            Log.d(TAG, currentWeather.getFormattedTime());
            Integer iconId = currentWeather.getIconId();
            if (iconId == null) {
                Log.d(TAG, "null");
            } else {
                Log.d(TAG, iconId.toString());
            }

            return currentWeather;
        } catch (JSONException e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }

    private void alertUserAboutError() {
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.show(getFragmentManager(), "error_dialog");
    }

    public void refreshOnClick(View view) {
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_LONG).show();
        getForecast();
    }
}