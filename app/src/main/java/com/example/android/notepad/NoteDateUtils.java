package com.example.android.notepad;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Formats all user-visible date and time values in China Standard Time.
 */
final class NoteDateUtils {

    private static final TimeZone SHANGHAI = TimeZone.getTimeZone("Asia/Shanghai");

    private NoteDateUtils() {
    }

    static SimpleDateFormat formatter(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                pattern,
                Locale.getDefault());
        formatter.setTimeZone(SHANGHAI);
        return formatter;
    }

    static Calendar calendar() {
        return Calendar.getInstance(SHANGHAI, Locale.getDefault());
    }
}
