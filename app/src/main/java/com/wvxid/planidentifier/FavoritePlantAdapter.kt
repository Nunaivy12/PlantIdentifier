package com.wvxid.planidentifier

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class FavoritePlantAdapter(private val plants: List<Plant>) : RecyclerView.Adapter<FavoritePlantAdapter.FavoritePlantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritePlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_plant, parent, false)
        return FavoritePlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritePlantViewHolder, position: Int) {
        val plant = plants[position]
        holder.bind(plant)
    }

    override fun getItemCount() = plants.size

    inner class FavoritePlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val plantImageView: ImageView = itemView.findViewById(R.id.plant_image_view)

        fun bind(plant: Plant) {
            val context = itemView.context
            val imageResId = context.resources.getIdentifier(plant.imageName, "drawable", context.packageName)
            plantImageView.setImageResource(imageResId)

            itemView.setOnClickListener {
                val intent = Intent(context, PlantDetailActivity::class.java)
                intent.putExtra("plant_id", plant.id)
                context.startActivity(intent)
            }
        }
    }
}