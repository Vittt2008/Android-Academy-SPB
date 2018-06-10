package com.android.academy.spb.team1;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.academy.spb.team1.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String API_KEY = "111b4406c7757474f9e4ba2bee689f93";
    public static final double LATITUDE = 59.9059;
    public static final double LONGITUDE = 30.5130;

    private final OkHttpClient client = new OkHttpClient();

    private CurrentWeather currentWeather;

    private ImageView iconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForecast();
    }

//     private final LocationListener locationListener = new LocationListener() {
//        public void onLocationChanged(Location location) {
//            LATITUDE = location.getLongitude();
//            LONGITUDE = location.getLatitude();
//        }
//    }

    private void getForecast() {
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this,
                R.layout.activity_main);

        if (!isNetworkAvailable()) {
            alertUserAboutError();
            return;
        }

        TextView darkSkyLabel = findViewById(R.id.darkSkyAttribution);
        darkSkyLabel.setMovementMethod(LinkMovementMethod.getInstance());

        String forecastURL = String.format(
                Locale.US,
                "https://api.darksky.net/forecast/%s/%f,%f",
                API_KEY, LATITUDE, LONGITUDE
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

                    currentWeather = getCurrentDetails(responseBody.string());
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

                    iconImageView = findViewById(R.id.iconImageView);
                    Integer iconId = currentWeather.getIconId();
                    if (iconId!=null)
                    iconImageView.setImageDrawable(getResources().getDrawable(iconId));

                } catch (IOException e) {
                    alertUserAboutError();
                }
            }
        });
    }

    private CurrentWeather getCurrentDetails(String jsonAsString) throws IOException {
        try {
            JSONObject jsonObject = new JSONObject(jsonAsString);
            CurrentWeather currentWeather = new CurrentWeather();

            currentWeather.setHumidity(jsonObject.getJSONObject("currently").getDouble("humidity"));
            currentWeather.setIcon(jsonObject.getJSONObject("currently").getString("icon"));
            currentWeather.setLocationLabel("CouldRaw Country");
            currentWeather.setPrecipChance(jsonObject.getJSONObject("currently").getDouble("precipProbability"));
            currentWeather.setSummary(jsonObject.getJSONObject("currently").getString("summary"));
            currentWeather.setTime(jsonObject.getJSONObject("currently").getLong("time"));
            currentWeather.setTemperature(jsonObject.getJSONObject("currently").getDouble("temperature"));
            currentWeather.setTimezone(jsonObject.getString("timezone"));

            Log.d(TAG, currentWeather.getFormattedTime());
            Integer iconId = currentWeather.getIconId();
            if (iconId==null){
                Log.d(TAG, "null");
            }else {
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