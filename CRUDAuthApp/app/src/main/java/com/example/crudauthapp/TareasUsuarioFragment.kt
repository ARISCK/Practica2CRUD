package com.example.crudauthapp

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crudauthapp.adapter.TaskAdapter
import com.example.crudauthapp.model.Task
import com.example.crudauthapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TareasUsuarioFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getInt("userId") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tareas_usuario, container, false)
        recyclerView = view.findViewById(R.id.rvTareasUsuario)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val token = SessionManager(requireContext()).fetchAuthToken()

        if (token != null && userId != -1) {
            RetrofitClient.adminApi.getTasksByUserId("Bearer $token", userId)
                .enqueue(object : Callback<List<Task>> {
                    override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                        if (response.isSuccessful) {
                            val tareas = response.body() ?: emptyList()
                            recyclerView.adapter = TaskAdapter(tareas) { task ->
                                mostrarDialogoEditar(task, token)
                            }
                        } else {
                            Toast.makeText(requireContext(), "Error al obtener tareas", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // ✅ Aquí agregas el botón fabAddTask
        val fabAdd = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTask)
        fabAdd.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_task, null)
            val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
            val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

            AlertDialog.Builder(requireContext())
                .setTitle("Nueva tarea para usuario")
                .setView(dialogView)
                .setPositiveButton("Crear") { _, _ ->
                    val title = etTitle.text.toString()
                    val description = etDescription.text.toString()

                    val taskData = mapOf("title" to title, "description" to description)
                    val token = SessionManager(requireContext()).fetchAuthToken()

                    if (token != null) {
                        RetrofitClient.adminApi.createTaskForUser("Bearer $token", userId, taskData)
                            .enqueue(object : Callback<Map<String, Any>> {
                                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(requireContext(), "Tarea creada", Toast.LENGTH_SHORT).show()
                                        recargar()
                                    } else {
                                        Toast.makeText(requireContext(), "Error al crear tarea", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                    Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        return view
    }


    private fun mostrarDialogoEditar(task: Task, token: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        etTitle.setText(task.title)
        etDescription.setText(task.description)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Tarea")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val data = mapOf("title" to etTitle.text.toString(), "description" to etDescription.text.toString())

                RetrofitClient.taskApi.updateTask("Bearer $token", task.id, data)
                    .enqueue(object : Callback<Map<String, Any>> {
                        override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                            Toast.makeText(requireContext(), "Actualizada", Toast.LENGTH_SHORT).show()
                            recargar()
                        }

                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Eliminar") { _, _ ->
                RetrofitClient.taskApi.deleteTask("Bearer $token", task.id)
                    .enqueue(object : Callback<Map<String, Any>> {
                        override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                            Toast.makeText(requireContext(), "Eliminada", Toast.LENGTH_SHORT).show()
                            recargar()
                        }

                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun recargar() {
        val bundle = Bundle()
        bundle.putInt("userId", userId)
        val nuevoFragmento = TareasUsuarioFragment()
        nuevoFragmento.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, nuevoFragmento)
            .commit()
    }
}
