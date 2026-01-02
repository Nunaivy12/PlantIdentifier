package com.wvxid.planidentifier

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messagesRecyclerView: RecyclerView
    private var receiverId: String? = null
    private var currentUserId: String? = null
    private val messageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserId = auth.currentUser?.uid
        receiverId = intent.getStringExtra("receiver_id")
        val receiverName = intent.getStringExtra("receiver_name")

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = receiverName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        if (currentUserId == null || receiverId == null) {
            Toast.makeText(this, "User information is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        messagesRecyclerView = findViewById(R.id.messages_recycler_view)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        // Corrected the constructor call
        messageAdapter = MessageAdapter(messageList)
        messagesRecyclerView.adapter = messageAdapter

        listenForMessages()

        findViewById<Button>(R.id.send_button).setOnClickListener {
            val content = findViewById<EditText>(R.id.message_edit_text).text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(content)
            }
        }
    }

    private fun sendMessage(content: String) {
        val message = Message(
            senderId = currentUserId!!,
            receiverId = receiverId!!,
            content = content,
            participants = listOf(currentUserId!!, receiverId!!)
        )

        findViewById<EditText>(R.id.message_edit_text).text.clear()

        firestore.collection("messages").add(message)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForMessages() {
        firestore.collection("messages")
            .whereArrayContains("participants", currentUserId!!)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val newMessages = snapshots?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: listOf()
                
                val chatMessages = newMessages.filter {
                    (it.senderId == currentUserId && it.receiverId == receiverId) || (it.senderId == receiverId && it.receiverId == currentUserId)
                }

                messageList.clear()
                messageList.addAll(chatMessages)
                messageAdapter.notifyDataSetChanged()
                messagesRecyclerView.scrollToPosition(messageList.size - 1)
            }
    }
}