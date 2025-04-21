package com.example.crudauthapp

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.crudauthapp.model.UserRequest
import com.example.crudauthapp.model.AuthResponse
import com.example.crudauthapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.etEmail).text.toString()
            val password = view.findViewById<EditText>(R.id.etPassword).text.toString()

            // Validación antes de enviar la solicitud
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Aquí corregimos: especificamos name = null explícitamente
            val request = UserRequest(name = null, email = email, password = password)
            val sessionManager = SessionManager(requireContext())

            RetrofitClient.authApi.login(request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null && body.token != null) {
                        sessionManager.saveAuthToken(body.token)
                        body.role?.let { sessionManager.saveUserRole(it) }
                        Toast.makeText(requireContext(), "Bienvenido, rol: ${body.role}", Toast.LENGTH_SHORT).show()

                        val destino = when (body.role?.lowercase()) {
                            "admin", "administrador" -> CrudFragment()
                            "usuario" -> ProfileFragment()
                            else -> null
                        }

                        destino?.let {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, it)
                                .addToBackStack(null)
                                .commit()
                        }

                    } else {
                        Toast.makeText(requireContext(), body?.error ?: "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }


        return view
    }
}
