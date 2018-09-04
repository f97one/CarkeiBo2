/**
 *
 */
package net.formula97.andorid.car_kei_bo

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import net.formula97.andorid.car_kei_bo.activity.AbstractAppActivity
import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.logic.CarListLogic
import net.formula97.andorid.car_kei_bo.view.adapter.CarListAdapter

/**
 * クルマリストを表示するActivity
 * @author kazutoshi
 */
/**
 * 明示的コンストラクタ
 * Activityの場合、onCreate()がコンストラクタの役割を果たすので、
 * 特に処理を書かなくても成立する。
 */
class CarListActivity : AbstractAppActivity(), OnClickListener {

    private val dbman = DbManager(this)

    internal var cCarList: Cursor? = null
    internal var selectedRow: Cursor? = null

    // ウィジェットを扱うための定義
    internal var tv_label_value_defaultcar: TextView? =  null
    internal var TableLayout1: TableLayout? = null
    internal var listView_CarList: ListView? = null
    internal var button_addFuelRecord: Button? = null

    // DBから取得したデフォルト値を格納する変数
    private var defaultCarID: Int = 0
    private var defaultCarName: String? = null

    // ListViewのカレント値を格納する変数
    private var currentCarID: Int = 0
    private var currentCarName: String? = null

    private var externalFile: String? = null

    private var defaultCar: CarMaster? = null

    private val sDefaultCarInstanceState = this::class.java.canonicalName + ".sDefaultCarInstanceState"

    /**
     * SDカードとやりとりするためのファイル名を取得する。
     * @return String型、externalFileフィールドの値を返す
     */
    internal fun getExternalFile(): String? {
        Log.d("getExternalFile", "returned external file name = " + externalFile!!)
        return externalFile
    }

    /**
     * SDカードとやりとりするためのファイル名をセットする。
     * @param externalFile String型、externalFileフィールドにセットする値
     */
    internal fun setExternalFile(externalFile: String) {
        this.externalFile = externalFile
        Log.d("setExternalFile", "set external file name = " + getExternalFile()!!)
    }

    /**
     * Activity初期化処理、ここではウィジェット類のIDを取得している。
     * @param savedInstanceState Bundle型、インスタンスを取得したときの状態
     * @see android.app.Activity.onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.carlist)

        // ウィジェット
        //   プログラムから扱うための定数を検索してセット
        tv_label_value_defaultcar = findViewById<View>(R.id.tv_label_value_defaultcar) as TextView
        button_addFuelRecord = findViewById<View>(R.id.button_addFuelRecord) as Button
        listView_CarList = findViewById<View>(R.id.listView_CarList) as ListView

        // 燃費記録追加画面を呼び出す
        button_addFuelRecord!!.setOnClickListener {
            addMileage(defaultCarID, defaultCarName)
        }

        val carListLogic = CarListLogic(getAppDb())
        defaultCar = carListLogic.findForDefault()
    }

    /**
     * Menuキーを押した段階で呼び出される処理。
     * @param menu 項目を配置したメニューを表示
     * @return trueにするとメニューを表示、falseだと表示しない
     * @see android.app.Activity.onCreateOptionsMenu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        // res/menu/menu.xmlの記述に従い、メニューを展開する
        menuInflater.inflate(R.menu.optionsmenu, menu)
        return true
    }

    /**
     * [Menu]キーを押したとき、システムから呼ばれる。
     * XMLで定義しているメニューに応じた処理を行わせる。
     * @param item MenuItem型、選択されたメニューアイテムを格納
     * @return boolean型、trueにするとアイテム有効、falseは無効
     * @see android.app.Activity.onOptionsItemSelected
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /*
		 * switch文でそれぞれのメニューに対するアクションへ分岐する。
		 * メニュー項目を増やしたら、アクションを追加すること。
		 * ....て、別クラスにすれば修正箇所を集約できると思うが、できんのか？
		 */

        // 別画面呼び出しのためのインテント宣言
        val configActivity = Intent(this, ConfigActivity::class.java) // 設定画面
        val addCarActivity = Intent(this, AddMyCarActivity::class.java) // 「クルマを追加」画面
        val carListActivity = Intent(this, CarListActivity::class.java) // 「クルマリスト」画面

