package com.example.crudauthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crudauthapp.R
import com.example.crudauthapp.SessionManager
import com.example.crudauthapp.adapter.TaskAdapter
import com.example.crudauthapp.model.Task
import com.example.crudauthapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class CrudFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val role = SessionManager(requireContext()).fetchUserRole()
        if (role != "admin" && role != "administrador") {
            Toast.makeText(requireContext(), "Acceso restringido", Toast.LENGTH_SHORT).show()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
            return View(requireContext())
        }

        val view = inflater.inflate(R.layout.fragment_crud, container, false)
        recyclerView = view.findViewById(R.id.rvCrudTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        session = SessionManager(requireContext())
        val token = session.fetchAuthToken()

        if (token != null) {
            RetrofitClient.taskApi.getAllTasks("Bearer $token").enqueue(object : Callback<List<Task>> {
                override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                    if (response.isSuccessful) {
                        val tasks = response.body() ?: emptyList()
                        recyclerView.adapter = TaskAdapter(tasks) { task -> mostrarDialogoEditar(task, token) }
                    } else {
                        Toast.makeText(requireContext(), "Acceso denegado", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
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
                            recargarFragment()
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
                            recargarFragment()
                        }

                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun recargarFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CrudFragment())
            .commit()
    }
}
