package com.mzom.xtraqueur;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class DateFormatter {

    static String formatDate(Date date){

        // Date string format
        final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

        // Format to string
        return dateFormat.format(date);

    }

    static String formatDate(Long date){

        // Date string format
        final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

        // Format to string
        return dateFormat.format(new Date(date));

    }


}
