package com.example.crudauthapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crudauthapp.R
import com.example.crudauthapp.model.User

class UserAdapter(
    private val users: List<User>,
    private val onChangeRole: (User) -> Unit,
    private val onDelete: (User) -> Unit,
    private val onViewTasks: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvRole: TextView = itemView.findViewById(R.id.tvUserRole)
        val btnRole: Button = itemView.findViewById(R.id.btnChangeRole)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteUser)
        val btnViewTasks: Button = itemView.findViewById(R.id.btnViewTasks)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_admin, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.name
        holder.tvEmail.text = user.email
        holder.tvRole.text = "Rol: ${user.role}"

        holder.btnRole.text = if (user.role == "admin") "Quitar Admin" else "Hacer Admin"
        holder.btnRole.setOnClickListener { onChangeRole(user) }
        holder.btnDelete.setOnClickListener { onDelete(user) }
        holder.btnViewTasks.setOnClickListener { onViewTasks(user) }

    }

    override fun getItemCount() = users.size
}
