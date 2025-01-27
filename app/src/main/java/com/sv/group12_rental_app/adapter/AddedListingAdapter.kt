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

class AddedListingAdapter(
    private val rentalList:MutableList<Rental>,
    private val rowClickHandler:(Int) -> Unit,
    private val detailBtnClickHandler:(Int) -> Unit) : RecyclerView.Adapter<AddedListingAdapter.RentalViewHolder>()

{
    inner class RentalViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView) {
        init {
            itemView.setOnClickListener {
                rowClickHandler(adapterPosition)
            }
            itemView.findViewById<Button>(R.id.detailBtn).setOnClickListener {
                detailBtnClickHandler(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.row_item_landlord_rental, parent, false)
        return RentalViewHolder(view)

    }


    override fun getItemCount(): Int {
        return rentalList.size
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        //Get the current rental listing
        val currRental: Rental = rentalList.get(position)

        //Populate the UI with the rental details
        //Get the price and populate it
        val price = holder.itemView.findViewById<TextView>(R.id.price)
        price.setText("$${currRental.price.toInt().toString()}")
        //Populate the UI with the fruit details
        val propertyAddress = holder.itemView.findViewById<TextView>(R.id.propertyAddress)
        propertyAddress.text = currRental.propertyAddress

        //Populate the image
        //Get the context variable
        val context = holder.itemView.context

        //Use the context to update the image
        val resId = context.resources.getIdentifier(currRental.imageFilename, "drawable", context.packageName)
        val ivRental = holder.itemView.findViewById<ImageView>(R.id.ivRental)
        //Check if resId is null, and set a default image if needed
        if(resId != null) {
            ivRental.setImageResource(resId)
        } else {
            //Set a default image
            ivRental.setImageResource(R.drawable.apartment)
        }



    }


}