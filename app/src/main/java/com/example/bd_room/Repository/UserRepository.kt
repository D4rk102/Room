package com.example.bd_room.Repository

import com.example.bd_room.DAO.UserDao
import com.example.bd_room.Model.User

class UserRepository(private val userDao: UserDao){
    suspend fun insertar(user: User){
        userDao.insert(user)
    }

    suspend fun getAllUsers(): List<User>{
        return userDao.getAllUsers()
    }
}