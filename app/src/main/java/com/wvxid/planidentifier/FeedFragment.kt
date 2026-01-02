package com.wvxid.planidentifier

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var postAdapter: PostAdapter
    private lateinit var postsRecyclerView: RecyclerView
    private val postList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)
        firestore = FirebaseFirestore.getInstance()

        postsRecyclerView = view.findViewById(R.id.posts_recycler_view)
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList)
        postsRecyclerView.adapter = postAdapter

        listenForPosts()

        return view
    }

    private fun listenForPosts() {
        firestore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("FeedFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                postList.clear()
                for (doc in snapshots!!) {
                    val post = doc.toObject(Post::class.java).copy(id = doc.id) // Assuming Post has an id field
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()
            }
    }
}