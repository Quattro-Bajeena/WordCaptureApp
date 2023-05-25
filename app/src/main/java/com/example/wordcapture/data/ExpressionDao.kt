package com.example.wordcapture.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpressionDao {

    @Query("SELECT * FROM expression")
    fun getAll(): List<Expression>

    @Query("SELECT * FROM expression WHERE id=:id")
    fun get(id: Int): Expression

    @Insert
    fun insert(expression: Expression)

}