package com.monstrous.shadowtest.gui;

public class FormatUtils {

    public static String formatFloat( float f ){
        int frac = (int)(10*(f - (int)f));   // one decimal position
        return String.valueOf((int)f)+"."+String.valueOf(frac);
    }
}
