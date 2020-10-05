package com.ilnur.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.ilnur.Json.Root;
import com.ilnur.Json.Tema;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;


public class MyDB extends SQLiteOpenHelper {
    private static String dbname = "jsondb.db";
    private static Context acontext;
    private static String myPath;
    private static SQLiteDatabase sqliteDb;
    private static int version = 10;
    private static MyDB instance;
    private static ContentValues values;
    //private MyDB db;



    public MyDB(Context context){
        super(context, dbname, null, version);
        acontext = context;
    }

    public static MyDB getInstance(Context context){
        if (instance == null){
            instance = new MyDB(context);
            init(instance, false);
        }
        return instance;
    }

    public static void init(MyDB db, boolean External){
        Log.i("INIT", "START");
        instance = db;

        Log.i("LOGI", Environment.getExternalStorageState() );
        if (External){
            myPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/"+acontext.getPackageName()+"/";
            Log.i("PATH", myPath);
        } else {
            myPath = "/data/data/com.reshuege/databases/";
        }
        try {
            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 4096 * 2048);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("FIELDset", "exception");
        }

        if (!checkdb()){
            Log.i("Copy", "START");
            copyDb();
        } else {
            sqliteDb = instance.getReadableDatabase();
            Log.i("VERSION", String.valueOf(sqliteDb.getVersion())+" "+String.valueOf(version));
            if (sqliteDb.getVersion()!=version){
                copyDb();
            }
        }
    }

   /* public static User getUser() {
        SQLiteDatabase sqdb = instance.getReadableDatabase();
        Cursor cursor = sqdb.rawQuery("SELECT login, password, session_id FROM user", null);
        if (cursor.moveToFirst()) {
            User user = new User();
            user.setLogin(cursor.getString(0));
            user.setPassword(cursor.getString(1));
            user.setSession_id(cursor.getString(2));
            cursor.close();
            return user;
        } else {
            return new User(null, null, null);
        }
    }*/

    public static void updateUser(String login, String password, String session_id) {
        SQLiteDatabase sqdb = instance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("login", login);
        values.put("password", password);
        values.put("session_id", session_id);
        int id = sqdb.update("user", values, "login = ?", new String[]{login});
        if (id == 0)
            sqdb.insertWithOnConflict("user", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        values.clear();
    }

    public static void removeUser(String old_login) {
        SQLiteDatabase sqdb = instance.getWritableDatabase();
        sqdb.delete("user", "login = ?", new String[]{old_login});
    }

    // to get Root names
    public static String[] getRootNames(String predmet){
        SQLiteDatabase sqdb = instance.getReadableDatabase();
        //Log.d("getRootnames", predmet);
        if (predmet.contains(" "))
            predmet = predmet.replace(" ", "_");
        Cursor cursor = sqdb.rawQuery("SELECT data FROM " +predmet+" WHERE id = ?",
                new String[]{"-1"});
        cursor.moveToFirst();
        String[] s = cursor.getString(0).split("\n");
        cursor.close();
        return s;
    }


    public static String[] getTemasNames(String key, String id){
        SQLiteDatabase sqdb = instance.getReadableDatabase();
        if (key.contains(" "))
            key = key.replace(" ", "_");
        Cursor cursor = sqdb.rawQuery("SELECT data FROM "+key+" WHERE id = ?", new String[]{id});
        cursor.moveToFirst();
        String[] s = cursor.getString(0).split("\n");
        cursor.close();
        return s;
    }
    public static Tema getTema(String key, String id){
        SQLiteDatabase sqdb = instance.getReadableDatabase();
        if (key.contains(" "))
            key = key.replace(" ", "_");
        Cursor cursor = sqdb.rawQuery("SELECT data FROM "+key+" WHERE id = ?", new String[]{id});
        cursor.moveToFirst();
        Tema tema = new Gson().fromJson(cursor.getString(0), Tema.class);
        cursor.close();
        return tema;
    }

    public static String getTemasData(String key, String id){
        SQLiteDatabase sqdb = instance.getReadableDatabase();
        if (key.contains(" "))
            key = key.replace(" ", "_");
        Cursor cursor = sqdb.rawQuery("SELECT data FROM "+key+" WHERE id = ?", new String[]{id});
        cursor.moveToFirst();
        Tema tema = new Gson().fromJson(cursor.getString(0), Tema.class);
        cursor.close();
        return tema.getData();
    }




    public static void updateDB(Root root){
        String name = root.getName();
        values = new ContentValues();
        values.put("name", root.getName());
        values.put("data", root.getTemasNames());
        values.put("id", "-1");
        if (name.contains(" "))
            name = name.replace(" ", "_");
        int id = sqliteDb.update(name, values, "id = ?", new String[]{"-1"});
        if (id == 0)
            sqliteDb.insertWithOnConflict(name, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        values.clear();
        values = null;
        updateTema(root.getTemas(), name);
    }

    private static void updateTema(Tema[] temas, String table){
        //ContentValues values;
        Tema s;
        for (int i = 0; i<temas.length; i++){
            s = temas[i];
            if (s.getData().equals("") || s.getTemas().length!=0){
                //String data = s.getTemasNames();
                values = new ContentValues();
                values.put("name", s.getName());
                values.put("data", s.getTemasNames());
                values.put("id", s.getId());
                int id = sqliteDb.update(table, values, "id = ?", new String[]{String.valueOf(s.getId())});
                if (id == 0){
                    sqliteDb.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
                values.clear();
                values = null;
                updateTema(s.getTemas(), table);
            } else {
                values = new ContentValues();
                values.put("id", s.getId());
                values.put("name", s.getName());
                //String tmp = new Gson().toJson(s);
                values.put("data", new Gson().toJson(s));
                int id = sqliteDb.update(table, values, "id = ?", new String[]{String.valueOf(s.getId())});
                if (id == 0){
                    sqliteDb.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
                values.clear();
                values = null;
            }
        }
    }

    private static void copyDb(){
        InputStream inputStream = null;
        try {
            inputStream = acontext.getAssets().open(dbname);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String outputfile = myPath + dbname;
        File f = new File(myPath);
        if (!f.exists()) {
            f.mkdir();
        }
        Log.d("f", f.getAbsolutePath());
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[4096];
        int length;
        try {
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sqliteDb = instance.getWritableDatabase();
        Log.i("Copy", "FINISH");
    }

    private static boolean checkdb(){
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(myPath+dbname, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
        /*File dbFile = acontext.getDatabasePath(dbname);
        Log.d("dbPath", dbFile.getAbsolutePath());
        Log.d("dbname", dbname);
        Log.d("mypath", myPath);
        Log.d("stordir", Environment.getExternalStorageDirectory().getAbsolutePath());
        return dbFile.exists();*/
    }
    @Override
    public synchronized void close() {
        if (sqliteDb != null)
            sqliteDb.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("CREATE TABLE IF NOT EXISTS user ("+
                "login TEXT," +
                "password TEXT," +
                "session_id TEXT" +
                ");");
    }

}