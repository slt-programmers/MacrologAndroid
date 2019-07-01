package com.csl.macrologandroid.util;

import android.util.Log;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateParser {

//    private static DateTimeFormatter standardFormat = DateTimeFormatter.ISO_LOCAL_DATE;
//    private static DateTimeFormatter shortFormat = DateTimeFormatter.ofPattern("yyyy-M-d");
//    private static DateTimeFormatter reversedFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//    private static DateTimeFormatter reversedShortFormat = DateTimeFormatter.ofPattern("d-M-yyyy");
//
//    public static LocalDate parse(String stringDate) {
//        LocalDate date = null;
//        try {
//            date = LocalDate.parse(stringDate, standardFormat);
//        } catch (DateTimeParseException ex) {
//            try {
//                date = LocalDate.parse(stringDate, shortFormat);
//            } catch (DateTimeParseException ex2) {
//                try {
//                    date = LocalDate.parse(stringDate, reversedFormat);
//                } catch (DateTimeParseException ex3) {
//                    try {
//                        date = LocalDate.parse(stringDate, reversedShortFormat);
//                    } catch (DateTimeParseException ex4) {
//                        Log.e(LocalDateParser.class.toString(), "Could not parse to LocalDate");
//                    }
//                }
//            }
//        }
//
//        return date;
//    }

}
