package com.example.crudauthapp

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crudauthapp.adapter.UserAdapter
import com.example.crudauthapp.model.User
import com.example.crudauthapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog


class AdminPanelFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState:
    Bundle? ): View
    {
        val view = inflater.inflate(R.layout.fragment_admin_panel, container, false)
        rvUsers = view.findViewById(R.id.rvUsers)
        rvUsers.layoutManager = LinearLayoutManager(requireContext())

        val token = SessionManager(requireContext()).fetchAuthToken()

        val btnAddUser = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btnAddUser)

        btnAddUser.setOnClickListener {
            token?.let { mostrarDialogoRegistrarUsuario(it) }
        }

        if (token != null) {
            RetrofitClient.adminApi.getAllUsers("Bearer $token").enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful) {
                        val users = response.body() ?: emptyList()
                        rvUsers.adapter = UserAdapter(
                            users,
                            onChangeRole = { user -> cambiarRol(user, token) },
                            onDelete = { user -> eliminarUsuario(user, token) },
                            onViewTasks = { user -> verTareasDeUsuario(user) } // ✅ parámetro agregado
                        )
                    } else {
                        Toast.makeText(requireContext(), "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }

    private fun cambiarRol(user: User, token: String) {
        val nuevoRol = if (user.role == "admin") "usuario" else "admin"
        val data = mapOf("role" to nuevoRol)

        RetrofitClient.adminApi.updateUserRole("Bearer $token", user.id, data)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    Toast.makeText(requireContext(), "Rol actualizado", Toast.LENGTH_SHORT).show()
                    recargar()
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error al actualizar rol", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun eliminarUsuario(user: User, token: String) {
        RetrofitClient.adminApi.deleteUser("Bearer $token", user.id)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    recargar()
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun verTareasDeUsuario(user: User) {
        val fragment = TareasUsuarioFragment()
        fragment.arguments = Bundle().apply { putInt("userId", user.id) }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun recargar() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AdminPanelFragment())
            .commit()
    }

    private fun mostrarDialogoRegistrarUsuario(token: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_register_user, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etRole = dialogView.findViewById<EditText>(R.id.etRole)

        AlertDialog.Builder(requireContext())
            .setTitle("Registrar Nuevo Usuario")
            .setView(dialogView)
            .setPositiveButton("Registrar") { _, _ ->
                val data = mapOf(
                    "name" to etName.text.toString(),
                    "email" to etEmail.text.toString(),
                    "password" to etPassword.text.toString(),
                    "role" to etRole.text.toString()
                )

                RetrofitClient.authApi.registerUserAsAdmin("Bearer $token", data)
                    .enqueue(object : Callback<Map<String, Any>> {
                        override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Usuario registrado", Toast.LENGTH_SHORT).show()
                                recargar()
                            } else {
                                Toast.makeText(requireContext(), "Error al registrar", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}
