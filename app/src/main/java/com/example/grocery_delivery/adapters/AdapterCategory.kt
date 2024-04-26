package com.example.grocery_delivery.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.grocery_delivery.databinding.ItemViewProductCategoryBinding
import com.example.grocery_delivery.models.Category

class AdapterCategory(
    val categoryList: ArrayList<Category>,
    val onCategoryClicked: (Category) -> Unit
) : RecyclerView.Adapter<AdapterCategory.CategoryViewHolder>() {
    class CategoryViewHolder(val binding: ItemViewProductCategoryBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemViewProductCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryList = categoryList[position]
        holder.binding.apply {
            CategoryImage.setImageResource(categoryList.image)
            CategoryTitle.text = categoryList.title
        }
        holder.itemView.setOnClickListener {
            onCategoryClicked(categoryList)
        }
    }
}