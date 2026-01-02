package com.wvxid.planidentifier

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var postImageView: ImageView
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val publishButton = findViewById<Button>(R.id.publish_button)
        val backButton = findViewById<Button>(R.id.back_button)
        val postContentEditText = findViewById<EditText>(R.id.post_content_edit_text)
        val galleryIcon = findViewById<ImageView>(R.id.gallery_icon)
        postImageView = findViewById(R.id.post_image_view)

        galleryIcon.setOnClickListener {
            openGallery()
        }

        publishButton.setOnClickListener {
            publishButton.isEnabled = false // Disable button to prevent multiple clicks
            uploadPost()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            postImageView.setImageURI(selectedImageUri)
            postImageView.visibility = View.VISIBLE
        }
    }

    private fun uploadPost() {
        val postContent = findViewById<EditText>(R.id.post_content_edit_text).text.toString()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to post", Toast.LENGTH_SHORT).show()
            findViewById<Button>(R.id.publish_button).isEnabled = true
            return
        }

        if (postContent.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Please write something or select an image", Toast.LENGTH_SHORT).show()
            findViewById<Button>(R.id.publish_button).isEnabled = true
            return
        }

        if (selectedImageUri != null) {
            // If there's an image, upload it first
            val imageRef = storage.reference.child("posts/${UUID.randomUUID()}")
            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { 
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        savePostToFirestore(postContent, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    findViewById<Button>(R.id.publish_button).isEnabled = true
                }
        } else {
            // If there's no image, save post directly
            savePostToFirestore(postContent, null)
        }
    }

    private fun savePostToFirestore(content: String, imageUrl: String?) {
        val currentUser = auth.currentUser!!
        val post = hashMapOf(
            "userId" to currentUser.uid,
            "content" to content,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post published successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error publishing post: ${e.message}", Toast.LENGTH_SHORT).show()
                findViewById<Button>(R.id.publish_button).isEnabled = true
            }
    }
}