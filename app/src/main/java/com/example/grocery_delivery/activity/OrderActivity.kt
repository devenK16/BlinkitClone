package com.example.grocery_delivery.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.grocery_delivery.R
import com.example.grocery_delivery.Utils
import com.example.grocery_delivery.adapters.CartProductsAdapter
import com.example.grocery_delivery.databinding.ActivityOrderBinding
import com.example.grocery_delivery.databinding.AddressLayoutBinding
import com.example.grocery_delivery.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class OrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderBinding
    val viewModel: UserViewModel by viewModels()
    private lateinit var adapterCartProducts: CartProductsAdapter
    private lateinit var b2BPGRequest: B2BPGRequest

    //    private lateinit var b2BPGRequest : B2BPGRequest
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setStatusBarColor()
        getAllProducts()
        onBackClicked()
//        initializePhonePe()
        onPlaceOrderClicked()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {
            viewModel.fetchAddress().observe(this) {
                if (it) {
                    //payment
                    getPaymentView()
                } else {
                    val addressLayoutBinding =
                        AddressLayoutBinding.inflate(LayoutInflater.from(this))
                    val alterDialog = AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root)
                        .create()
                    alterDialog.show()
                    addressLayoutBinding.Add.setOnClickListener {
                        saveAddress(
                            alterDialog,
                            addressLayoutBinding
                        )
                    }
                }
            }
        }
    }

    private fun saveAddress(alterDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "Processing")
        val userPinCode = addressLayoutBinding.pincode.text.toString()
        val userPhone = addressLayoutBinding.phoneNo.text.toString()
        val userState = addressLayoutBinding.state.text.toString()
        val userDistrict = addressLayoutBinding.district.text.toString()
        val userAddress = addressLayoutBinding.address.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress, $userPhone"


        lifecycleScope.launch {
            viewModel.saveAddress(address)
            viewModel.saveAddressStatus()
        }
        Utils.showToast(this, "Address Saved")
        Utils.hideDialog()
        alterDialog.dismiss()

    }

    private fun getPaymentView() {
        try {
            PhonePe.getImplicitIntent(this, b2BPGRequest, "com.phonepe.simulator")
                .let {
                    phonePeView.launch(it)
                }

        } catch (e: PhonePeInitException) {
            Utils.Toast(this, e.message.toString())
        }
    }

    private fun onBackClicked() {
        binding.toolbar2.setNavigationOnClickListener {
            startActivity(Intent(this, UsersMainActivity::class.java))
        }
    }

    private fun getAllProducts() {
        viewModel.getAll().observe(this) {
            adapterCartProducts = CartProductsAdapter()
            binding.RVCartProduct.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(it)

            var GrandTotal = 0;
            var subTotal = 0;
            for (products in it) {
                val price: String = products.productPrice!!.substring(1)
                subTotal += (price.toString().toInt() * products.productCount!!)
            }
            GrandTotal = subTotal
            if (subTotal < 200) {
                binding.deliveryCharges.text = "₹15"
                GrandTotal += 15
            }
            binding.grandTotal.text = GrandTotal.toString()
            binding.subtotal.text = subTotal.toString()
        }
    }

    private fun setStatusBarColor() {
        window?.apply {
            val statusBarColors = ContextCompat.getColor(applicationContext, R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}