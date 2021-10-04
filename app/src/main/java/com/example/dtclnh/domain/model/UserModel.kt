package com.example.dtclnh.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "USER")
data class UserModel(@PrimaryKey(autoGenerate = true) var idUser: Int =0, var userName: String, var password: String)