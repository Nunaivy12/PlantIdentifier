package com.wvxid.planidentifier

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.wvxid.planidentifier.databinding.ActivitySpeciesListBinding
import com.google.firebase.firestore.FirebaseFirestore

class SpeciesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpeciesListBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpeciesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        val category = intent.getStringExtra("CATEGORY") ?: ""
        binding.categoryTitle.text = category

        binding.speciesRecyclerView.layoutManager = GridLayoutManager(this, 2)

        fetchPlants(category)

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchPlants(category: String) {
        firestore.collection("plants")
            .whereEqualTo("category", category.lowercase())
            .get()
            .addOnSuccessListener { documents ->
                val plantList = documents.map { doc ->
                    doc.toObject(Plant::class.java).copy(id = doc.id) // Use Firestore document ID
                }
                binding.speciesRecyclerView.adapter = PlantAdapter(plantList) { plant ->
                    val intent = Intent(this, PlantDetailActivity::class.java)
                    intent.putExtra("plant_id", plant.id)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching plants: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}