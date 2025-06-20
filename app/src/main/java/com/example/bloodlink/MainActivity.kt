package com.example.bloodlink

import User
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: UserAdapter
    private var userList: MutableList<User> = mutableListOf()
    private val db: FirebaseFirestore = Firebase.firestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(listOf(), ::onCallClick, ::onMessageClick)
        recyclerView.adapter = adapter

        setupTabLayout()
        setupSearch()
        setupAddUserButton()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastKnownLocation()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            } else {
                // Permission denied: still load users (distance will be 0)
                loadUsersFromFirestore() // <-- ADDED: always load users
            }
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude
                    }
                    // Always load users, even if location is null
                    loadUsersFromFirestore() // <-- ADDED: always load users
                }
        } else {
            // If permission not granted, still load users
            loadUsersFromFirestore() // <-- ADDED: always load users
        }
    }

    private fun calculateDistance(user: User): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLatitude,
            currentLongitude,
            user.latitude,
            user.longitude,
            results
        )
        return results[0] / 1000 // Convert meters to kilometers
    }

    private fun setupTabLayout() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Donor"))
        tabLayout.addTab(tabLayout.newTab().setText("Acceptor"))
        tabLayout.addTab(tabLayout.newTab().setText("Prfl"))

        // Select the Acceptor tab (index 1) by default
        tabLayout.getTabAt(1)?.select()


    }


    @SuppressLint("DefaultLocale")
    private fun setupSearch() {
        findViewById<EditText>(R.id.searchEditText).addTextChangedListener {
            val query = it.toString().trim().lowercase()
            val filtered = userList.filter { user ->
                user.bloodGroup.contains(query, true) ||
                        user.name.contains(query, true)
            }.map { user ->
                val distanceKm = calculateDistance(user)
                val distanceDisplay = String.format("%.1f KM Away", distanceKm)
                UserWithDistance(user, distanceDisplay)
            }.sortedBy { calculateDistance(it.user) }
            adapter.updateList(filtered)
        }
    }

    private fun setupAddUserButton() {
        findViewById<FloatingActionButton>(R.id.fabAddUser).setOnClickListener {
            showAddUserDialog()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun loadUsersFromFirestore() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                userList.addAll(result.mapNotNull { it.toObject(User::class.java) })
                val userWithDistanceList = userList.map { user ->
                    val distanceKm = calculateDistance(user)
                    val distanceDisplay = String.format("%.1f KM Away", distanceKm)
                    UserWithDistance(user, distanceDisplay)
                }.sortedBy { calculateDistance(it.user) }
                adapter.updateList(userWithDistanceList)
            }
            .addOnFailureListener { e ->
                showToast("Error loading users: ${e.message}")
            }
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null).apply {
            findViewById<EditText>(R.id.etPhone).inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        AlertDialog.Builder(this)
            .setTitle("Add User")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                validateAndAddUser(dialogView)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateAndAddUser(dialogView: android.view.View) {
        val name = dialogView.findViewById<EditText>(R.id.etName).text.toString().trim()
        val bloodGroup = dialogView.findViewById<EditText>(R.id.etBloodGroup).text.toString().trim()
        val latitude =
            dialogView.findViewById<EditText>(R.id.etLatitude).text.toString().toDoubleOrNull()
        val longitude =
            dialogView.findViewById<EditText>(R.id.etLongitude).text.toString().toDoubleOrNull()
        val phone = dialogView.findViewById<EditText>(R.id.etPhone).text.toString().trim()

        when {
            name.isEmpty() -> showToast("Name cannot be empty")
            bloodGroup.isEmpty() -> showToast("Blood group cannot be empty")
            latitude == null -> showToast("Invalid latitude")
            longitude == null -> showToast("Invalid longitude")
            phone.isEmpty() -> showToast("Phone number cannot be empty")
            formatIndianPhone(phone) == null -> showToast("Invalid Indian phone number")
            else -> addUserToFirestore(
                User(
                    name, bloodGroup,
                    latitude, longitude, formatIndianPhone(phone)!!
                )
            )
        }
    }

    private fun formatIndianPhone(phone: String): String? {
        val cleaned = phone.replace("+", "").replace(" ", "")
        return when {
            cleaned.startsWith("91") && cleaned.length == 12 -> "+$cleaned"
            cleaned.length == 10 && cleaned.all { it.isDigit() } -> "+91$cleaned"
            cleaned.length == 13 && cleaned.startsWith("91") -> "+$cleaned"
            cleaned.startsWith("0") && cleaned.length == 11 -> "+91${cleaned.substring(1)}"
            cleaned.startsWith("+91") && cleaned.length == 13 -> cleaned
            else -> null
        }
    }

    private fun addUserToFirestore(user: User) {
        db.collection("users")
            .add(user)
            .addOnSuccessListener {
                loadUsersFromFirestore()
                showToast("User added successfully")
            }
            .addOnFailureListener { e ->
                showToast("Error adding user: ${e.message}")
            }
    }

    private fun onCallClick(phone: String) {
        val formatted = formatIndianPhone(phone)
        if (formatted != null) {
            startActivity(Intent(Intent.ACTION_DIAL, "tel:$formatted".toUri()))
        } else {
            showToast("Invalid Indian phone number")
        }
    }

    private fun onMessageClick(phone: String) {
        val formatted = formatIndianPhone(phone)
        if (formatted != null) {
            startActivity(Intent(Intent.ACTION_SENDTO, "smsto:$formatted".toUri()))
        } else {
            showToast("Invalid Indian phone number")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}



