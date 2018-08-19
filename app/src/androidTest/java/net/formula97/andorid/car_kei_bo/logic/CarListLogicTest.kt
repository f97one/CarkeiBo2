package net.formula97.andorid.car_kei_bo.logic

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.repository.AppDatabase
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarListLogicTest {

    private var appDb: AppDatabase? = null

    private val fixture1 = CarMaster(
            carId = 1, carName = "Car1", currentFuelMileage = 0.0, currentRunningCost = 0.0,
            defaultFlag = false, distanceUnit = "km", fuelMileageLabel = "km", priceUnit = "円",
            runningCostLabel = "円", volumeUnit = "l"
    )
    private val fixture2 = CarMaster(
            carId = 3, carName = "Car2", currentFuelMileage = 35.5, currentRunningCost = 11.3,
            defaultFlag = true, distanceUnit = "ML", fuelMileageLabel = "ML", priceUnit = "USD.",
            runningCostLabel = "USD.", volumeUnit = "Gal."
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        appDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        if (appDb != null) {
            // Fixtureを投入
            val carMasterDao = appDb!!.carMasterDao()

            carMasterDao.addItem(fixture1)
            carMasterDao.addItem(fixture2)
        }
    }

    @After
    fun tearDown() {
        val carMasterDao = appDb?.carMasterDao()

        if (carMasterDao != null) {
            val items: List<CarMaster> = carMasterDao.findAll() ?: ArrayList()

            for (i in items) {
                carMasterDao.deleteItem(i)
            }
        }
    }

    /**
     * データがないとき空のクルマリストを取得できる
     */
    @Test
    fun testN0001() {
        val carMasterDao = appDb?.carMasterDao()

        if (carMasterDao != null) {
            // Fixtureをけす
            val fixtures = carMasterDao.findAll()
            for (f in fixtures) {
                carMasterDao.deleteItem(f)
            }

            val logic = CarListLogic(appDb!!)

            val items = logic.getCarList()

            assertThat("nullではない", items, `is`(notNullValue()))
            assertThat("サイズは0", items.size, `is`(0))
        }
    }

    /**
     * データがあるときにクルマリストが昇順で取得できる
     */
    @Test
    fun testN0002() {
        val logic = CarListLogic(appDb!!)

        val items = logic.getCarList()

        assertThat("nullではない", items, `is`(notNullValue()))
        assertThat("サイズは2", items.size, `is`(2))

        val i1 = items[0]
        assertTrue("一つ目のFixtureとひとしい", i1 == fixture1)
        val i2 = items[1]
        assertTrue("二つ目のFixtureにひとしい", i2 == fixture2)
    }

    /**
     * データがあるときにクルマリストが降順で取得できる
     */
    @Test
    fun testN0003() {
        val logic = CarListLogic(appDb!!)

        val items = logic.getCarList(true)

        assertThat("nullではない", items, `is`(notNullValue()))
        assertThat("サイズは2", items.size, `is`(2))

        val i1 = items[0]
        assertTrue("二つ目のFixtureとひとしい", i1 == fixture2)
        val i2 = items[1]
        assertTrue("一つ目のFixtureにひとしい", i2 == fixture1)
    }
}