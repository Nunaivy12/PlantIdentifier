package com.wvxid.planidentifier

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PlantAdapter(
    private val plants: List<Plant>,
    private val onPlantClick: (Plant) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        // Corrected to use the existing layout file
        val view = LayoutInflater.from(parent.context).inflate(R.layout.species_grid_item, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(plants[position])
    }

    override fun getItemCount() = plants.size

    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Corrected the IDs to match species_grid_item.xml
        private val plantImageView: ImageView = itemView.findViewById(R.id.plant_image)
        private val plantNameTextView: TextView = itemView.findViewById(R.id.plant_name)

        fun bind(plant: Plant) {
            plantNameTextView.text = plant.commonName
            
            val imageResId = itemView.context.resources.getIdentifier(plant.imageName, "drawable", itemView.context.packageName)
            if (imageResId != 0) {
                Glide.with(itemView.context).load(imageResId).into(plantImageView)
            } else {
                // You can set a placeholder image here if needed
            }

            itemView.setOnClickListener { onPlantClick(plant) }
        }
    }
}