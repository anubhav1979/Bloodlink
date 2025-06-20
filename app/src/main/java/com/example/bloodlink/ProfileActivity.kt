package com.example.bloodlink

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bloodlink.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadUserData()

        binding.editButton.setOnClickListener {
            toggleEdit(true)
        }

        binding.saveButton.setOnClickListener {
            saveUserData()
        }

        toggleEdit(false) // Fields are read-only by default
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.nameEditText.setText(document.getString("name") ?: "")
                    binding.emailEditText.setText(document.getString("email") ?: "")
                    binding.phoneEditText.setText(document.getString("phone") ?: "")
                    binding.bloodGroupEditText.setText(document.getString("bloodGroup") ?: "")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return

        val updatedData = hashMapOf(
            "name" to binding.nameEditText.text.toString(),
            "email" to binding.emailEditText.text.toString(),
            "phone" to binding.phoneEditText.text.toString(),
            "bloodGroup" to binding.bloodGroupEditText.text.toString()
        )

        firestore.collection("users").document(uid)
            .set(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                toggleEdit(false)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleEdit(enable: Boolean) {
        isEditing = enable

        binding.nameEditText.isEnabled = enable
        binding.phoneEditText.isEnabled = enable
        binding.bloodGroupEditText.isEnabled = enable
        binding.emailEditText.isEnabled = false // email stays read-only

        binding.editButton.visibility = if (enable) View.GONE else View.VISIBLE
        binding.saveButton.visibility = if (enable) View.VISIBLE else View.GONE
    }
}
