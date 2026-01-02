package com.wvxid.planidentifier

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount() = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private val postContentTextView: TextView = itemView.findViewById(R.id.post_content_text_view)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)
        private val postImageView: ImageView = itemView.findViewById(R.id.post_image_view)

        fun bind(post: Post) {
            postContentTextView.text = post.content
            timestampTextView.text = getFormattedTimestamp(post.timestamp)

            // Fetch user data from Firestore
            val db = FirebaseFirestore.getInstance()
            if (post.userId.isNotEmpty()) {
                db.collection("users").document(post.userId).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            usernameTextView.text = document.getString("name") ?: "Unknown User"
                        }
                    }
            }

            // Load image from URL using Glide
            if (post.imageUri != null) {
                postImageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.imageUri)
                    .into(postImageView)
            } else {
                postImageView.visibility = View.GONE
            }

            usernameTextView.setOnClickListener {
                if (post.userId.isNotEmpty()) {
                    val context = itemView.context
                    val intent = Intent(context, UserProfileActivity::class.java)
                    intent.putExtra("user_id", post.userId)
                    context.startActivity(intent)
                }
            }
        }

        private fun getFormattedTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)

            return if (minutes < 60) {
                "$minutes minutes ago"
            } else {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                if (hours < 24) "$hours hours ago" else "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
            }
        }
    }
}