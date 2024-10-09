package com.example.bd_room

import UserApp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bd_room.DAO.UserDao
import com.example.bd_room.Database.UserDatabase
import com.example.bd_room.Repository.UserRepository
import com.example.bd_room.ui.theme.BD_RoomTheme

class MainActivity : ComponentActivity() {
    //lateinit evita el nulo por lo cual se debe de inicializar mas adelante
    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = UserDatabase.getDatabase(applicationContext)
        userDao = db.userDao()
        userRepository=UserRepository(userDao) // Inicializa user Repository
        enableEdgeToEdge()
        setContent {
            UserApp(userRepository)
        }
    }
}

