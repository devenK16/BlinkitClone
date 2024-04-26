package com.example.grocery_delivery.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.grocery_delivery.CartListener
import com.example.grocery_delivery.R
import com.example.grocery_delivery.adapters.CartProductsAdapter
import com.example.grocery_delivery.databinding.ActivityUsersMainBinding
import com.example.grocery_delivery.databinding.BdCartProductsBinding
import com.example.grocery_delivery.roomdb.cartProducts
import com.example.grocery_delivery.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class UsersMainActivity : AppCompatActivity(), CartListener {
    private lateinit var binding: ActivityUsersMainBinding
    val viewModel: UserViewModel by viewModels()
    private lateinit var cartProducts: List<cartProducts>
    private lateinit var adapterCartProducts: CartProductsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUsersMainBinding.inflate(layoutInflater)
        getAllCartProducts()
        getTotalItemCountInCart()
        onCartClicked()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this) {
            cartProducts = it
        }
    }

    private fun onCartClicked() {
        binding.llitemCart.setOnClickListener {
            val bsCartProductsBinding = BdCartProductsBinding.inflate(LayoutInflater.from(this))
            val bs = BottomSheetDialog(this)
            bs.setContentView(bsCartProductsBinding.root)

            adapterCartProducts = CartProductsAdapter()
            bsCartProductsBinding.NumberOfProductCount.text = binding.NumberOfProductCount.text
            bsCartProductsBinding.RVcartProducts.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartProducts)
            bsCartProductsBinding.btnNext.setOnClickListener {
//                startActivity(Intent(this, OrderActiviity::class.java))
            }
            bs.show()
        }
    }

    private fun getTotalItemCountInCart() {
        viewModel.fetchTotalcartItemCount().observe(this) {
            if (it > 0) {
                binding.llcart.visibility = View.VISIBLE
                binding.NumberOfProductCount.text = it.toString()
            } else {
                binding.llcart.visibility = View.GONE
            }
        }
    }

    override fun showCartLayout(itemCount: Int) {
        val previousCount = binding.NumberOfProductCount.text.toString().toInt()
        val updatedCount = previousCount + itemCount

        if (updatedCount > 0) {
            binding.llcart.visibility = View.VISIBLE
            binding.NumberOfProductCount.text = updatedCount.toString()
        } else {
            binding.llcart.visibility = View.GONE
            binding.NumberOfProductCount.text = "0"
        }
    }

    override fun saveSharedPref(itemCount: Int) {
        viewModel.fetchTotalcartItemCount().observe(this) {
            viewModel.savingCartItemCount(it + itemCount)
        }
    }

    override fun hideCartLayout() {
        binding.llcart.visibility = View.GONE
        binding.NumberOfProductCount.text = "0"
    }
}