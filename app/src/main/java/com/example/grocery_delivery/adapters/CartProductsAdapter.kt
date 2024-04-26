package com.example.grocery_delivery.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grocery_delivery.databinding.SampleCartProductsBinding
import com.example.grocery_delivery.roomdb.cartProducts

class CartProductsAdapter : RecyclerView.Adapter<CartProductsAdapter.Viewholder>() {
    class Viewholder(val binding: SampleCartProductsBinding) :
        RecyclerView.ViewHolder(binding.root) {}


    val diffUtil = object : DiffUtil.ItemCallback<cartProducts>() {
        override fun areItemsTheSame(oldItem: cartProducts, newItem: cartProducts): Boolean {
            return oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(oldItem: cartProducts, newItem: cartProducts): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        return Viewholder(
            SampleCartProductsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val product = differ.currentList[position]
        holder.binding.apply {
            Glide.with(holder.itemView).load(product.image).into(productImg)
            productName.text = product.productTitle
            Quantity.text = product.productQuantity
            price.text = product.productPrice
            itemCount.text = product.productCount.toString()
        }
    }
}