package com.wvxid.planidentifier

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.wvxid.planidentifier.databinding.ActivityHistoryDetailBinding

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        val plantId = intent.getStringExtra("plant_id")
        val imageUrl = intent.getStringExtra("image_url")

        if (plantId == null || imageUrl == null) {
            Toast.makeText(this, "History data is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load the actual user's image from history
        Glide.with(this)
            .load(imageUrl)
            .into(binding.plantImageView)

        // Load the rest of the plant's details from the 'plants' collection
        loadPlantDetails(plantId)

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadPlantDetails(plantId: String) {
        firestore.collection("plants").document(plantId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val plant = document.toObject(Plant::class.java)
                    plant?.let {
                        binding.plantNameTextView.text = it.commonName
                        binding.genusTextView.text = "Genus: ${it.genus}"
                        binding.familyTextView.text = "Family: ${it.family}"
                        binding.foundInTextView.text = "Found in: ${it.canFound}"
                        binding.healthStatusTextView.text = "Healthy Status: ${it.healthStatus}"
                        binding.suggestionTextView.text = "Suggestion: ${it.suggestion}"
                        binding.descriptionTextView.text = "Description: ${it.description}"
                    }
                } else {
                    Toast.makeText(this, "Plant details not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}