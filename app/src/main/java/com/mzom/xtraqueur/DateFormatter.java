package com.mzom.xtraqueur;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class DateFormatter {

    private static String format(Long date, String format, Locale locale){

        // Date string format
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);

        // Format to string
        return dateFormat.format(new Date(date));
    }

    static String format(Long date,String format){

        return format(date,format,Locale.getDefault());

    }

    static String formatDate(Long date){

        // Date string format
        final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

        // Format to string
        return dateFormat.format(new Date(date));

    }

    static String formatTime(Long date){

        // Date string format
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Format to string
        return dateFormat.format(new Date(date));

    }

    static String formatDateAndTime(Long date){

        return formatDate(date) + " " + formatTime(date);

    }


}
