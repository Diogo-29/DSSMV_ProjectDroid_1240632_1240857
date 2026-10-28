package com.example.projectdroid.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// As datas são guardadas no RestDB como texto no formato "2026-10-28"
public class DateUtils {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String today() {
        return FORMAT.format(Calendar.getInstance().getTime());
    }

    // Soma dias a uma data, por exemplo addDays("2026-10-28", 14) = "2026-11-11"
    public static String addDays(String date, int days) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(FORMAT.parse(date));
        } catch (ParseException e) {
            // Se a data não estiver no formato certo, conta a partir de hoje
        }
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return FORMAT.format(calendar.getTime());
    }

    // Como as datas estão no formato ano-mês-dia, comparar o texto é o mesmo que comparar as datas
    public static boolean isBeforeToday(String date) {
        return date != null && date.compareTo(today()) < 0;
    }
}
