package com.wvxid.planidentifier

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wvxid.planidentifier.databinding.ActivityMyFavoritesBinding

class MyFavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyFavoritesBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        binding.favoritesRecyclerView.layoutManager = GridLayoutManager(this, 2)

        if (currentUserId == null) {
            Toast.makeText(this, "You need to be logged in to see favorites", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadFavoritePlants()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadFavoritePlants() {
        firestore.collection("users").document(currentUserId!!)
            .collection("favorites").get()
            .addOnSuccessListener { favoriteDocs ->
                if (favoriteDocs.isEmpty) {
                    Toast.makeText(this, "You have no favorite plants yet.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val plantIds = favoriteDocs.map { it.getString("plantId")!! }
                fetchPlantDetails(plantIds)
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to load favorites", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchPlantDetails(plantIds: List<String>) {
        if (plantIds.isEmpty()) return

        firestore.collection("plants").whereIn("__name__", plantIds).get()
            .addOnSuccessListener { plantDocs ->
                val favoritePlants = plantDocs.map {
                    it.toObject(Plant::class.java).copy(id = it.id)
                }
                binding.favoritesRecyclerView.adapter = PlantAdapter(favoritePlants) { plant ->
                    val intent = Intent(this, PlantDetailActivity::class.java)
                    intent.putExtra("plant_id", plant.id)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load plant details", Toast.LENGTH_SHORT).show()
            }
    }
}