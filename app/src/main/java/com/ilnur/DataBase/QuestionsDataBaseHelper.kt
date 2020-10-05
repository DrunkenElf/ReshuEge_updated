package com.ilnur.DataBase

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class QuestionsDataBaseHelper(context: Context, tableName: String) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    internal var TABLE_NAME: String? = null
    internal var DB_PATH: String

    init {
        TABLE_NAME = tableName
        DB_PATH = context.getDatabasePath(DB_NAME).path

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table " + TABLE_NAME + " ("
                + "id integer primary key autoincrement,"
                + "question_name text,"
                + "body text,"
                + "solution text,"
                + "task integer,"
                + "the_text text,"
                + "answer text,"
                + "type integer,"
                + "question_id integer,"
                + "category integer);")
        Log.d("myLogs", "Database created")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun addTable(tableName: String) {
        val db = writableDatabase
        db.execSQL("create table " + tableName + " ("
                + "id integer primary key autoincrement,"
                + "question_name text,"
                + "body text,"
                + "solution text,"
                + "task integer,"
                + "the_text text,"
                + "answer text,"
                + "type integer,"
                + "question_id integer,"
                + "category integer" + ");")
        Log.d("myLogs", "Table added")
    }

    fun addTableThemes(tableName: String) {
        val db = writableDatabase
        db.execSQL("create table " + tableName + " ("
                + "id integer primary key autoincrement,"
                + "theme_id integer,"
                + "childs boolean,"
                + "theme_name text" + ");")
        Log.d("myLogs", "Table added")
    }

    fun addTableTheory(tableName: String) {
        val db = writableDatabase
        db.execSQL("create table " + tableName + " ("
                + "id integer primary key autoincrement,"
                + "parent_id integer,"
                + "children integer,"
                + "current_id integer,"
                + "title text,"
                + "theory text" + ");")
        Log.d("myLogs", "Table added")
    }

    fun updateTable(tableName: String) {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $tableName")
        addTable(tableName)
    }

    fun updateTableThemes(tableName: String) {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $tableName")
        addTableThemes(tableName)
    }

    fun updateTableTheory(tableName: String) {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $tableName")
        addTableTheory(tableName)
    }

    fun checkTable(tableName: String): Boolean {

        val db = writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", arrayOf("table", tableName))
        if (!cursor.moveToFirst()) {
            return false
        }
        val count = cursor.getInt(0)
        cursor.close()
        if (count > 0 && !tableName.contains("_")) {
            val cur2 = db.rawQuery("SELECT * FROM $tableName WHERE 0", null)
            val columnNames = cur2.columnNames
            var found: Boolean? = false
            for (i in columnNames.indices) {
                if (columnNames[i] == "category") {
                    found = true
                    break
                }
            }
            cur2.close()
            if ((!found!!)) {
                Log.d("myLogs", "table $tableName structure wrong $columnNames")
                updateTable(tableName)
                return false
            }
        }
        return count > 0
    }

    companion object {

        private val DB_NAME = "questionsDB.sqlite"
        val DB_VERSION = 1
        private val values: ContentValues? = null
    }

}
