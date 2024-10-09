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
    suspend fun deleteById(userId: Int): Int {
        return userDao.deleteById(userId) //llama al metodo deleteById del DAO
    }
    suspend fun actualizar(user: User) {
        userDao.updateUser(user.id, user.nombre, user.apellido, user.edad)
    }

}