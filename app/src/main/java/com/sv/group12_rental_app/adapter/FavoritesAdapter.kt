package com.sv.group12_rental_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sv.group12_rental_app.R
import com.sv.group12_rental_app.models.Rental

class FavoritesAdapter(
    private val rentalsList: MutableList<Rental>,
    private val rowClickHandler: (Int) -> Unit,
    private val removeBtnClickHandler: (Int) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    inner class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            // row
            itemView.setOnClickListener {
                rowClickHandler(adapterPosition)
            }
            // remove button
            itemView.findViewById<Button>(R.id.btnRemove).setOnClickListener {
                removeBtnClickHandler(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.row_item_favourites, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rentalsList.size
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val currRental: Rental = rentalsList[position]

        //price
        val tvTitle = holder.itemView.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "$${currRental.price}"

        //address
        val tvDetail = holder.itemView.findViewById<TextView>(R.id.tvDetail)
        tvDetail.text = currRental.propertyAddress

        // image
        val context = holder.itemView.context
        // Use the context to update the image
        val resId = context.resources.getIdentifier(currRental.imageFilename, "drawable", context.packageName)
        val ivRental = holder.itemView.findViewById<ImageView>(R.id.ivRental)
        // Check if resId is null, and set a default image if needed
        if (resId != null) {
            ivRental.setImageResource(resId)
        } else {
            // Set a default image
            ivRental.setImageResource(R.drawable.apartment)
        }
    }
}