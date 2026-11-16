package com.example.android.notepad;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;

/**
 * Keeps the stored color index and the rendered note background in sync.
 */
final class NoteColorUtils {

    private static final int[] COLORS = new int[] {
            0xFFFFFFFF,
            0xFFFFF4CC,
            0xFFE7F0FF,
            0xFFE8F6ED,
            0xFFFDE9EF,
            0xFFEFEAFE
    };

    private NoteColorUtils() {
    }

    static int count() {
        return COLORS.length;
    }

    static int normalize(int colorIndex) {
        return colorIndex >= 0 && colorIndex < COLORS.length ? colorIndex : 0;
    }

    static int color(int colorIndex) {
        return COLORS[normalize(colorIndex)];
    }

    static void applyCardBackground(View view, Context context, int colorIndex) {
        setBackground(view, createRoundedBackground(
                context,
                color(colorIndex),
                1f,
                0xFFE2E7ED));
    }

    static void applySwatchBackground(
            View view,
            Context context,
            int colorIndex,
            boolean selected) {
        setBackground(view, createRoundedBackground(
                context,
                color(colorIndex),
                selected ? 3f : 1f,
                selected ? 0xFF2F6FED : 0xFFBFC8D4));
        view.setSelected(selected);
    }

    private static GradientDrawable createRoundedBackground(
            Context context,
            int fillColor,
            float strokeWidthDp,
            int strokeColor) {
        float density = context.getResources().getDisplayMetrics().density;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fillColor);
        drawable.setCornerRadius(8f * density);
        drawable.setStroke(Math.max(1, Math.round(strokeWidthDp * density)), strokeColor);
        return drawable;
    }

    @SuppressWarnings("deprecation")
    private static void setBackground(View view, GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }
}
