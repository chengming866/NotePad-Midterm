/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.example.android.notepad;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Shows notes marked as todos. Pending items stay above completed items.
 */
public class TodoListActivity extends ListActivity {

    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
            NotePad.Notes.COLUMN_NAME_TODO_DONE,
            NotePad.Notes.COLUMN_NAME_BACKGROUND_COLOR,
            NotePad.Notes.COLUMN_NAME_TODO_PRIORITY,
            NotePad.Notes.COLUMN_NAME_TODO_DUE_DATE,
            NotePad.Notes.COLUMN_NAME_FONT_STYLE
    };

    private TodoAdapter mAdapter;
    private TextView mEmptyView;
    private TextView mSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_list);

        mEmptyView = (TextView) findViewById(android.R.id.empty);
        mSummary = (TextView) findViewById(R.id.todo_summary);
        getListView().setOnCreateContextMenuListener(this);

        mAdapter = new TodoAdapter(queryTodos());
        setListAdapter(mAdapter);
        getListView().setEmptyView(mEmptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTodos();
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            Cursor cursor = mAdapter.getCursor();
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        super.onDestroy();
    }

    private Cursor queryTodos() {
        return getContentResolver().query(
                NotePad.Notes.CONTENT_URI,
                PROJECTION,
                NotePad.Notes.COLUMN_NAME_IS_TODO + " = 1",
                null,
                NotePad.Notes.COLUMN_NAME_TODO_DONE + " ASC, "
                        + NotePad.Notes.COLUMN_NAME_TODO_PRIORITY + " DESC, "
                        + "CASE WHEN " + NotePad.Notes.COLUMN_NAME_TODO_DUE_DATE
                        + " = 0 THEN 1 ELSE 0 END ASC, "
                        + NotePad.Notes.COLUMN_NAME_TODO_DUE_DATE + " ASC, "
                        + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " DESC");
    }

    private void refreshTodos() {
        if (mAdapter == null) {
            return;
        }
        Cursor newCursor = queryTodos();
        Cursor oldCursor = mAdapter.swapCursor(newCursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
        updateSummary(newCursor);
    }

    private void updateSummary(Cursor cursor) {
        int pending = 0;
        int completed = 0;
        if (cursor != null) {
            int doneIndex = cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_TODO_DONE);
            while (cursor.moveToNext()) {
                if (cursor.getInt(doneIndex) == 1) {
                    completed++;
                } else {
                    pending++;
                }
            }
            cursor.moveToPosition(-1);
        }
        mSummary.setText(getString(
                R.string.todo_summary_format,
                pending,
                completed));
    }

    private void createTodo() {
        Intent intent = new Intent(Intent.ACTION_INSERT, NotePad.Notes.CONTENT_URI)
                .setClass(this, NoteEditor.class);
        intent.putExtra(NoteEditor.EXTRA_CREATE_TODO, true);
        startActivity(intent);
    }

    private void updateTodo(long noteId, boolean completed) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_TODO_DONE, completed ? 1 : 0);
        values.put(
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                System.currentTimeMillis());
        Uri noteUri = ContentUris.withAppendedId(
                NotePad.Notes.CONTENT_ID_URI_BASE,
                noteId);
        getContentResolver().update(noteUri, values, null, null);
        refreshTodos();
        Toast.makeText(
                this,
                completed ? R.string.todo_marked_done : R.string.todo_marked_pending,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.todo_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add_todo) {
            createTodo();
            return true;
        } else if (id == R.id.menu_all_notes) {
            startActivity(new Intent(this, NotesList.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, id);
        startActivity(new Intent(Intent.ACTION_EDIT, noteUri)
                .setClass(this, NoteEditor.class));
    }

    @Override
    public void onCreateContextMenu(
            ContextMenu menu,
            View view,
            ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            return;
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.todo_context_menu, menu);
        menu.setHeaderTitle(cursor.getString(
                cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_TITLE)));

        boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(
                NotePad.Notes.COLUMN_NAME_TODO_DONE)) == 1;
        menu.findItem(R.id.context_todo_toggle).setTitle(completed
                ? R.string.context_mark_pending
                : R.string.context_mark_done);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        Uri noteUri = ContentUris.withAppendedId(
                NotePad.Notes.CONTENT_ID_URI_BASE,
                info.id);
        int id = item.getItemId();
        if (id == R.id.context_open) {
            startActivity(new Intent(Intent.ACTION_EDIT, noteUri)
                    .setClass(this, NoteEditor.class));
            return true;
        } else if (id == R.id.context_todo_toggle) {
            Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
            if (cursor != null) {
                boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(
                        NotePad.Notes.COLUMN_NAME_TODO_DONE)) == 1;
                updateTodo(info.id, !completed);
            }
            return true;
        } else if (id == R.id.context_delete) {
            getContentResolver().delete(noteUri, null, null);
            refreshTodos();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private class TodoAdapter extends CursorAdapter {

        TodoAdapter(Cursor cursor) {
            super(TodoListActivity.this, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(
                    R.layout.todo_list_item,
                    parent,
                    false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final long noteId = cursor.getLong(cursor.getColumnIndexOrThrow(
                    NotePad.Notes._ID));
            final boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_TODO_DONE)) == 1;
            NoteColorUtils.applyCardBackground(
                    view.findViewById(R.id.todo_card),
                    context,
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            NotePad.Notes.COLUMN_NAME_BACKGROUND_COLOR)));

            String title = cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_TITLE));
            TextView titleView = (TextView) view.findViewById(R.id.todo_title);
            int fontStyle = NoteFontUtils.normalize(cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            NotePad.Notes.COLUMN_NAME_FONT_STYLE)));
            NoteFontUtils.applyBold(titleView, fontStyle);
            titleView.setText(TextUtils.isEmpty(title)
                    ? getString(R.string.note_untitled)
                    : title);
            int priority = Math.max(0, Math.min(2, cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            NotePad.Notes.COLUMN_NAME_TODO_PRIORITY))));
            boolean urgentPending = !completed && priority == 2;
            titleView.setTextColor(context.getResources().getColor(
                    urgentPending ? R.color.danger : R.color.text_primary));
            int paintFlags = titleView.getPaintFlags();
            titleView.setPaintFlags(completed
                    ? paintFlags | Paint.STRIKE_THRU_TEXT_FLAG
                    : paintFlags & ~Paint.STRIKE_THRU_TEXT_FLAG);

            String content = cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_NOTE));
            String preview = content == null ? "" : content.replace('\n', ' ').trim();
            if (preview.length() > 80) {
                preview = preview.substring(0, 80) + "...";
            }
            TextView previewView = (TextView) view.findViewById(R.id.todo_preview);
            NoteFontUtils.apply(previewView, fontStyle);
            previewView.setText(
                    TextUtils.isEmpty(preview)
                            ? getString(R.string.note_preview_empty)
                            : preview);

            CheckBox doneBox = (CheckBox) view.findViewById(R.id.todo_done);
            doneBox.setChecked(completed);
            doneBox.setContentDescription(getString(R.string.checkbox_todo_done));
            doneBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateTodo(noteId, !completed);
                }
            });

            long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_TODO_DUE_DATE));
            boolean overdue = !completed
                    && dueDate > 0
                    && dueDate < System.currentTimeMillis();
            SimpleDateFormat formatter = NoteDateUtils.formatter("yyyy-MM-dd");
            String status = getString(completed
                    ? R.string.todo_completed
                    : R.string.todo_pending);
            List<String> meta = new ArrayList<String>();
            meta.add(status);
            if (priority > 0) {
                meta.add(getString(priority == 2
                        ? R.string.todo_priority_urgent
                        : R.string.todo_priority_important));
            }
            if (dueDate > 0) {
                String date = formatter.format(new Date(dueDate));
                meta.add(getString(overdue
                        ? R.string.todo_overdue_format
                        : R.string.todo_due_date_format, date));
            }
            StringBuilder metaBuilder = new StringBuilder();
            for (int i = 0; i < meta.size(); i++) {
                if (i > 0) {
                    metaBuilder.append(" · ");
                }
                metaBuilder.append(meta.get(i));
            }
            TextView metaView = (TextView) view.findViewById(R.id.todo_meta);
            metaView.setText(metaBuilder.toString());
            metaView.setTextColor(context.getResources().getColor(
                    overdue ? R.color.danger : R.color.accent));
        }
    }
}
