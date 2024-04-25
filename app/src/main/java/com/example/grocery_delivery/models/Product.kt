package com.example.grocery_delivery.models

import com.example.grocery_delivery.Utils


data class Product(
    val productRandomId: String = Utils.getRandomUid(),
    var productTitle: String? = null,
    var productQuantity: Int? = null,
    var productTUnit: String? = null,
    var productPrice: Int? = null,
    var productStock: Int? = null,
    var productCategory: String? = null,
    var productType: String? = null,
    val itemCount: Int? = null,
    val adminUid: String? = null,
    var productImageUris: ArrayList<String?>? = null,
    val timestamp: Long = System.currentTimeMillis()
)