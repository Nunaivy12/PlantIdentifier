package com.wvxid.planidentifier

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Simplified data class for items from Firestore
data class HistoryItem(val plantId: String = "", val imageUrl: String = "", val timestamp: Long = 0)

class HistoryAdapter(private val historyItems: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyItems[position])
    }

    override fun getItemCount() = historyItems.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val historyImageView: ImageView = itemView.findViewById(R.id.history_image_view)
        private val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)

        fun bind(item: HistoryItem) {
            // Load image from Firebase Storage URL
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .centerCrop()
                .into(historyImageView)

            // Format and display date
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateTextView.text = sdf.format(Date(item.timestamp))

            // Set click listener to open detail activity
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, HistoryDetailActivity::class.java).apply {
                    putExtra("plant_id", item.plantId)
                    putExtra("image_url", item.imageUrl) // Pass the actual image URL
                }
                context.startActivity(intent)
            }
        }
    }
}