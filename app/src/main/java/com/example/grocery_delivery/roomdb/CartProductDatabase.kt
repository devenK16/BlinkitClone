package com.example.grocery_delivery.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [cartProducts::class], version = 1, exportSchema = false)
abstract class CartProductDatabase : RoomDatabase() {

    abstract fun cartProductDao(): cartProductsDao

    companion object {
        @Volatile
        var INSANCE: CartProductDatabase? = null

        fun getDatabaseInstance(context: Context): CartProductDatabase {
            val tempInstance = INSANCE
            if (tempInstance != null) return tempInstance
            else {
                synchronized(this) {
                    val roomdb = Room.databaseBuilder(
                        context,
                        CartProductDatabase::class.java,
                        "CartProducts"
                    )
                        .allowMainThreadQueries()
                        .build()
                    INSANCE = roomdb
                    return roomdb
                }
            }
        }
    }
}