        when (item.itemId) {
            R.id.optionsmenu_closeAPP -> {
                // アプリを終了させる
                finish()
                return true
            }
            R.id.optionsmenu_call_preference -> {
                // 設定画面を呼び出す
                startActivity(configActivity)
                return true
            }
            R.id.optionsmenu_addcar -> {
                // 「クルマを追加」画面を呼び出す
                startActivity(addCarActivity)
                return true
            }
            R.id.optionsmenu_carlist ->
                // 「クルマリスト」画面を呼び出す
                //   CarListはこのクラスなので、呼び出しは行わずtrueのみ返す
                //startActivity(carListActivity);
                return true
            R.id.export_sd -> {
                // SDカードエクスポートのダイアログを表示する
                createExportMenu()
                return true
            }
            R.id.import_sd -> {
                // SDカードインポートのダイアログを表示する
                createImportMenu()
                return true
            }
            else -> return false
        }
    }

    /**
     * ActivityがほかのActivityに遷移するとき、システムから呼ばれる。
     * DBとCursorが開いていたら閉じる。
     * @see android.app.Activity.onPause
     */
    override fun onPause() {
        super.onPause()

        // CursorとDBが閉じていなければそれぞれを閉じる
        closeDbAndCursorIfOpen()
    }

    /**
     * Activityが破棄されるとき、システムから呼ばれる。
     * DBとCursorが開いていたら閉じる。
     * @see android.app.Activity.onDestroy
     */
    override fun onDestroy() {
        super.onDestroy()

        // CursorとDBが閉じていなければそれぞれを閉じる
        closeDbAndCursorIfOpen()
    }

    /**
     * Activityの初期化後、システムから呼ばれる。
     * 最終的な画面描画の調整と、クルマリストの表示を行うのに使用。
     * @see android.app.Activity.onResume
     */
    override fun onResume() {
        super.onResume()

        val carListLogic = CarListLogic(getAppDb())

        val carList = carListLogic.getCarList()
        if (carList.size > 0) {

            // ListViewにデータを貼る
            val carListAdapter = CarListAdapter(this, R.layout.listviewelement_carlist, carList)
            val lv : ListView = findViewById(R.id.listView_CarList)
            lv.adapter = carListAdapter

            registerForContextMenu(lv)

            lv.setOnItemClickListener { parent, view, position, id ->
                val cla = lv.adapter as CarListAdapter
                val posItem = cla.getItem(position)

                showMileageList(posItem.carId, posItem.carName)
            }

            if (defaultCar != null) {
                tv_label_value_defaultcar!!.text = defaultCar!!.carName
            }
        }
//
//
//
//
//        // 参照専用でDBを開く
//        db = dbman.readableDatabase
//
//        // クルマリストとデフォルトカーの表示処理
//        // クルマリストのArrayをつくる
//        //   count()の結果が1レコード以上ないとAdapterが作成できないので、CAR_MASTERに１レコード以上あるかを調べ、
//        //   あった場合のみAdapterをつくる
//        if (dbman.hasCarRecords(db)) {
//            // Adapterのもととなるレコードの取得
//            cCarList = dbman.getCarList(db)
//            Log.i("CAR_MASTER",
//                    "Got " + cCarList!!.count.toString() + " records, including "
//                            + cCarList!!.columnCount.toString() + " columns.")
//            for (i in 0 until cCarList!!.columnCount) {
//                Log.i("CAR_MASTER", "name of Column Index " + i.toString() + ":" + cCarList!!.getColumnName(i))
//            }
//
//            // AdapterからListViewへ差し込むデータの整形
//            val from = arrayOf("CAR_NAME", "CURRENT_FUEL_MILEAGE", "FUELMILEAGE_LABEL", "CURRENT_RUNNING_COST", "RUNNINGCOST_LABEL")
//            val to = intArrayOf(R.id.tv_element_CarName, R.id.tv_value_FuelMileage, R.id.tv_unit_fuelMileage, R.id.tv_value_RunningCosts, R.id.tv_unit_runningCosts)
//
//            val sca = SimpleCursorAdapter(applicationContext,
//                    R.layout.listviewelement_carlist, cCarList, from, to)
//            listView_CarList.adapter = sca
//
//            // コンテキストメニュー表示を車クルマリストに対して登録をする
//            registerForContextMenu(listView_CarList)
//
//            // 別画面呼び出し用に、デフォルト値を格納する
//            defaultCarID = dbman.getDefaultCarId(db)
//            defaultCarName = dbman.getDefaultCarName(db)
//
//            // デフォルトカーの名前を取得してセット
//            tv_label_value_defaultcar.text = defaultCarName
//
//            // イベントリスナ（onItemClick）
//            listView_CarList.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
//                // とりあえず、LogCatに流して挙動を観察
//                Log.d("onItemClick", "ListView item pressed.")
//                Log.d("onItemClick", "parent = " + parent.toString())
//                Log.d("onItemClick", "v = " + v.toString())
//                Log.d("onItemClick", "position = " + position.toString())
//                Log.d("onItemClick", "id = " + id.toString())
//
//                // 呼び出されたListViewの要素位置を取得する
//                selectedRow = listView_CarList.getItemAtPosition(position) as Cursor
//
//                // カレント値を変数に格納
//                currentCarID = selectedRow!!.getInt(selectedRow!!.getColumnIndex("_id"))
//                currentCarName = selectedRow!!.getString(selectedRow!!.getColumnIndex("CAR_NAME"))
//
//                selectedRow!!.close()
//
//                // クルマの燃費記録一覧画面を呼び出す
//                showMileageList(currentCarID, currentCarName)
//            }
//
//        }
    }

    /**
     * 「燃費記録を追加」ボタンを押すことで、デフォルトカーに燃費記録を追加する。
     * @param v View型、クリックされたView
     * @see android.view.View.OnClickListener.onClick
     */
    //@Override
    override fun onClick(v: View) {
        // デフォルトカーについての燃費記録画面を表示する
        // とりあえず、LogCatに流して挙動を観察
        Log.d("onClick", "Button pressed.")
        Log.d("onClick", "v = " + v.toString())

        // 燃費記録追加画面を呼び出す
        addMileage(defaultCarID, defaultCarName)
    }

    /**
     * コンテキストメニューの項目を選択した時の処理。
     * @param item MenuItem型、選択されたメニュー項目
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.ctxitem_add_mileage ->
                // 燃費記録追加画面を呼び出す
                addMileage(currentCarID, currentCarName)
            R.id.ctxitem_delete_car ->
                // クルマを削除する
                deleteCar(currentCarID, currentCarName)
            //		case R.id.ctxitem_edit_car_preference:
            //			// クルマの設定を変更する
            //			editCarPreference(currentCarID, currentCarName);
            //			break;
            R.id.ctxitem_set_default_car ->
                // デフォルトカーにする
                changeAsDefault(currentCarID, currentCarName)
            R.id.ctxitem_show_mileage ->
                // 燃費記録一覧を表示
                showMileageList(currentCarID, currentCarName)
            else -> return super.onContextItemSelected(item)
        }

        return true
    }

    /**
     * コンテキストメニューを閉じたときの処理。
     * onCreateContextMenu()の最後でCursor selectedRowを閉じているが、
     * その副作用で画面表示されているクルマリストが消失してしまうため、
     * 描画処理のあるonResume()をコールしている。
     * @param menu Menu型、閉じられようとしているMenu
     * @see android.app.Activity.onContextMenuClosed
     */
    override fun onContextMenuClosed(menu: Menu) {
        // TODO 自動生成されたメソッド・スタブ
        super.onContextMenuClosed(menu)

        // DBとCursorを閉じてActivityを再始動する
        closeDbAndCursorIfOpen()
        onResume()
    }

    /**
     * コンテキストメニューの生成を行う。
     * ここでは、XMLの記述に従いコンテキストメニューを展開している。
     * @param menu ContextMenu型
     * @param v View型
     * @param menuInfo ContextMenuInfo型
     * @see android.view.View.OnCreateContextMenuListener.onCreateContextMenu
     */
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)

        // 呼び出されたListViewの要素位置を取得する
        val acmi = menuInfo as AdapterContextMenuInfo
        selectedRow = listView_CarList!!.getItemAtPosition(acmi.position) as Cursor

        // カレント値を変数に格納
        currentCarID = selectedRow!!.getInt(selectedRow!!.getColumnIndex("_id"))
        currentCarName = selectedRow!!.getString(selectedRow!!.getColumnIndex("CAR_NAME"))

        // LocCatに流して挙動を観察
        Log.d("onCreateContextMenu", "ContextMenu created, v = " + v.id.toString())
        Log.d("onCreateContextMenu", "row number = $currentCarID")
        Log.d("onCreateContextMenu", "Car Name = " + currentCarName!!)

        // XMLの記述に従い、コンテキストメニューを展開する
        menuInflater.inflate(R.menu.context_carlist, menu)
        menu.setHeaderTitle(getString(R.string.ctxmenutitle_carlist))

        // Cursorを閉じる。
        // ※副作用で、現在表示されているクルマリストが消える。
        selectedRow!!.close()
    }

    /**
     * 燃費記録リストを表示するActivityを呼び出す。
     * Activity呼び出しがメインなので、戻り値はvoidとした。
     * @param carId int型、燃費リスト画面に引き渡すクルマのCAR_ID値。
     * @param carName String型、燃費リスト画面に引き渡すクルマのCAR_NAME値。
     */
    protected fun showMileageList(carId: Int, carName: String?) {
        // 画面遷移の前に、DBとCursorを閉じる。
        closeDbAndCursorIfOpen()

        Log.d(resources.toString(), "CAR_ID : " + carId.toString())
        Log.d(resources.toString(), "CAR_NAME : " + carName!!)

        // 取得したCAR_IDとCAR_NAMEを引数にセットしてstartActivity
        val i = Intent(applicationContext, MileageListActivity::class.java)
        i.putExtra("CAR_ID", carId)
        i.putExtra("CAR_NAME", carName)
        startActivity(i)

    }

    /**
     * 燃費記録を追加するActivityを呼び出す。
     * @param carId int型、呼び出すクルマのCAR_ID
     * @param carName String型、呼び出すクルマのCAR_NAME
     */
    protected fun addMileage(carId: Int, carName: String?) {
        // 画面遷移の前に、DBとCursorを閉じる。
        closeDbAndCursorIfOpen()

        // 取得したCAR_IDとCAR_NAMEを引数にセットしてstartActivity
        val i = Intent(applicationContext, FuelMileageAddActivity::class.java)
        i.putExtra("CAR_ID", carId)
        i.putExtra("CAR_NAME", carName)
        startActivity(i)
    }

    /**
     * 選択したクルマをデフォルトに切り替える。
     * 再描画はonContextMenuClosed(int)の中に定義しているため、特にここでは何もしていない。
     * @param carId int型、デフォルトに切り替えるクルマのCAR_ID
     * @param carName String型、デフォルトに切り替えるクルマのCAR_NAME
     */
    protected fun changeAsDefault(carId: Int, carName: String?) {
        val iRet = dbman.changeDefaultCar(db, carId)

        Log.d("changeAsDefault", iRet.toString() + " row(s) updated.")
        Log.d("changeAsDefault", "Set as default car, CAR_ID = " + carId.toString())
        Log.d("changeAsDefault", "CAR_NAME = " + carName!!)
    }

    /**
     * クルマの設定を変更する。
     */
    protected fun editCarPreference(carId: Int, carName: String) {

    }

    /**
     * クルマを削除する。
     * このクラス内では、getReadableDatabase()でDBを開いているが、Androidにおいては
     * ディスクフルでもない限り書き込みができる仕様なので、これで問題ない。
     * @param carId int型、削除するクルマのCAR_ID
     * @param carName String型、削除するクルマのCAR_NAME
     * @see android.database.sqlite.SQLiteDatabase.delete
     */
    protected fun deleteCar(carId: Int, carName: String?) {
        // TODO 削除確認を行うポップアップダイアログを表示させる
        val adbuilder = AlertDialog.Builder(this)
        adbuilder.setTitle(carName)
        adbuilder.setMessage(getString(R.string.adbuilder_confirm_deletecar))
        // [back]キーでキャンセルができないようにする
        adbuilder.setCancelable(false)

        // 「はい」ボタンの処理
        adbuilder.setPositiveButton(android.R.string.yes) { dialog, which ->
            // TODO 自動生成されたメソッド・スタブ
            var result: Int

            // 削除前に車の名前を取得しておく
            val carname = dbman.getCarNameById(db, carId)

            // クルマのレコードを削除する
            result = dbman.deleteCarById(db, carId)
            Log.d("deleteCar", "car record deleted, CAR_ID = " + carId.toString())
            Log.d("deleteCar", "car record deleted, CAR_NAME = " + carName!!)
            Log.d("deleteCar", "deleted records = " + result.toString())

            // TODO 給油記録とランニングコスト記録を削除するか否かの
            //      確認ダイアログを表示させる
            // 給油記録を削除
            result = dbman.deleteLubsByCarId(db, carId)
            Log.d("deleteCar", "lub record deleted, CAR_ID = " + carId.toString())
            Log.d("deleteCar", "lub record deleted, CAR_NAME = $carName")
            Log.d("deleteCar", "deleted records = " + result.toString())

            // ランニングコスト記録を削除
            result = dbman.deleteCostsByCarId(db, carId)
            Log.d("deleteCar", "costs record deleted, CAR_ID = " + carId.toString())
            Log.d("deleteCar", "costs record deleted, CAR_NAME = $carName")
            Log.d("deleteCar", "deleted records = " + result.toString())

            // DBを再編成する
            dbman.reorgDb(db)

            // レコードを消したというトーストを表示する。
            val line = carname + getString(R.string.adbuilder_toast_deleterecord)
            Toast.makeText(applicationContext, line, Toast.LENGTH_LONG).show()

            // DBとCursorを閉じてActivityを再始動する
            closeDbAndCursorIfOpen()
            onResume()
        }

        // 「キャンセル」ボタンの処理
        //   noなので「いいえ」かと思ったのだが....。
        //   何もせずに終了する。
        adbuilder.setNegativeButton(android.R.string.no) { dialog, which ->
            // TODO 自動生成されたメソッド・スタブ
        }

        // AlertDialogを表示する
        adbuilder.show()
    }

    /**
     * CursorとDBが開いていたら閉じる。
     * 引数なし、グローバル変数を使用。エレガントではないのはわかってはいるが。
     */
    protected fun closeDbAndCursorIfOpen() {
        if (db.isOpen) {
            if (dbman.hasCarRecords(db)) {
                if (cCarList!!.isClosed != true) {
                    cCarList!!.close()
                }
            }
            Log.d(application.toString(), "SQLite database is closing.")
            dbman.close()
        }

        //		if (selectedRow.isClosed() != true) {
        //			selectedRow.close();
        //		}
    }

    /**
     * 「SDカードへのエクスポート」メニューを作成する。
     */
    private fun createExportMenu() {
        Log.d("createEnportMenu", "called export method.")
        val adbuildr = AlertDialog.Builder(this)

        // カスタムビューを使うためのView定義
        val li = LayoutInflater.from(this)
        val view = li.inflate(R.layout.import_dialog, null)
        val editText_exportFilename = view.findViewById<View>(R.id.editText_exportFilename) as EditText

        adbuildr.setTitle(R.string.export_to_sd)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    // TODO 自動生成されたメソッド・スタブ
                    setExternalFile(editText_exportFilename.text.toString())
                    writeToSD(getExternalFile())
                }
                .setNegativeButton(android.R.string.cancel) { dialog, which ->
                    // テキストボックスの入力値をクリアしているが、
                    // キャンセルボタンを押した瞬間にダイアログが閉じるので意味なし(^^;)
                    editText_exportFilename.setText("")
                }
                .show()

    }

    /**
     * 「SDカードからのインポート」メニューを作成する。
     */
    private fun createImportMenu() {
        Log.d("createEnportMenu", "called export method.")

        val adbuilder = AlertDialog.Builder(this)

        adbuilder.setTitle(R.string.import_caution_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.import_caution_body)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes) { dialog, which ->
                    // TODO 自動生成されたメソッド・スタブ
                }
                .setNegativeButton(android.R.string.no) { dialog, which ->
                    // TODO 自動生成されたメソッド・スタブ
                }
                .show()
    }

    /**
     * SDカードにテーブルデータをエクスポートする。
     * @param filename String型、エクスポートするファイル名
     */
    private fun writeToSD(filename: String?) {
        val fullpath = Environment.getExternalStorageDirectory().path + "/" + filename

        writeAllData(fullpath, DbManager.CAR_MASTER, false)
        writeAllData(fullpath, DbManager.LUB_MASTER, true)
        writeAllData(fullpath, DbManager.COSTS_MASTER, true)

    }

    /**
     * テーブルからデータを取得し、指定したパスのファイルへ書き込む。
     * @param fullpath
     * @param targetTableName
     * @param append
     */
    private fun writeAllData(fullpath: String, targetTableName: String, append: Boolean) {

        if (append == false) {
            writeDBVersionHeader(fullpath, db.version)
        }

        try {
            val target = File(fullpath)
            val targetTable = dbman.getWholeRecords(targetTableName, db)

            val maxColumn = targetTable.columnCount
            var headerWords = arrayOfNulls<String>(maxColumn)
            headerWords = targetTable.columnNames.clone()

            val bw = BufferedWriter(FileWriter(target, true))
            val sb = StringBuilder()

            // 最初にヘッダ行をかく
            bw.write("Table name : $targetTableName")
            bw.newLine()

            for (str in headerWords) {
                sb.append(str).append(",")
            }
            val header = sb.append(headerWords[maxColumn - 1]).toString()
            Log.d("writeToSD", "header string = $header")

            bw.write(header)
            bw.newLine()

            if (targetTable.isFirst == false) {
                targetTable.moveToFirst()
            }

            while (targetTable.isAfterLast == false) {
                val sbRow = StringBuilder()

                for (i in 0 until maxColumn - 1) {
                    sbRow.append(targetTable.getString(i)).append(",")
                }
                val rowLine = sbRow.append(targetTable.getString(maxColumn - 1)).toString()

                bw.write(rowLine)
                bw.newLine()

                targetTable.moveToNext()
            }

            bw.newLine()
            bw.close()
            if (targetTable.isClosed == false) targetTable.close()

        } catch (e: Exception) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace()
        }

    }

    /**
     * 目標のファイルが書き込み可能かどうかをチェックする。
     * @param targetFile File型、書き込みを開始する予定のファイル
     * @return boolean型、目標のファイルが存在し、かつ書き込み可能である場合はtrue、そうでない場合はfalse
     */
    private fun isReadyToWriteFile(targetFile: File): Boolean {
        if (targetFile.exists()) {
            if (targetFile.isFile && targetFile.canWrite()) {
                return true
            }
        }
        return false
    }

    /**
     * DBのバージョンをファイルに書き込む。
     * @param fullpath String型、書き込むファイルのフルパス
     * @param dbVersion 書き込むDBのバージョン番号
     */
    private fun writeDBVersionHeader(fullpath: String, dbVersion: Int) {
        try {
            val target = File(fullpath)
            val sb = StringBuilder()
            val bw = BufferedWriter(FileWriter(target, false))
            bw.write("database version : " + dbVersion.toString())
            bw.newLine()
            bw.close()
        } catch (e: IOException) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace()
        }

    }

    companion object {
        lateinit var db: SQLiteDatabase
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState!!.putSerializable(sDefaultCarInstanceState, defaultCar)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        defaultCar = savedInstanceState!!.getSerializable(sDefaultCarInstanceState) as CarMaster
    }
}