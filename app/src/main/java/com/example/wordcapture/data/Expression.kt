package com.example.wordcapture.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Expression(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val original: String,
    @ColumnInfo val translation: String?,
    @ColumnInfo val time: Date,
    @ColumnInfo val language: String?,
    @ColumnInfo val imageFilename: String?
)
