package com.wvxid.planidentifier

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wvxid.planidentifier.databinding.ActivityPeopleBinding

class PeopleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPeopleBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.peopleRecyclerView.layoutManager = LinearLayoutManager(this)

        loadUsers()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadUsers() {
        val currentUserId = auth.currentUser?.uid

        firestore.collection("users").get()
            .addOnSuccessListener { documents ->
                val userList = documents.mapNotNull { doc ->
                    if (doc.id != currentUserId) {
                        doc.toObject(User::class.java).copy(id = doc.id)
                    } else {
                        null
                    }
                }
                binding.peopleRecyclerView.adapter = PeopleAdapter(userList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}