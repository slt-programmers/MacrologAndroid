package com.csl.macrologandroid.util;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateParserTest {

    @Test
    public void format() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 5, 14);
        Date date = calendar.getTime();

        assertEquals(DateParser.format(date), "2018-05-14");
    }

    @Test
    public void parse() {
        String date1 = "2018-05-14";
        String date2 = "2018-5-14";
        String date3 = "2019-07-1";
        String date4 = "2019-7-01";
        String date5 = "2019-7-1";
        String date6 = "14-05-2018";
        String date7 = "14-5-2018";
        String date8 = "01-7-2019";
        String date9 = "1-07-2019";
        String date10 = "1-7-2019";

        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 4, 14, 0,0,0);
        Date resultDate = calendar.getTime();

        calendar.set(2019, 6, 1,0,0,0);
        Date resultDate2 = calendar.getTime();

        assertEquals(DateParser.format(DateParser.parse(date1)), DateParser.format(resultDate));
        assertEquals(DateParser.format(DateParser.parse(date2)), DateParser.format(resultDate));
        assertEquals(DateParser.format(DateParser.parse(date3)), DateParser.format(resultDate2));
        assertEquals(DateParser.format(DateParser.parse(date4)), DateParser.format(resultDate2));
        assertEquals(DateParser.format(DateParser.parse(date5)), DateParser.format(resultDate2));
        assertEquals(DateParser.format(DateParser.parse(date6)), DateParser.format(resultDate));
        assertEquals(DateParser.format(DateParser.parse(date7)), DateParser.format(resultDate));
        assertEquals(DateParser.format(DateParser.parse(date8)), DateParser.format(resultDate2));
        assertEquals(DateParser.format(DateParser.parse(date9)), DateParser.format(resultDate2));
        assertEquals(DateParser.format(DateParser.parse(date10)), DateParser.format(resultDate2));
    }
}