package com.example.bd_room.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bd_room.Model.User
import com.example.bd_room.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UserApp(userRepository: UserRepository) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableStateOf(-1) } // Guardar el ID del usuario seleccionado para modificar
    var scope = rememberCoroutineScope()

    var context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(text = "Nombre") },
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = edad,
            onValueChange = { edad = it },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val user = User(
                    nombre = nombre,
                    apellido = apellido,
                    edad = edad.toIntOrNull() ?: 0
                )
                scope.launch {
                    withContext(Dispatchers.IO) {
                        if (selectedUserId == -1) {
                            userRepository.insertar(user) // Si no hay usuario seleccionado, insertar nuevo
                        } else {
                            userRepository.actualizar(user.copy(id = selectedUserId)) // Modificar si hay usuario seleccionado
                        }
                    }
                    Toast.makeText(context, if (selectedUserId == -1) "Usuario Registrado" else "Usuario Modificado", Toast.LENGTH_SHORT).show()
                    selectedUserId = -1 // Resetear el ID después de la modificación
                    nombre = ""
                    apellido = ""
                    edad = ""
                }
            }) {
            Text(text = if (selectedUserId == -1) "Registrar" else "Modificar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        var users by remember { mutableStateOf(listOf<User>()) }

        Button(
            onClick = {
                scope.launch{
                    users = withContext(Dispatchers.IO){
                        userRepository.getAllUsers()
                    }
                }
            }
        ){
            Text("Listar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            users.forEach { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Información del usuario
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${user.nombre} ${user.apellido} ${user.edad}")
                    }

                    // Icono de lápiz para modificar
                    IconButton(
                        onClick = {
                            selectedUserId = user.id
                            nombre = user.nombre
                            apellido = user.apellido
                            edad = user.edad.toString()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Modificar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Icono de bote de basura para eliminar
                    IconButton(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    userRepository.deleteById(user.id) // Eliminar usuario por id
                                }
                                users = withContext(Dispatchers.IO) {
                                    userRepository.getAllUsers()
                                }
                                Toast.makeText(context, "Usuario Eliminado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
