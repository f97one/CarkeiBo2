package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.*

@Dao
interface CarMasterDao : BaseDao<CarMaster> {

    @Query("SELECT * FROM CAR_MASTER WHERE CAR_ID = :id")
    fun findById(id: Int) : CarMaster?

    @Query("SELECT * FROM CAR_MASTER")
    fun findAll() : List<CarMaster>

    /**
     * デフォルトフラグがあるレコードを返す。
     *
     * @return デフォルトフラグがあるレコード、ない場合はnull、複数ある場合はIDの小さいもの
     */
    @Query("SELECT * FROM CAR_MASTER WHERE DEFAULT_FLAG = 1 ORDER BY CAR_ID LIMIT 1")
    fun findByDefault(): CarMaster?

    /**
     * デフォルトフラグを一律下ろす。
     */
    @Query("UPDATE CAR_MASTER SET DEFAULT_FLAG = '0'")
    fun decreaseDefault()

    @Query("UPDATE CAR_MASTER SET DEFAULT_FLAG = '1' WHERE CAR_ID = :id")
    fun setAsDefault(id: Int)
}