package com.example.crudauthapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_login -> replaceFragment(LoginFragment())
                R.id.nav_register -> replaceFragment(RegisterFragment())
               // R.id.nav_crud -> replaceFragment(CrudFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                R.id.nav_admin_panel -> replaceFragment(AdminPanelFragment())
                R.id.nav_logout -> {
                    val session = SessionManager(this)
                    session.clearToken()
                    session.saveUserRole("")
                    val headerView = navView.getHeaderView(0)
                    val emailText = headerView.findViewById<TextView>(R.id.nav_header_email)
                    emailText.text = "No autenticado"

                    // Imagen predeterminada al cerrar sesión
                    val profileImage = headerView.findViewById<CircleImageView>(R.id.nav_header_image)
                    profileImage.setImageResource(R.drawable.ic_user)

                    Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    replaceFragment(LoginFragment())
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        val headerView = navView.getHeaderView(0)
        val emailText = headerView.findViewById<TextView>(R.id.nav_header_email)
        val profileImage = headerView.findViewById<CircleImageView>(R.id.nav_header_image)

        val session = SessionManager(this)
        val token = session.fetchAuthToken()
        val role = session.fetchUserRole()

        // Mostrar u ocultar opción CRUD
       /* val crudItem = navView.menu.findItem(R.id.nav_crud)
        crudItem.isVisible = role == "admin" || role == "administrador"*/

        val adminItem = navView.menu.findItem(R.id.nav_admin_panel)
        adminItem.isVisible = role == "admin" || role == "administrador"


        // Mostrar correo o estado
        if (token != null) {
            emailText.text = "Sesión activa"
        } else {
            emailText.text = "No autenticado"
        }

        // Cargar imagen de perfil desde preferencias
        val prefs = getSharedPreferences("profile", Context.MODE_PRIVATE)
        val savedUri = prefs.getString("profile_image", null)

        if (savedUri != null) {
            try {
                val uri = Uri.parse(savedUri)
                contentResolver.openInputStream(uri)?.close()
                profileImage.setImageURI(uri)
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.ic_user)
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_user)
        }

        replaceFragment(LoginFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
