package com.example.grocery_delivery.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartProducts")
data class cartProducts(
    @PrimaryKey
    val productRandomId:String= "random",
    var productTitle:String?=null,
    var productQuantity:String?=null,
    var productPrice:String?=null,
    val productCount:Int?=null,
    var productStock:Int?=null,
    var image:String?=null,
    var productCategory:String?=null,
    val adminUid:String?=null,
    var productType:String?=null,
)