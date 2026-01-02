package com.wvxid.planidentifier

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wvxid.planidentifier.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var visitedUserId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        visitedUserId = intent.getStringExtra("user_id")
        currentUserId = auth.currentUser?.uid

        if (visitedUserId == null || currentUserId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        displayUserProfile()
        displayUserPosts()
        updateFriendButtonStatus()

        binding.addFriendButton.setOnClickListener {
            toggleFriendship()
        }
    }

    private fun displayUserProfile() {
        firestore.collection("users").document(visitedUserId!!).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    supportActionBar?.title = user?.name
                    binding.bioTextView.text = user?.bio ?: "No bio available."
                    binding.emailTextView.text = user?.email
                } else {
                    Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayUserPosts() {
        val postList = mutableListOf<Post>()
        val postAdapter = PostAdapter(postList)
        binding.userPostsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userPostsRecyclerView.adapter = postAdapter

        firestore.collection("posts")
            .whereEqualTo("userId", visitedUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val post = document.toObject(Post::class.java).copy(id = document.id)
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFriendButtonStatus() {
        val friendshipDocRef = firestore.collection("friends")
            .document("${currentUserId}_${visitedUserId}")

        friendshipDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                binding.addFriendButton.text = "Friend"
                binding.addFriendButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friend, 0, 0, 0)
            } else {
                binding.addFriendButton.text = "Add friend"
                binding.addFriendButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_friend, 0, 0, 0)
            }
        }
    }

    private fun toggleFriendship() {
        val friendshipDocRef = firestore.collection("friends")
            .document("${currentUserId}_${visitedUserId}")

        friendshipDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Remove friend
                friendshipDocRef.delete().addOnSuccessListener {
                    Toast.makeText(this, "Friend removed", Toast.LENGTH_SHORT).show()
                    updateFriendButtonStatus()
                }
            } else {
                // Add friend
                val friendship = hashMapOf(
                    "userIds" to listOf(currentUserId, visitedUserId)
                )
                friendshipDocRef.set(friendship).addOnSuccessListener {
                    Toast.makeText(this, "Friend added", Toast.LENGTH_SHORT).show()
                    updateFriendButtonStatus()
                }
            }
        }
    }
}