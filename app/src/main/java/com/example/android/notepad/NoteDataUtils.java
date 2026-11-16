/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.example.android.notepad;

import android.text.TextUtils;

/**
 * Small helpers shared by the note list and editor.
 */
final class NoteDataUtils {

    private NoteDataUtils() {
    }

    static String normalizeCategory(String category) {
        return TextUtils.isEmpty(category) ? NotePad.Notes.DEFAULT_CATEGORY : category;
    }

    /**
     * Escapes SQL LIKE wildcards so search text is always matched literally.
     */
    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
