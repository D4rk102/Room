import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var edad by rememberSaveable { mutableStateOf("") }
    var selectedUserId by rememberSaveable { mutableStateOf(-1) }
    var users by rememberSaveable { mutableStateOf(listOf<User>()) }

    var scope = rememberCoroutineScope()
    var context = LocalContext.current

    // Estado para manejo de cuadros de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showModifyDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var userToModify by remember { mutableStateOf<User?>(null) }

    // Estado para manejar el error en el registro
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        TextField(
            value = nombre,
            onValueChange = {
                nombre = it
                showError = false
            },
            label = { Text(text = "Nombre") },
            isError = showError && nombre.isEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = apellido,
            onValueChange = {
                apellido = it
                showError = false
            },
            label = { Text("Apellido") },
            isError = showError && apellido.isEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = edad,
            onValueChange = {
                edad = it
                showError = false
            },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            isError = showError && (edad.isEmpty() || edad.toIntOrNull() == null || edad.toIntOrNull() !in 1..100)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val edadInt = edad.toIntOrNull()
                if (nombre.isEmpty() || apellido.isEmpty() || edad.isEmpty() || edadInt == null || edadInt !in 1..100) {
                    showError = true
                    errorMessage = when {
                        nombre.isEmpty() -> "El campo Nombre es obligatorio."
                        apellido.isEmpty() -> "El campo Apellido es obligatorio."
                        edad.isEmpty() -> "El campo Edad es obligatorio."
                        edadInt == null -> "La edad debe ser un número válido."
                        edadInt !in 1..100 -> "La edad debe estar entre 1 y 100 años."
                        else -> ""
                    }
                    return@Button
                }

                val user = User(
                    nombre = nombre,
                    apellido = apellido,
                    edad = edadInt
                )
                scope.launch {
                    withContext(Dispatchers.IO) {
                        if (selectedUserId == -1) {
                            userRepository.insertar(user)
                        } else {
                            userRepository.actualizar(user.copy(id = selectedUserId))
                        }
                    }
                    Toast.makeText(context, if (selectedUserId == -1) "Usuario Registrado" else "Usuario Modificado", Toast.LENGTH_SHORT).show()
                    selectedUserId = -1
                    nombre = ""
                    apellido = ""
                    edad = ""
                }
            }
        ) {
            Text(text = if (selectedUserId == -1) "Registrar" else "Modificar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    users = withContext(Dispatchers.IO) {
                        userRepository.getAllUsers()
                    }
                }
            }
        ) {
            Text("Listar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(users) { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${user.nombre} ${user.apellido} ${user.edad}")
                    }

                    IconButton(
                        onClick = {
                            userToModify = user
                            showModifyDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Modificar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            userToDelete = user
                            showDeleteDialog = true
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

        // Cuadro de confirmación para eliminar
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            userToDelete?.let {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        userRepository.deleteById(it.id)
                                    }
                                    users = withContext(Dispatchers.IO) {
                                        userRepository.getAllUsers()
                                    }
                                }
                                Toast.makeText(context, "Usuario Eliminado", Toast.LENGTH_SHORT).show()
                            }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Estás seguro de que deseas eliminar este usuario?") }
            )
        }

        // Cuadro de confirmación para modificar
        if (showModifyDialog) {
            AlertDialog(
                onDismissRequest = { showModifyDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            userToModify?.let {
                                selectedUserId = it.id
                                nombre = it.nombre
                                apellido = it.apellido
                                edad = it.edad.toString()
                            }
                            showModifyDialog = false
                        }
                    ) {
                        Text("Modificar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showModifyDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar modificación") },
                text = { Text("¿Estás seguro de que deseas modificar este usuario?") }
            )
        }
    }
}

