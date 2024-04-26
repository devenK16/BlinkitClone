package com.example.grocery_delivery.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.grocery_delivery.FilteringProducts
import com.example.grocery_delivery.databinding.ItemViewProductBinding
import com.example.grocery_delivery.models.Product

class AdapterProduct(
    val OnAddBtnClicked: (Product, ItemViewProductBinding) -> Unit,
    val onIncrementBtnCLicked: (Product, ItemViewProductBinding) -> Unit,
    val onDecrementBtnCLicked: (Product, ItemViewProductBinding) -> Unit
) :
    RecyclerView.Adapter<AdapterProduct.viewHolder>(),
    Filterable {
    class viewHolder(val binding: ItemViewProductBinding) : RecyclerView.ViewHolder(binding.root) {}

    val difutil = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, difutil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        return viewHolder(
            ItemViewProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.binding.apply {

            val imageList = ArrayList<SlideModel>()
            val productImage = product.productImageUris
            for (i in 0 until productImage!!.size) {
                imageList.add(SlideModel(productImage[i].toString()))
            }
            imageSlider.setImageList(imageList)
            tvProductTitle.text = product.productTitle
            val quantity = product.productQuantity.toString() + product.productTUnit
            tvProductQuantity.text = quantity
            tvProductPrice.text = "₹" + product.productPrice

            if (product.itemCount!! > 0) {
                add.visibility = View.GONE
                productCount.visibility = View.VISIBLE
                count.text = product.itemCount.toString()
            }

            add.setOnClickListener {
                OnAddBtnClicked(product, this)
            }
            increment.setOnClickListener {
                onIncrementBtnCLicked(product, this)
            }
            decrement.setOnClickListener {
                onDecrementBtnCLicked(product, this)
            }
        }


    }

    private val filter: FilteringProducts? = null
    var originalList = ArrayList<Product>()
    override fun getFilter(): Filter {
        if (filter == null) return FilteringProducts(this, originalList)
        return filter
    }
}