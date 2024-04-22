package com.example.grocery_delivery

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.grocery_delivery.adapters.AdapterCategory
import com.example.grocery_delivery.databinding.FragmentHomeBinding
import com.example.grocery_delivery.models.Category
import java.lang.reflect.Array


class homeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        SetStatusBarColor()
        SetAllCategories()
        return binding.root
    }

    private fun SetAllCategories(){
        val categoryList = ArrayList<Category>()

        for( i in 0 until Constants.allProductsCategory.size){
            categoryList.add(Category(Constants.allProductsCategory[i] , Constants.allProductsCategoryIcon[i]))
        }

        binding.rvCategories.adapter = AdapterCategory(categoryList)
    }
    private fun SetStatusBarColor(){
        activity?.window?.apply{
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}