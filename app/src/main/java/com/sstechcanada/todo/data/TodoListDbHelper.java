package com.sstechcanada.todo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

import static com.sstechcanada.todo.data.TodoListContract.TodoListEntry.COLUMN_CATEGORY;
import static com.sstechcanada.todo.data.TodoListContract.TodoListEntry.COLUMN_CATEGORY_COUNT;
import static com.sstechcanada.todo.data.TodoListContract.TodoListEntry.COLUMN_ID;
import static com.sstechcanada.todo.data.TodoListContract.TodoListEntry.TABLE_NAME;

public class TodoListDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "todolist.db";
    private static final int DATABASE_VERSION = 3;

    public TodoListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TODOLIST_TABLE =

                "CREATE TABLE " + TABLE_NAME + " (" +
                        TodoListContract.TodoListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TodoListContract.TodoListEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                        TodoListContract.TodoListEntry.COLUMN_PRIORITY + " INTEGER NOT NULL, " +
                        TodoListContract.TodoListEntry.COLUMN_DUE_DATE + " LONG NOT NULL, " +
                        TodoListContract.TodoListEntry.COLUMN_CATEGORY + " TEXT, " +
                        TodoListContract.TodoListEntry.COLUMN_CATEGORY_COUNT + " INTEGER ," +
                        TodoListContract.TodoListEntry.COLUMN_COMPLETED + " INTEGER NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_TODOLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public int updateCategory(String category, int cat_count, int id){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_CATEGORY_COUNT, cat_count);
        int count = database.update(TABLE_NAME, values, COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
        return count;
    }

    public ArrayList<HashMap<String, String>> getUser(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> userlist = new ArrayList<>();

        String query = "SELECT " +
                "" + COLUMN_CATEGORY + "," +
                "" + COLUMN_CATEGORY_COUNT +
                " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_ID + " = " + id;
        Cursor cursor = database.rawQuery(query, null);
        while (cursor.moveToNext()){
            HashMap<String, String> user = new HashMap<>();
            user.put(COLUMN_CATEGORY, cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
            user.put(COLUMN_CATEGORY_COUNT, cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_COUNT)));
            userlist.add(user);
        }
        return userlist;
    }
}
