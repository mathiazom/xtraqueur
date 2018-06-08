package com.mzom.xtraqueur;

import java.text.NumberFormat;
import java.util.Locale;

class CurrencyFormatter {

    static String formatValue(double value){

        // Get currency format
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return nf.format(value);

    }

}
