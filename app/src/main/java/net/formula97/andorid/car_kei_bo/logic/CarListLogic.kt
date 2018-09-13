package net.formula97.andorid.car_kei_bo.logic

import android.arch.persistence.room.Transaction
import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.repository.AppDatabase

class CarListLogic(appDatabase: AppDatabase) : BaseAppLogic(appDatabase) {

    /**
     * クルマのリストを取得する。
     *
     * @param orderByDesc 降順でソートする場合true、そうでない場合false
     * @return 現在のクルマのリスト、レコードがない場合は空のCollection
     */
    fun getCarList(orderByDesc : Boolean = false) : List<CarMaster> {
        val items = appDatabase.carMasterDao().findAll()

        return if (orderByDesc) {
            items.sortedWith(Comparator { o1, o2 -> o2.carId.compareTo(o1.carId) })
        } else {
            items.sortedWith(Comparator { o1, o2 -> o1.carId.compareTo(o2.carId) })
        }
    }

    /**
     * デフォルトフラグがあるクルマを取得する。
     *
     * @return デフォルトフラグのあるCarMaster、見つからないときはnull、デフォルトフラグが複数ある場合はIDが小さいほう
     */
    fun findForDefault(): CarMaster? {
        return appDatabase.carMasterDao().findByDefault()
    }

    /**
     * デフォルトフラグを変更する。
     *
     * @param id デフォルトフラグを立てるクルマのID
     */
    @Transaction
    fun changeDefault(id : Int) {
        // いったん全部フラグを下ろし、指定番号に対してフラグを立てる
        val carMasterDao = appDatabase.carMasterDao()

        val targetItem = carMasterDao.findById(id)
        if (targetItem == null) {
            throw IllegalArgumentException("Can't find record by specified CAR_ID = $id")
        } else {
            carMasterDao.decreaseDefault()
            targetItem.defaultFlag = true
            carMasterDao.updateItem(targetItem)
        }
    }

    /**
     * クルマの記録を消去する。
     *
     * @param id 消去対象のクルマのID
     */
    @Transaction
    fun deleteCarMileage(id: Int) {
        appDatabase.costsMasterDao().deleteCostsByCar(id)
        appDatabase.lubMasterDao().deleteLubByCar(id)

        val carMasterDao = appDatabase.carMasterDao()
        val carMaster = carMasterDao.findById(id)
        if (carMaster != null) {
            carMasterDao.deleteItem(carMaster)
        }
    }

    @Transaction
    fun addNewCar(entity: CarMaster) {
        // TODO コルーチンで処理を書く

        val dao = appDatabase.carMasterDao()

        if (entity.defaultFlag) {
            dao.decreaseDefault()
        }
        dao.addItem(entity)

    }
}