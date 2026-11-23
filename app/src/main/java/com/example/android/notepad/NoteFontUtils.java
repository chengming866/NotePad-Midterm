package com.example.android.notepad;

import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Maps the persisted font index to one of Android's reliable system families.
 */
final class NoteFontUtils {

    private static final Typeface[] TYPEFACES = new Typeface[] {
            Typeface.DEFAULT,
            Typeface.SERIF,
            Typeface.SANS_SERIF,
            Typeface.MONOSPACE
    };

    private NoteFontUtils() {
    }

    static int normalize(int fontStyle) {
        return fontStyle >= 0 && fontStyle < TYPEFACES.length ? fontStyle : 0;
    }

    static void apply(TextView view, int fontStyle) {
        view.setTypeface(TYPEFACES[normalize(fontStyle)]);
    }

    static void applyBold(TextView view, int fontStyle) {
        view.setTypeface(TYPEFACES[normalize(fontStyle)], Typeface.BOLD);
    }
}
