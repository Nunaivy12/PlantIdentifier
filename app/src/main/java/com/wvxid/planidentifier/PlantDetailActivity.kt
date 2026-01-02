package com.wvxid.planidentifier

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wvxid.planidentifier.databinding.ActivityPlantDetailBinding

class PlantDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantDetailBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var plantId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        plantId = intent.getStringExtra("plant_id")
        currentUserId = auth.currentUser?.uid

        if (plantId == null || currentUserId == null) {
            Toast.makeText(this, "Plant or user not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPlantDetails()
        updateFavoriteButtonStatus()

        binding.addToFavoritesButton.setOnClickListener {
            toggleFavorite()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadPlantDetails() {
        firestore.collection("plants").document(plantId!!).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val plant = document.toObject(Plant::class.java)?.copy(id = document.id)
                    plant?.let {
                        binding.plantNameTextView.text = it.commonName
                        binding.genusTextView.text = it.genus
                        binding.familyTextView.text = it.family
                        binding.foundInTextView.text = it.canFound
                        binding.descriptionTextView.text = it.description

                        val imageResId = resources.getIdentifier(it.imageName, "drawable", packageName)
                        if (imageResId != 0) {
                            Glide.with(this).load(imageResId).into(binding.plantImageView)
                        }
                    }
                } else {
                    Toast.makeText(this, "Plant details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to load plant details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFavoriteButtonStatus() {
        val favoriteRef = firestore.collection("users").document(currentUserId!!)
            .collection("favorites").document(plantId!!)

        favoriteRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                binding.addToFavoritesButton.text = "Remove from Favorites"
            } else {
                binding.addToFavoritesButton.text = "Add to Favorites"
            }
        }
    }

    private fun toggleFavorite() {
        val favoriteRef = firestore.collection("users").document(currentUserId!!)
            .collection("favorites").document(plantId!!)

        favoriteRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Remove from favorites
                favoriteRef.delete().addOnSuccessListener {
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    updateFavoriteButtonStatus()
                }
            } else {
                // Add to favorites
                val favoriteData = hashMapOf("plantId" to plantId)
                favoriteRef.set(favoriteData).addOnSuccessListener {
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                    updateFavoriteButtonStatus()
                }
            }
        }
    }
}