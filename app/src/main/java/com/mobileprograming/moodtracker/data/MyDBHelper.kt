package com.mobileprograming.moodtracker.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

class MyDBHelper(private val context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        const val DB_NAME = "myDB.db"
        const val DB_VERSION = 1
        const val TABLE_NAME = "diaries"
        const val DATE = "date"
        const val CONTENT = "content"
        const val IMAGE = "image"
        const val MOOD = "mood"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        val createTable = "create table if not exists $TABLE_NAME(" +
                "$DATE long primary key ," +
                "$MOOD int," +
                "$CONTENT text," +
                "$IMAGE BLOB)"
        p0?.let {
            it.execSQL(createTable)
        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        val dropTable = "drop table if exists $TABLE_NAME;"
        p0?.let {
            it.execSQL(dropTable)
            onCreate(it)
        }
    }

    fun getAllDiary(): List<Diary> {
        val strSql = "select * from $TABLE_NAME;"
        val db = readableDatabase
        val cursor = db.rawQuery(strSql, null)
        val mutableList = mutableListOf<Diary>()
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            do {
                val date = cursor.getLong(0)
                val mood = cursor.getInt(1)
                val content = cursor.getString(2)
                val image = cursor.getBlob(3)
                mutableList.add(
                    Diary(
                        date,
                        mood,
                        content,
                        image
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return mutableList.toList()
    }

    fun insert(diary: Diary) {
        val values = ContentValues()
        values.put(DATE, diary.date)
        values.put(MOOD, diary.mood)
        values.put(CONTENT, diary.content)
        values.put(IMAGE, diary.image)

        val w = writableDatabase
        w.replace(TABLE_NAME, null, values)
        w.close()
    }

    fun drawableToByteArray(bitmap: Bitmap?): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun getDiary(date:Long):List<Diary>{
        val strSql = "select * from $TABLE_NAME WHERE $DATE = $date;"
        val db = readableDatabase
        val cursor = db.rawQuery(strSql, null)
        val mutableList = mutableListOf<Diary>()
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            do {
                val date = cursor.getLong(0)
                val mood = cursor.getInt(1)
                val content = cursor.getString(2)
                val image = cursor.getBlob(3)
                mutableList.add(
                    Diary(
                        date,
                        mood,
                        content,
                        image
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return mutableList.toList()
    }
}