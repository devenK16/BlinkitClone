package com.example.grocery_delivery.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.grocery_delivery.models.Product
import com.example.grocery_delivery.roomdb.CartProductDatabase
import com.example.grocery_delivery.roomdb.cartProducts
import com.example.grocery_delivery.roomdb.cartProductsDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class UserViewModel(application: Application) : AndroidViewModel(application) {


        //initialization
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("My_Pref", MODE_PRIVATE)

    val cartProductsDao: cartProductsDao =
        CartProductDatabase.getDatabaseInstance(application).cartProductDao()

    val _paymentStatus = MutableStateFlow<Boolean>(false)
    val paymentStatus = _paymentStatus

    //Room DB
    suspend fun insertCartProduct(cartProducts: cartProducts) {
        cartProductsDao.insertCartProduct(cartProducts)
    }

    fun getAll(): LiveData<List<cartProducts>> {
        return cartProductsDao.getAllCartProducts()
    }
//
    suspend fun updateCartProduct(cartProducts: cartProducts) {
        cartProductsDao.updateCartProduct(cartProducts)
    }

    suspend fun deleteCartProduct(productId: String) {
        cartProductsDao.deleteCartProduct(productId)
    }

    suspend fun deleteAllCartProducts() {
        cartProductsDao.deleteAllCartProducts()
    }
//
    //Firebase Call
    fun getCategoryProducts(title: String?): Flow<List<Product>> = callbackFlow {
        val database =
            FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory")
                .child(title!!)

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val product = ArrayList<Product>()
                    for (products in snapshot.children) {
                        val p = products.getValue(Product::class.java)
                        product.add(p!!)
                    }
                    val sortedProducts = product.sortedByDescending { it.timestamp }
                    trySend(sortedProducts)
                } else {
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        database.addValueEventListener(eventListener)
        awaitClose { database.removeEventListener(eventListener) }

    }

    fun fetchAllProducts(): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)

                }
                val sortedProducts = products.sortedByDescending { it.timestamp }
                trySend(sortedProducts)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }
//
    fun addProductToFirebase(product: Product, itemCount: Int) {
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")
            .child(product.productRandomId).child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory")
            .child(product.productCategory!!).child(product.productRandomId).child("itemCount")
            .setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType")
            .child(product.productType!!).child(product.productRandomId).child("itemCount")
            .setValue(itemCount)

    }

    fun saveStockAfterOrdering(stock: Int, product: cartProducts) {
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")
            .child(product.productRandomId).child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory")
            .child(product.productCategory!!).child(product.productRandomId).child("itemCount")
            .setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType")
            .child(product.productType!!).child(product.productRandomId).child("itemCount")
            .setValue(0)


        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")
            .child(product.productRandomId).child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory")
            .child(product.productCategory!!).child(product.productRandomId).child("productStock")
            .setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType")
            .child(product.productType!!).child(product.productRandomId).child("productStock")
            .setValue(stock)
    }

    fun getAllOrders(): Flow<List<Orders>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders")
            .orderByChild("orderStatus")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for (orders in snapshot.children) {
                    val order = orders.getValue(Orders::class.java)
                    if (order?.orderingUserId == Utils.getCurrentUid()) {
                        orderList.add(order)
                    }
                }
                trySend(orderList)

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun getOrderedProducts(orderId: String): Flow<List<cartProducts>> = callbackFlow {
        val db =
            FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }


    //shared Preference
    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit().putInt("itemCount", itemCount).apply()
    }

    fun fetchTotalcartItemCount(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount
    }

    fun saveAddress(address: String) {
        FirebaseDatabase.getInstance().getReference("ALl Users")
            .child("Users")
            .child(Utils.getCurrentUid())
            .child("address")
            .setValue(address)
    }

    fun getUserAddress(callback: (String?) -> Unit) {
        val db = FirebaseDatabase.getInstance().getReference("ALl Users")
            .child("Users")
            .child(Utils.getCurrentUid())
            .child("address")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val address = snapshot.getValue(String::class.java)
                    callback(address)
                } else {
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    fun saveAddressStatus() {
        sharedPreferences.edit().putBoolean("address", true).apply()
    }

    fun fetchAddress(): MutableLiveData<Boolean> {
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("address", false)
        return status
    }

    //Retrofit
    suspend fun checkPaymentStatus(header: Map<String, String>) {
        val res = ApiUtilities.statusApi.checkStatus(
            header,
            Constants.MERCHAT_ID,
            Constants.merchantTransactionId
        )
        if (res.body() != null && res.body()!!.success) {
            _paymentStatus.value = true
        } else {
            _paymentStatus.value = false
        }

    }

    fun saveOrderedProducts(orders: Orders) {
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders")
            .child(orders.orderId!!).setValue(orders)
    }

}