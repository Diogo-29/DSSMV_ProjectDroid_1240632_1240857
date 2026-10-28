package com.example.projectdroid.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// As datas são guardadas no RestDB como texto no formato "2026-10-28"
public class DateUtils {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String today() {
        return FORMAT.format(Calendar.getInstance().getTime());
    }

    public static String daysFromToday(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return FORMAT.format(calendar.getTime());
    }
}
