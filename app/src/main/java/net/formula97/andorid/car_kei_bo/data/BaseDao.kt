package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Update

interface BaseDao<T> {

    @Insert
    fun addItem(vararg entity: T)

    @Update
    fun updateItem(vararg entity: T)

    @Delete
    fun deleteItem(vararg entity: T)
}