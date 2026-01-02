package com.wvxid.planidentifier

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.wvxid.planidentifier.databinding.ActivityResultBinding
import java.util.UUID

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val imageUriString = intent.getStringExtra("image_uri")
        if (imageUriString == null) {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val imageUri = Uri.parse(imageUriString)
        binding.plantImageView.setImageURI(imageUri)

        fetchRandomPlantAndSaveHistory(imageUri)

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchRandomPlantAndSaveHistory(imageUri: Uri) {
        firestore.collection("plants").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener
                
                val randomPlantDoc = documents.documents.random()
                val randomPlant = randomPlantDoc.toObject(Plant::class.java)?.copy(id = randomPlantDoc.id)

                if (randomPlant != null) {
                    displayPlantInfo(randomPlant)
                    // Start the upload process
                    uploadImageAndSaveHistory(imageUri, randomPlant.id)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ResultActivity", "Failed to fetch plant data.", e)
                Toast.makeText(this, "Failed to fetch plant data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayPlantInfo(plant: Plant) {
        binding.plantNameTextView.text = plant.commonName
        binding.genusTextView.text = plant.genus
        binding.familyTextView.text = plant.family
        binding.foundInTextView.text = plant.canFound
        binding.healthStatusTextView.text = plant.healthStatus
        binding.suggestionTextView.text = plant.suggestion
        binding.descriptionTextView.text = plant.description
    }

    private fun uploadImageAndSaveHistory(imageUri: Uri, plantId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val imageRef = storage.reference.child("history/${currentUserId}/${UUID.randomUUID()}")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { 
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Once image is uploaded, save the history with the URL
                    saveHistoryToFirestore(currentUserId, plantId, downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("ResultActivity", "Image upload failed", e)
                Toast.makeText(this, "Failed to save history image.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveHistoryToFirestore(userId: String, plantId: String, imageUrl: String) {
        val historyEntry = hashMapOf(
            "plantId" to plantId,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .collection("history").add(historyEntry)
            .addOnSuccessListener {
                Log.d("ResultActivity", "History with image URL saved successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("ResultActivity", "Failed to save history entry to Firestore", e)
            }
    }
}