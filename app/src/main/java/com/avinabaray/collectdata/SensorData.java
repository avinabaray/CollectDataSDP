package com.avinabaray.collectdata;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SensorData implements Serializable {
    private Long timestamp;
    private final String formattedTime;
    private Float x;
    private Float y;
    private Float z;

    public SensorData(Long timestamp, Float x, Float y, Float z) {
        this.timestamp = timestamp;
        formattedTime = Commons.formatTime(timestamp);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getZ() {
        return z;
    }

    public void setZ(Float z) {
        this.z = z;
    }

    public String getFormattedTime() {
        return formattedTime;
    }
}
