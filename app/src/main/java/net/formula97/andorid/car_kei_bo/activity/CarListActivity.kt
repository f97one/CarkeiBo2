/**
 *
 */
package net.formula97.andorid.car_kei_bo.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import net.formula97.andorid.car_kei_bo.*
import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.logic.CarListLogic
import net.formula97.andorid.car_kei_bo.view.adapter.CarListAdapter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * クルマリストを表示するActivity
 * @author kazutoshi
 */
class CarListActivity : AbstractAppActivity() {

    // ウィジェットを扱うための定義
    internal var tv_label_value_defaultcar: TextView? =  null
    internal var listView_CarList: ListView? = null
    internal var button_addFuelRecord: Button? = null

    // ListViewのカレント値を格納する変数
    private var currentCarID: Int = 0
    private var currentCarName: String? = null

    private var externalFile: String? = null

    private var defaultCar: CarMaster? = null

    /**
     * デフォルトカー保存キー
     */
    private val sDefaultCarInstanceState = this::class.java.canonicalName!! + ".sDefaultCarInstanceState"

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
            if (defaultCar == null) {
                val logic = CarListLogic(getAppDb())
                defaultCar = logic.findForDefault()
            }

            addMileage(defaultCar!!)
        }
    }

    override fun onStart() {
        super.onStart()

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
    }

    /**
     * Activityが破棄されるとき、システムから呼ばれる。
     * DBとCursorが開いていたら閉じる。
     * @see android.app.Activity.onDestroy
     */
    override fun onDestroy() {
        super.onDestroy()
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
        if (carList.isNotEmpty()) {

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

            if (!button_addFuelRecord!!.isEnabled) {
                button_addFuelRecord!!.isEnabled = true
            }

        } else {
            // 空の場合は、ボタンが押せるとクラッシュするためロックする
            button_addFuelRecord!!.isEnabled = false
        }
    }

    /**
     * コンテキストメニューの項目を選択した時の処理。
     * @param item MenuItem型、選択されたメニュー項目
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {

        val logic = CarListLogic(getAppDb())

        when (item.itemId) {
            R.id.ctxitem_add_mileage ->
                // 燃費記録追加画面を呼び出す
                addMileage(currentCarID, currentCarName)
            R.id.ctxitem_delete_car ->
                // クルマを削除する
                deleteCar(currentCarID, currentCarName)
            R.id.ctxitem_set_default_car ->
                // デフォルトカーにする
                logic.changeDefault(currentCarID)
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
        super.onContextMenuClosed(menu)

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
        val selectedCar = listView_CarList!!.adapter.getItem(acmi.position) as CarMaster

        // カレント値を格納
        currentCarID = selectedCar.carId
        currentCarName = selectedCar.carName

        // XMLの記述に従い、コンテキストメニューを展開する
        menuInflater.inflate(R.menu.context_carlist, menu)
        menu.setHeaderTitle(getString(R.string.ctxmenutitle_carlist))
    }

    /**
     * 燃費記録リストを表示するActivityを呼び出す。
     * Activity呼び出しがメインなので、戻り値はvoidとした。
     * @param carId int型、燃費リスト画面に引き渡すクルマのCAR_ID値。
     * @param carName String型、燃費リスト画面に引き渡すクルマのCAR_NAME値。
     */
    protected fun showMileageList(carId: Int, carName: String?) {

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
    private fun addMileage(carId: Int, carName: String?) {
        // 取得したCAR_IDとCAR_NAMEを引数にセットしてstartActivity
        val i = Intent(applicationContext, FuelMileageAddActivity::class.java)
        i.putExtra("CAR_ID", carId)
        i.putExtra("CAR_NAME", carName)
        startActivity(i)
    }

    private fun addMileage(carMaster: CarMaster) {
        addMileage(carMaster.carId, carMaster.carName)
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
        // 削除確認を行うポップアップダイアログを表示させる
        val callback: MsgDialog.OnDialogButtonClickCallback = object : MsgDialog.OnDialogButtonClickCallback {
            override fun onPositiveClick(msgResId: Int) {
                val logic = CarListLogic(getAppDb())
                logic.deleteCarMileage(carId)

                val line = carName + getString(R.string.adbuilder_toast_deleterecord)
                Toast.makeText(applicationContext, line, Toast.LENGTH_LONG).show()

            }

            override fun onNegativeClick(msgResId: Int) {
                // nothing to do
            }

            override fun onMiddleClick(msgResId: Int) {
                // nothing to do
            }
        }

        val cond = MsgDialogCondition(R.string.adbuilder_confirm_deletecar)
        cond.titleStr = carName
        cond.cancelable = false
        cond.positiveButtonMsgResId = android.R.string.yes
        cond.negativeButtonMsgResId = android.R.string.no
        cond.needNegativeButton = true

        val d: MsgDialog = MsgDialog.getInstance(cond, callback)
        d.show(supportFragmentManager, MsgDialog.DIALOG_TAG)
    }

    /**
     * 「SDカードへのエクスポート」メニューを作成する。
     */
    private fun createExportMenu() {
        // TODO エクスポート確認ダイアログを表示する処理を書く

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

        // TODO インポート確認ダイアログを表示する処理を書く

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

        // TODO ファイル出力処理を再実装する

//        if (append == false) {
//            writeDBVersionHeader(fullpath, db.version)
//        }
//
//        try {
//            val target = File(fullpath)
//            val targetTable = dbman.getWholeRecords(targetTableName, db)
//
//            val maxColumn = targetTable.columnCount
//            var headerWords = arrayOfNulls<String>(maxColumn)
//            headerWords = targetTable.columnNames.clone()
//
//            val bw = BufferedWriter(FileWriter(target, true))
//            val sb = StringBuilder()
//
//            // 最初にヘッダ行をかく
//            bw.write("Table name : $targetTableName")
//            bw.newLine()
//
//            for (str in headerWords) {
//                sb.append(str).append(",")
//            }
//            val header = sb.append(headerWords[maxColumn - 1]).toString()
//            Log.d("writeToSD", "header string = $header")
//
//            bw.write(header)
//            bw.newLine()
//
//            if (targetTable.isFirst == false) {
//                targetTable.moveToFirst()
//            }
//
//            while (targetTable.isAfterLast == false) {
//                val sbRow = StringBuilder()
//
//                for (i in 0 until maxColumn - 1) {
//                    sbRow.append(targetTable.getString(i)).append(",")
//                }
//                val rowLine = sbRow.append(targetTable.getString(maxColumn - 1)).toString()
//
//                bw.write(rowLine)
//                bw.newLine()
//
//                targetTable.moveToNext()
//            }
//
//            bw.newLine()
//            bw.close()
//            if (targetTable.isClosed == false) targetTable.close()
//
//        } catch (e: Exception) {
//            // TODO 自動生成された catch ブロック
//            e.printStackTrace()
//        }

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

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState!!.putSerializable(sDefaultCarInstanceState, defaultCar)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        defaultCar = savedInstanceState!!.getSerializable(sDefaultCarInstanceState) as CarMaster
    }
}