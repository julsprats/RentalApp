package com.sv.group12_rental_app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.sv.group12_rental_app.R
import com.sv.group12_rental_app.models.Rental

class RentalPropertyAdapter(
    private val rentalsList: MutableList<Rental>,
    private val rowClickHandler:(Int) -> Unit,
    private val favBtnClickHandler: (Int) -> Unit
) : RecyclerView.Adapter<RentalPropertyAdapter.RentalPropertyViewHolder>() {

    private var userFavorites: List<String> = emptyList()

    fun setUserFavorites(favourites: List<String>) {
        this.userFavorites = favourites
        notifyDataSetChanged()
    }

    fun updateList(newList: MutableList<Rental>) {
        rentalsList.clear()
        rentalsList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class RentalPropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            // row
            itemView.setOnClickListener {
                rowClickHandler(adapterPosition)
            }
            // add to fav
            val btnFavourite = itemView.findViewById<AppCompatImageButton>(R.id.btnFavourite)
            btnFavourite.setOnClickListener {
                Log.d("RentalPropertyAdapter", "Favourites button clicked at position $adapterPosition")
                favBtnClickHandler(adapterPosition)
            }
        }

        fun updateHeartIcon(isFavorite: Boolean) {
            // Get the heart icon ImageView from your item layout
            val heartIcon = itemView.findViewById<ImageView>(R.id.btnFavourite)

            // Update the heart icon based on the favorite status
            if (isFavorite) {
                // Set the heart icon to the filled heart drawable
                heartIcon.setImageResource(R.drawable.favheartfilled)
            } else {
                // Set the heart icon to the empty heart drawable
                heartIcon.setImageResource(R.drawable.favheart)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalPropertyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.row_item_rental, parent, false)
        return RentalPropertyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rentalsList.size
    }

    override fun onBindViewHolder(holder: RentalPropertyViewHolder, position: Int) {
        val currProperty: Rental = rentalsList[position]

        // price
        val tvTitle = holder.itemView.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "$${currProperty.price}"

        // address
        val tvDetail = holder.itemView.findViewById<TextView>(R.id.tvDetail)
        tvDetail.text = currProperty.propertyAddress

        // image
        val context = holder.itemView.context
        // Use the context to update the image
        val resId = context.resources.getIdentifier(currProperty.imageFilename, "drawable", context.packageName)
        val ivRental = holder.itemView.findViewById<ImageView>(R.id.ivRental)
        // Check if resId is null, and set a default image if needed
        if (resId != null) {
            ivRental.setImageResource(resId)
        } else {
            // Set a default image
            ivRental.setImageResource(R.drawable.apartment)
        }

        // Check if the property is in favorites
        val isFavorite = userFavorites.contains(currProperty.propertyAddress)

        // Update the UI based on the favorite status
        val btnFavourite = holder.itemView.findViewById<AppCompatImageButton>(R.id.btnFavourite)
        if (isFavorite) {
            // Set the heart icon to the filled heart drawable
            btnFavourite.setImageResource(R.drawable.favheartfilled)
        } else {
            // Set the heart icon to the empty heart drawable
            btnFavourite.setImageResource(R.drawable.favheart)
        }
    }
}