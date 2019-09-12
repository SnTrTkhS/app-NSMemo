package com.example.simplememo;

import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;

public class SettingValues extends AppCompatActivity {
    private static String[] _textSizes;
    public static final int DEFAULT_TEXTSIZE_MAIN_TITLE = 7;
    public static final int DEFAULT_TEXTSIZE_MAIN_BODY = 5;
    public static final int DEFAULT_TEXTSIZE_MAIN_DATETIME = 5;
    public static final int DEFAULT_TEXTSIZE_CREATE_TITLE = 10;
    public static final int DEFAULT_TEXTSIZE_CREATE_BODY = 7;
    public static final int DEFAULT_TEXTSIZE_CREATE_DATETIME = 5;
    public static final String DEFAULT_FONTFAMILY_NAME = "meiryob.ttc";
    public static Typeface _typefaceBold;
    public static Typeface _typefaceIcon;
    private static String[] _maxLines;
    public static final int DEFAULT_MAXLINES_MAIN_BODY = 3;
    private static String[] _undoMaxs;
    public static final int DEFAULT_UNDOMAX = 4;


    public SettingValues() {
        //super();
    }

    public SettingValues(String[] textSizes, String[] maxLines, String[] undoMaxs, Typeface typefaceBold, Typeface typefaceIcon) {
        //super();
        _textSizes = textSizes;
        _maxLines = maxLines;
        _undoMaxs = undoMaxs;
        _typefaceBold = typefaceBold;
        _typefaceIcon = typefaceIcon;
    }


    public float[] getTextSizes() {
        return getStringsToFloats(_textSizes);
    }
//    public int[] getClockSizes() {
//        return getStringsToInts(_textSizes);
//    }
    public int[] getMaxLines() {
        return getStringsToInts(_maxLines);
    }
    public int[] getUndoMaxs() {
        return getStringsToInts(_undoMaxs);
    }
    public Typeface getTypefaceBold() {
        return _typefaceBold;
    }
    public Typeface getTypefaceIcon() {
        return _typefaceIcon;
    }



    public int[] getStringsToInts( String[] strings ) {
        int[] ints = new int[strings.length];
        for (int i = 0 ; i < strings.length ; i++) {
            strings[i] = strings[i].replace(".5","");
            ints[i] = Integer.parseInt(strings[i]);
        }
        return ints ;
    }

    public float[] getStringsToFloats( String[] strings ) {
        float[] floats = new float[strings.length];
        for (int i = 0 ; i < strings.length ; i++) {
            floats[i] = Float.parseFloat(strings[i]);
        }
        return floats ;
    }


}
