package com.example.grocery_delivery.models

import com.example.grocery_delivery.roomdb.cartProducts

data class Orders(
    val orderId:String?=null,
    val orderList:List<cartProducts>?=null,
    val userAddress:String?=null,
    val orderStatus:Int=0,
    val orderDate:String?=null,
    val orderingUserId:String?=null
)