package com.wvxid.planidentifier

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wvxid.planidentifier.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<Chat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()

        binding.newChatButton.setOnClickListener {
            startActivity(Intent(requireContext(), PeopleActivity::class.java))
        }

        listenForRecentChats()

        return binding.root
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatList)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun listenForRecentChats() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("messages")
            .whereArrayContains("participants", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val latestMessages = mutableMapOf<String, Message>()

                for (doc in snapshots!!) {
                    val message = doc.toObject(Message::class.java)
                    val otherUserId = message.participants.find { it != currentUserId } ?: continue
                    
                    // Keep only the newest message for each chat
                    if (!latestMessages.containsKey(otherUserId)) {
                        latestMessages[otherUserId] = message
                    }
                }
                
                val newChatList = latestMessages.values.map {
                    Chat(
                        otherUserId = it.participants.find { p -> p != currentUserId }!!,
                        lastMessage = it.content,
                        timestamp = it.timestamp
                    )
                }.sortedByDescending { it.timestamp }
                
                updateChats(newChatList)
            }
    }

    private fun updateChats(newChats: List<Chat>) {
        chatList.clear()
        chatList.addAll(newChats)
        
        if (chatList.isNotEmpty()) {
            fetchUserNames()
        } else {
            chatAdapter.notifyDataSetChanged() // To clear the list if no chats
        }
    }

    private fun fetchUserNames() {
        val userIds = chatList.map { it.otherUserId }
        if (userIds.isEmpty()) return

        firestore.collection("users").whereIn("id", userIds).get()
            .addOnSuccessListener { userSnapshots ->
                val usersMap = userSnapshots.documents.associate { it.id to it.getString("name") }
                
                chatList.forEach { chat ->
                    chat.otherUserName = usersMap[chat.otherUserId] ?: "Unknown User"
                }
                
                chatAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("ChatFragment", "Error fetching user names for chats")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}