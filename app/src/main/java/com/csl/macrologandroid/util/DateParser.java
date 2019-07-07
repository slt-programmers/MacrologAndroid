package com.csl.macrologandroid.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateParser {

    private static final SimpleDateFormat standardFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("nl"));
    private static final SimpleDateFormat shortFormat = new SimpleDateFormat("yyyy-M-d", Locale.forLanguageTag("nl"));
    private static final SimpleDateFormat reversedFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("nl"));
    private static final SimpleDateFormat reversedShortFormat = new SimpleDateFormat("d-M-yyyy", Locale.forLanguageTag("nl"));

    public static String format(Date date) {
        return standardFormat.format(date);
    }

    public static Date parse(String string) {
        Date date = null;
        try {
            date = standardFormat.parse(string);
        } catch (ParseException ex) {
            try {
                date = shortFormat.parse(string);
            } catch (ParseException ex2) {
                try {
                    date = reversedFormat.parse(string);
                } catch (ParseException ex3) {
                    try {
                        date = reversedShortFormat.parse(string);
                    } catch (ParseException ex4) {
                        Log.e(DateParser.class.toString(), "Could not parse to Date");
                    }
                }
            }
        }

        return date;
    }
}
