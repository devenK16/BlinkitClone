package com.example.grocery_delivery.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.grocery_delivery.CartListener
import com.example.grocery_delivery.Constants
import com.example.grocery_delivery.R
import com.example.grocery_delivery.Utils
import com.example.grocery_delivery.adapters.AdapterCategory
import com.example.grocery_delivery.adapters.AdapterProduct
import com.example.grocery_delivery.databinding.FragmentHomeBinding
import com.example.grocery_delivery.databinding.FragmentSearchBinding
import com.example.grocery_delivery.databinding.ItemViewProductBinding
import com.example.grocery_delivery.models.Category
import com.example.grocery_delivery.models.Product
import com.example.grocery_delivery.roomdb.cartProducts
import com.example.grocery_delivery.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class searchFragment : Fragment() {
    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapterProduct : AdapterProduct
    private var cartListener: CartListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater)
        SetStatusBarColor()
        getAllProducts()
        onBackClicked()
        searchFunction()
        return binding.root
    }
    private fun onBackClicked() {
        binding.goBackHome.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }
    }

    private fun searchFunction() {
        binding.searchRv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query=s.toString().trim()
                adapterProduct.filter.filter(query)
            }

            override fun afterTextChanged(s: Editable?) {}

        })
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

    private fun getAllProducts() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchAllProducts().collect {
                if (it.isEmpty()) {
                    binding.textView2.visibility = View.VISIBLE
                    binding.productsRv.visibility = View.GONE
                } else {
                    binding.textView2.visibility = View.GONE
                    binding.productsRv.visibility = View.VISIBLE
                }
                adapterProduct = AdapterProduct(
                    ::OnAddBtnClicked,
                    ::onIncrementBtnCLicked,
                    ::onDecrementBtnCLicked
                )
                binding.productsRv.adapter = adapterProduct
                adapterProduct.differ.submitList(it)
                adapterProduct.originalList = ArrayList(it)
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
    }

    fun OnAddBtnClicked(product: Product, productsBinding: ItemViewProductBinding) {
        productsBinding.add.visibility = View.GONE
        productsBinding.productCount.visibility = View.VISIBLE

        //step 1
        var itemCount = productsBinding.count.text.toString().toInt()
        itemCount++
        productsBinding.count.text = itemCount.toString()

        cartListener?.showCartLayout(1)

        //step 2
        product.itemCount = itemCount
        lifecycleScope.launch {
            cartListener?.saveSharedPref(1)
            saveProductInRoom(product)
            viewModel.addProductToFirebase(product, itemCount)
        }
    }

    private fun onIncrementBtnCLicked(product: Product, productsBinding: ItemViewProductBinding) {
        var itemCountInc = productsBinding.count.text.toString().toInt()
        if (product.productStock!! > itemCountInc) {
            itemCountInc++
            productsBinding.count.text = itemCountInc.toString()

            cartListener?.showCartLayout(1)
            //stem 2
            product.itemCount = itemCountInc
            lifecycleScope.launch {
                cartListener?.saveSharedPref(1)
                saveProductInRoom(product)
                viewModel.addProductToFirebase(product, itemCountInc)
            }
        } else {
            Utils.showToast(requireContext(), "Cannot added more of this")
        }


    }

    private fun onDecrementBtnCLicked(product: Product, productsBinding: ItemViewProductBinding) {
        var itemCountDec = productsBinding.count.text.toString().toInt()
        itemCountDec--

        //step 2
        product.itemCount = itemCountDec
        lifecycleScope.launch {
            cartListener?.saveSharedPref(-1)
            saveProductInRoom(product)
            viewModel.addProductToFirebase(product, itemCountDec)
        }

        if (itemCountDec > 0) {
            productsBinding.count.text = itemCountDec.toString()
        } else {
            lifecycleScope.launch {
                viewModel.deleteCartProduct(product.productRandomId)
            }
            productsBinding.add.visibility = View.VISIBLE
            productsBinding.productCount.visibility = View.GONE
            productsBinding.count.text = "0"
        }

        cartListener?.showCartLayout(-1)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CartListener) {
            cartListener = context
        } else {
            throw ClassCastException("Please implement cartListener")
        }
    }


    private  fun saveProductInRoom(product: Product) {
        val cartProducts= cartProducts(
            productRandomId = product.productRandomId,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString()+product.productTUnit.toString(),
            productPrice = "₹"+"${product.productPrice}",
            productCount = product.itemCount,
            productStock = product.productStock,
            image = product.productImageUris?.get(0),
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            productType = product.productType
        )
        lifecycleScope.launch { viewModel.insertCartProduct(cartProducts)}
    }


}

