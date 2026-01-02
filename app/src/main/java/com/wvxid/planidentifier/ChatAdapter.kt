package com.wvxid.planidentifier

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(private val chatList: List<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount() = chatList.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.last_message_text_view)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)

        fun bind(chat: Chat) {
            usernameTextView.text = chat.otherUserName
            lastMessageTextView.text = chat.lastMessage
            
            chat.timestamp?.let {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                timestampTextView.text = sdf.format(it)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("receiver_id", chat.otherUserId)
                    putExtra("receiver_name", chat.otherUserName)
                }
                context.startActivity(intent)
            }
        }
    }
}