package com.example.grocery_delivery

interface CartListener {
    fun showCartLayout(itemCount : Int)
    fun saveSharedPref(itemCount : Int)

    fun hideCartLayout()

}