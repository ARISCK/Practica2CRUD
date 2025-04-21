package com.example.crudauthapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crudauthapp.adapter.TaskAdapter
import com.example.crudauthapp.model.Task
import com.example.crudauthapp.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.crudauthapp.utils.FileUtil
import com.google.android.material.navigation.NavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ProfileFragment : Fragment() {

    private lateinit var imgProfile: CircleImageView
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val session = SessionManager(requireContext())
        val token = session.fetchAuthToken()

        // 📷 Configurar selección de imagen de perfil
        imgProfile = view.findViewById(R.id.imgProfile)

        // Cargar imagen guardada al iniciar
        val prefs = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE)
        val savedUri = prefs.getString("profile_image", null)
        if (savedUri != null) {
            val uri = Uri.parse(savedUri)
            try {
                requireContext().contentResolver.openInputStream(uri)?.close()
                imgProfile.setImageURI(uri)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Imagen no encontrada", Toast.LENGTH_SHORT).show()
            }
        }


        // Registrar launcher moderno para abrir galería
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    imgProfile.setImageURI(imageUri)
                    prefs.edit().putString("profile_image", imageUri.toString()).apply()

                    // Actualizar imagen en Drawer
                    val activity = activity as? MainActivity
                    val drawer = activity?.findViewById<NavigationView>(R.id.nav_view)
                    val header = drawer?.getHeaderView(0)
                    val profileImage = header?.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.nav_header_image)
                    profileImage?.setImageURI(imageUri)

                    // Subir imagen al servidor (nuevo)
                    val file = FileUtil.getFile(requireContext(), imageUri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val multipart = MultipartBody.Part.createFormData("photo", file.name, requestFile)

                    val token = SessionManager(requireContext()).fetchAuthToken()
                    if (token != null) {
                        RetrofitClient.authApi.uploadProfilePhoto("Bearer $token", multipart)
                            .enqueue(object : Callback<String> {
                                override fun onResponse(call: Call<String>, response: Response<String>) {
                                    Toast.makeText(requireContext(), "Foto subida al servidor", Toast.LENGTH_SHORT).show()
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    Toast.makeText(requireContext(), "Error al subir foto", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }


                // También actualizar imagen en Drawer (si se muestra)
                val activity = activity as? MainActivity
                val drawer = activity?.findViewById<NavigationView>(R.id.nav_view)
                val header = drawer?.getHeaderView(0)
                val profileImage = header?.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.nav_header_image)
                profileImage?.setImageURI(imageUri)
            }
        }


        imgProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        // 📋 Cargar tareas
        if (token != null) {
            RetrofitClient.taskApi.getMyTasks("Bearer $token").enqueue(object : Callback<List<Task>> {
                override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                    if (response.isSuccessful) {
                        val tasks = response.body() ?: emptyList()
                        recyclerView.adapter = TaskAdapter(tasks) { task ->
                            mostrarDialogoEditar(task, token)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error al obtener tareas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show()
                }
            })

            // ➕ Botón flotante para nueva tarea
            val fab = view.findViewById<FloatingActionButton>(R.id.fabAddTask)
            fab.setOnClickListener {
                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_task, null)
                val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
                val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

                AlertDialog.Builder(requireContext())
                    .setTitle("Nueva Tarea")
                    .setView(dialogView)
                    .setPositiveButton("Crear") { _, _ ->
                        val title = etTitle.text.toString()
                        val description = etDescription.text.toString()
                        val taskData = mapOf("title" to title, "description" to description)

                        RetrofitClient.taskApi.createTask("Bearer $token", taskData)
                            .enqueue(object : Callback<Map<String, Any>> {
                                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(requireContext(), "Tarea creada", Toast.LENGTH_SHORT).show()
                                        recargarFragment()
                                    } else {
                                        Toast.makeText(requireContext(), "Error al crear", Toast.LENGTH_SHORT).show()
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
            .replace(R.id.fragment_container, ProfileFragment())
            .commit()
    }
}
