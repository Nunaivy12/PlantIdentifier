package com.wvxid.planidentifier

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wvxid.planidentifier.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.historyRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.backButton.setOnClickListener { finish() }

        loadHistory()
    }

    private fun loadHistory() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "You need to be logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("users").document(currentUserId)
            .collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No history yet.", Toast.LENGTH_SHORT).show()
                } else {
                    val historyList = documents.toObjects(HistoryItem::class.java)
                    binding.historyRecyclerView.adapter = HistoryAdapter(historyList)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load history: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}