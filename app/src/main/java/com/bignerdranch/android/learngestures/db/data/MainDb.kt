package com.bignerdranch.android.learngestures.db.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


@Database(
    entities = [
        GesturesEntity::class
    ],
    version = 1,
    exportSchema = false
)

abstract class MainDb : RoomDatabase() {
    abstract val dao: Dao

    companion object {
        fun getDataBase(context: Context): MainDb {
                return Room.databaseBuilder(
                    context,
                    MainDb::class.java,
                    "Gestures.db"
                ).createFromAsset("database/Gestures.db")
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}