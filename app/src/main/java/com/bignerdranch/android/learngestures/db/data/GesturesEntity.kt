package com.bignerdranch.android.learngestures.db.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Gestures")
data class GesturesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val path: String
)