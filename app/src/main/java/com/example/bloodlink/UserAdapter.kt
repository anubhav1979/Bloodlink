package com.example.bloodlink

import User
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class UserWithDistance(
    val user: User,
    val distanceDisplay: String
)
class UserAdapter(
    private var users: List<UserWithDistance>,
    private val onCallClick: (String) -> Unit,
    private val onMessageClick: (String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBloodGroup: TextView = itemView.findViewById(R.id.tvBloodGroup)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        val btnCall: ImageButton = itemView.findViewById(R.id.btnCall)
        val btnMessage: ImageButton = itemView.findViewById(R.id.btnMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val userWithDistance = users[position]
        val user = userWithDistance.user
        holder.tvBloodGroup.text = user.bloodGroup
        holder.tvName.text = user.name
        holder.tvDistance.text = userWithDistance.distanceDisplay
        holder.btnCall.setOnClickListener { onCallClick(user.phone) }
        holder.btnMessage.setOnClickListener { onMessageClick(user.phone) }
    }

    override fun getItemCount() = users.size

    fun updateList(newUsers: List<UserWithDistance>) {
        users = newUsers
        notifyDataSetChanged()

    }
}
