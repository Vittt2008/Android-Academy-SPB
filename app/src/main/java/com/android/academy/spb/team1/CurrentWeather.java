package com.android.academy.spb.team1;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CurrentWeather {
    private String locationLabel;
    private String icon;
    private long time;
    private double temperature;
    private double humidity;
    private double precipChance;
    private String summary;
    private String timezone;

    public CurrentWeather() {
    }

    public CurrentWeather(String locationLabel, String icon, Long time, Double temperature, Double humidity, Double precipChance, String summary, String timezone) {
        this.locationLabel = locationLabel;
        this.icon = icon;
        this.time = time;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipChance = precipChance;
        this.summary = summary;
        this.timezone = timezone;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public void setLocationLabel(String locationLabel) {
        this.locationLabel = locationLabel;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getIconId() {
        try {
            return R.drawable.class
                    .getField(icon.replaceAll("-", "_"))
                    .getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public long getTime() {
        return time;
    }

    public String getFormattedTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone(timezone));
        return formatter.format(new Date(time * 1000));
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPrecipChance() {
        return precipChance;
    }

    public void setPrecipChance(double precipChance) {
        this.precipChance = precipChance;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return "CurrentWeather{" +
                "locationLabel='" + locationLabel + '\'' +
                ", icon='" + icon + '\'' +
                ", time=" + time +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", precipChance=" + precipChance +
                ", summary='" + summary + '\'' +
                ", timezone='" + timezone + '\'' +
                '}';
    }
}
