package com.avinabaray.collectdata;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Commons {
    public static String formatTime(Long timestampInMillis) {
        Date date = new Date(timestampInMillis);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS a");
//        formatter.setTimeZone(TimeZone.getTimeZone("IST"));

        return formatter.format(date);
    }
}
