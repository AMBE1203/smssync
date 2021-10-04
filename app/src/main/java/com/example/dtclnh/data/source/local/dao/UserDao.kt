package com.example.dtclnh.data.source.local.dao

import androidx.room.*
import com.example.dtclnh.domain.model.UserModel

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userModel: UserModel): Long

    @Query("SELECT * FROM User")
    fun loadAll(): MutableList<UserModel>

    @Delete
    fun delete(userModel: UserModel)

    @Query("DELETE FROM USER")
    fun deleteAll()

    @Query("SELECT * FROM USER where idUser = :userId")
    fun loadOneByPhotoId(userId: Long): UserModel?


    @Update
    fun update(userModel: UserModel)
}