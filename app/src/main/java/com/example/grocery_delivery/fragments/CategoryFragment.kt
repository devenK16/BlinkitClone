package com.example.grocery_delivery.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.grocery_delivery.CartListener
import com.example.grocery_delivery.R
import com.example.grocery_delivery.Utils
import com.example.grocery_delivery.adapters.AdapterProduct
import com.example.grocery_delivery.databinding.FragmentCategoryBinding
import com.example.grocery_delivery.databinding.ItemViewProductBinding
import com.example.grocery_delivery.models.Product
import com.example.grocery_delivery.roomdb.cartProducts
import com.example.grocery_delivery.viewmodels.UserViewModel
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [categoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class categoryFragment : Fragment() {
    private lateinit var binding: FragmentCategoryBinding
    var categoryName: String? = null
    private lateinit var adapterProduct: AdapterProduct
    private val viewModel: UserViewModel by viewModels()
    private var cartListener: CartListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(layoutInflater)
        setStatusBarColor()
        setTitle()
        onBackClicked()
        onSearchClicked()
        getCategoryProducts(categoryName)
        return binding.root
    }

    private fun onBackClicked() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }
    }

    private fun onSearchClicked() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.searchh -> {
                    findNavController().navigate(R.id.action_categoryFragment_to_searchFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun setTitle() {
        val bundle = arguments
        categoryName = bundle?.getString("categoryName")
        binding.toolbar.title = categoryName
    }

    private fun getCategoryProducts(categoryName: String?) {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getCategoryProducts(categoryName).collect {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CartListener) {
            cartListener = context
        } else {
            throw ClassCastException("Please implement cartListener")
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

    private fun saveProductInRoom(product: Product) {
        val cartProducts = cartProducts(
            productRandomId = product.productRandomId,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString() + product.productTUnit.toString(),
            productPrice = "₹" + "${product.productPrice}",
            productCount = product.itemCount,
            productStock = product.productStock,
            image = product.productImageUris?.get(0),
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            productType = product.productType
        )
        lifecycleScope.launch { viewModel.insertCartProduct(cartProducts) }
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}