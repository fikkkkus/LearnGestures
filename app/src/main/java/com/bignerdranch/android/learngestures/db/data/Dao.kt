package com.bignerdranch.android.learngestures.db.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Query("SELECT * FROM Gestures")
    fun getAllGestures(): Flow<List<GesturesEntity>>
}