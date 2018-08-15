package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.*

@Dao
interface CarMasterDao : BaseDao<CarMaster> {

    @Query("SELECT * FROM CAR_MASTER WHERE CAR_ID = :id")
    fun findById(id: Int) : CarMaster?

    @Query("SELECT * FROM CAR_MASTER")
    fun findAll() : List<CarMaster>
}