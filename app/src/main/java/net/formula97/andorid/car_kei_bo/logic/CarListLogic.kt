package net.formula97.andorid.car_kei_bo.logic

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
}