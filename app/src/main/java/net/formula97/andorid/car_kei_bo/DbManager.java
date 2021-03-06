/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.math.BigDecimal;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import net.formula97.andorid.car_kei_bo.logic.DateManager;

/**
 * @author kazutoshi
 *  DB操作関連メソッドを定義するクラス
 *
 *    クエリ系のメソッドのうち、Cursor型で定義していないメソッドについては、
 *    必要な値を仮変数に格納した後、Cursor#close()で閉じるようにしている。
 *    # でないと「Cursor閉じろやｺﾞﾙｧ!!」とか怒られる。が、動作上は問題はなさそうだが....
 *
 */
public class DbManager extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "fuel_mileage.db";
	private static final int DB_VERSION = 1;

	public static int getDbVersion() {
		return DB_VERSION;
	}

	// テーブルの名称を定義
	public static final String LUB_MASTER = "LUB_MASTER";
	public static final String COSTS_MASTER = "COSTS_MASTER";
	public static final String CAR_MASTER = "CAR_MASTER";

	public DbManager(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
		// TODO 自動生成されたコンストラクター・スタブ
		// ここにdbファイルとバージョンを定義する
	}

	/**
	 * CAR_IDに対応する燃費記録を、LUB_MASTERに記録する。
	 * @param db SQLiteDatabase型、燃費を記録するDBインスタンス
	 * @param carId int型、燃費を記録するクルマのCAR_ID
	 * @param amountOfOil long型、給油量
	 * @param tripMeter double型、給油を行った時のトリップメーター値
	 * @param unitPrice double型、給油を行った時の給油単価
	 * @param comments String型、給油時のコメントを入力
	 * @param gregolianDay Calendar型、給油を行った日時
	 * @return long型、insertに成功すればそのときのrowIdを、失敗すれば-1を返す。なお、失敗時はSQLExceptionを投げる
	 */
	protected long addMileageById (SQLiteDatabase db, int carId, double amountOfOil, double tripMeter,
			double unitPrice, String comments, Calendar gregolianDay) {
		long result = 0;

		// レコードを追加する
		ContentValues value = new ContentValues();
		value.put("CAR_ID", carId);
		// 入力されたCalendarをユリウス通日に変換する
		DateManager dm = new DateManager();
		double julianDay = dm.toJulianDay(gregolianDay);

		// 価格、距離、体積の各単位をセット
		value.put("REFUEL_DATE", julianDay);
		value.put("LUB_AMOUNT", amountOfOil);
		value.put("UNIT_PRICE", unitPrice);
		value.put("TRIPMETER", tripMeter);
		value.put("COMMENTS", comments);

		// トランザクション開始
		db.beginTransaction();
		try {
			// 失敗したら例外を投げるinsertOrThrowでレコードをINSERT
			result = db.insertOrThrow(LUB_MASTER, null, value);

			// 例外が投げられなければ、トランザクション成功をセット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(DATABASE_NAME, "Car record insert failed, ");
		} finally {
			// トランザクション終了
			// INSERTに失敗した場合は、endTransaction()を呼んだところでロールバックされる
			db.endTransaction();
		}

		return result;
	}

	/**
	 * RECORD_IDに相当する給油レコードを修正する。
	 * @param db  SQLiteDatabase型、燃費を記録するDBインスタンス
	 * @param recordId int型、記録を修正する給油記録のRECORD_ID
	 * @param amountOfOil long型、給油量
	 * @param tripMeter double型、給油を行った時のトリップメーター値
	 * @param unitPrice double型、給油を行った時の給油単価
	 * @param comments String型、給油時のコメントを入力
	 * @param gregolianDay Calendar型、給油を行った日時
	 * @return int型、UPDATEしたレコード数(通常は1)、UPDATEが行わなければ0を返す
	 */
	protected int updatedMileageByRecordId (SQLiteDatabase db, int recordId, double amountOfOil, double tripMeter,
			double unitPrice, String comments, Calendar gregolianDay) {
		int result = 0;

		// レコードを追加する
		ContentValues value = new ContentValues();
		// 入力されたCalendarをユリウス通日に変換する
		DateManager dm = new DateManager();
		double julianDay = dm.toJulianDay(gregolianDay);

		// 価格、距離、体積の各単位をセット
		value.put("REFUEL_DATE", julianDay);
		value.put("LUB_AMOUNT", amountOfOil);
		value.put("UNIT_PRICE", unitPrice);
		value.put("TRIPMETER", tripMeter);
		value.put("COMMENTS", comments);

		// クエリ範囲とその値
		String whereClause = "RECORD_ID = ?";
		String[] whereArgs = {String.valueOf(recordId)};

		// トランザクション開始
		db.beginTransaction();
		try {
			// 失敗したら例外を投げるinsertOrThrowでレコードをINSERT
			result = db.update(LUB_MASTER, value, whereClause, whereArgs);

			// 例外が投げられなければ、トランザクション成功をセット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(DATABASE_NAME, "SQLException occured, car record update failed, ");
		} catch (Exception e) {
			Log.e(DATABASE_NAME, "Other Exception occured, car record update failed, ");
		} finally {
			// トランザクション終了
			// INSERTに失敗した場合は、endTransaction()を呼んだところでロールバックされる
			db.endTransaction();
		}

		return result;
	}

	/**
	 * クルマのレコードを追加する。
	 *   ....引数多いな、オイ(^^;)
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carName String型、追加するクルマの名前
	 * @param isDefaultCar boolean型、デフォルトのクルマにセットするか否か
	 * @param priceUnit String型、価格の単位を格納
	 * @param distanceUnit String型、距離の単位を格納
	 * @param volumeUnit String型、体積の単位を格納
	 * @return long型、insertに成功すればそのときのrowIdを、失敗すれば-1を返す。なお、失敗時はSQLExceptionを投げる
	 */
	protected long addNewCar(SQLiteDatabase db, String carName, boolean isDefaultCar,
			String priceUnit, String distanceUnit, String volumeUnit) {
		// insertOrThrow()の戻り値を格納する変数を、0で初期化する
		long result = 0;

		// レコードを追加する
		ContentValues value = new ContentValues();
		value.put("CAR_NAME", carName);
		// defaultCarにtrueが渡されている場合は1を、そうでない場合は0をputする
		if (isDefaultCar == true) {
			value.put("DEFAULT_FLAG", 1);
		} else {
			value.put("DEFAULT_FLAG", 0);
		}

		// 価格、距離、体積の各単位をセット
		value.put("PRICEUNIT", priceUnit);			// 単価
		value.put("DISTANCEUNIT", distanceUnit);	// 距離
		value.put("VOLUMEUNIT", volumeUnit);		// 体積
		value.put("FUELMILEAGE_LABEL", distanceUnit + "/" + volumeUnit);	// 燃費
		value.put("RUNNINGCOST_LABEL", priceUnit + "/" + distanceUnit);		// ランニングコスト

		// トランザクション開始
		db.beginTransaction();
		try {
			// 失敗したら例外を投げるinsertOrThrowでレコードをINSERT
			result = db.insertOrThrow(CAR_MASTER, null, value);

			// 例外が投げられなければ、トランザクション成功をセット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(DATABASE_NAME, "Car record insert failed, ");
		} finally {
			// トランザクション終了
			// INSERTに失敗した場合は、endTransaction()を呼んだところでロールバックされる
			db.endTransaction();
		}

		return result;
	}

	/**
	 * デフォルトカーフラグを変更する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、フラグを変更するするクルマのcarId
	 * @return int型、Updateに成功したレコード数
	 */
	protected int changeDefaultCar(SQLiteDatabase db, int carId) {
		// クエリを格納する変数の定義
		ContentValues cv = new ContentValues();
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		int result;

		// 安全のためトランザクションを開始する
		db.beginTransaction();
		try {
			// まず、すべてのデフォルトカーフラグを降ろす
			cv.put("DEFAULT_FLAG", 0);
			result = db.update(CAR_MASTER, cv, null, null);

			// 次に、carIdで指定されたレコードのみに
			cv.clear();
			cv.put("DEFAULT_FLAG", 1);
			result = db.update(CAR_MASTER, cv, where, args);

			// トランザクションの正常終了を宣言
			db.setTransactionSuccessful();
		} finally {
			// トランザクションを終了する。
			// 例外発生とかでトランザクションが正常に完結しなかった場合（＝setTransactionSuccessful()が呼ばれていない）は、
			// endTransaction()を呼んだところでロールバックされる。
			db.endTransaction();
		}

		return result;
	}

	/**
	 * デフォルトフラグを一律下げる。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return int型、Updateに成功したレコード数
	 */
	protected int clearAllDefaultFlags(SQLiteDatabase db) {
		// クエリを格納する変数の定義
		ContentValues cv = new ContentValues();
		int result;

		// 安全のためトランザクションを開始する
		db.beginTransaction();
		try {
			cv.put("DEFAULT_FLAG", 0);
			result = db.update(CAR_MASTER, cv, null, null);

			// トランザクションの正常終了を宣言
			db.setTransactionSuccessful();
		} finally {
			// トランザクションを終了する。
			// 例外発生とかでトランザクションが正常に完結しなかった場合（＝setTransactionSuccessful()が呼ばれていない）は、
			// endTransaction()を呼んだところでロールバックされる。
			db.endTransaction();
		}

		return result;
	}

	/**
	 * クルマのCAR_IDからクルマのレコードを削除する。
	 * @param db SQLiteDatabase型、レコード削除対象のDBインスタンス
	 * @param carId int型、削除するクルマのCAR_ID
	 * @return int型、削除したレコード数
	 */
	protected int deleteCarById(SQLiteDatabase db, int carId) {
		int result;

		// deleteメソッドに渡す値
		String table = CAR_MASTER;
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};

		// 削除したレコード数を格納する。
		// 通常「1」しか入っていないはず....。
		result = db.delete(table, where, args);

		return result;
	}

	/**
	 * クルマのCAR_IDに対応するランニングコスト記録を削除する。
	 * @param db SQLiteDatabase型、レコード削除対象のDBインスタンス
	 * @param carId int型、削除するクルマのCAR_ID
	 * @return int型、削除したレコード数
	 */
	protected int deleteCostsByCarId(SQLiteDatabase db, int carId) {
		int result;

		// deleteメソッドに渡す値
		String table = COSTS_MASTER;
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};

		// 削除したレコード数を格納する。
		result = db.delete(table, where, args);

		return result;
	}

	/**
	 * クルマのCAR_IDに対応するランニングコスト記録を削除する。
	 * @param db SQLiteDatabase型、レコード削除対象のDBインスタンス
	 * @param carId int型、削除するクルマのCAR_ID
	 * @param recordId int型、削除するランニングコスト記録のRECORD_ID
	 * @return int型、削除したレコード数
	 */
	protected int deleteCostsByCarId(SQLiteDatabase db, int carId, int recordId) {
		int result;

		// deleteメソッドに渡す値
		String table = COSTS_MASTER;
		String where = "CAR_ID = ? AND RECORD_ID = ?";
		String[] args = {String.valueOf(carId), String.valueOf(recordId)};

		// 削除したレコード数を格納する。
		result = db.delete(table, where, args);

		return result;
	}

	/**
	 * クルマのCAR_IDに対応する給油記録を削除する。
	 * @param db SQLiteDatabase型、レコード削除対象のDBインスタンス
	 * @param carId int型、削除するクルマのCAR_ID
	 * @return int型、削除したレコード数
	 */
	protected int deleteLubsByCarId(SQLiteDatabase db, int carId) {
		int result;

		// deleteメソッドに渡す値
		String table = LUB_MASTER;
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};

		// 削除したレコード数を格納する。
		result = db.delete(table, where, args);

		return result;
	}

	/**
	 * クルマのCAR_IDに対応する給油記録を削除する。
	 * @param db SQLiteDatabase型、レコード削除対象のDBインスタンス
	 * @param carId int型、削除するクルマのCAR_ID
	 * @param recordId int型、削除する燃費記録のRECORD_ID
	 * @return int型、削除したレコード数
	 */
	protected int deleteLubsByCarId(SQLiteDatabase db, int carId, int recordId) {
		int result;

		// deleteメソッドに渡す値
		String table = LUB_MASTER;
		String where = "CAR_ID = ? AND RECORD_ID = ?";
		String[] args = {String.valueOf(carId), String.valueOf(recordId)};

		// 削除したレコード数を格納する。
		result = db.delete(table, where, args);

		return result;
	}

	/**
	 * 入力されたクルマのCAR_IDから、CAR_NAMEを返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、チェックするクルマのcarId
	 * @return String型、チェックする車のcarIdに対応する名前
	 */
	protected String findCarNameById(SQLiteDatabase db, int carId) {
		// 戻り値を格納する変数
		String sRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_ID = ?";
		// CAR_IDはintだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(carId)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		sRet = q.getString(0);
		q.close();

		return sRet;
	}

	/**
	 * 入力されたクルマの名前から、CAR_IDを返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carName String型、チェックするクルマの名前
	 * @return int型、チェックする車の名前に対応するcarId
	 */
	protected int getCarId(SQLiteDatabase db, String carName) {
		// 戻り値を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_ID"};
		String where = "CAR_NAME = ?";
		String[] args = {carName};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		iRet = q.getInt(0);
		q.close();

		return iRet;
	}

	/**
	 * クルマリストのリストビューに差し込むデータを取得する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return Cursor型、Cursorオブジェクトをそのまま返すので、Cursor#close()は行わない。
	 */
	protected Cursor getCarList(SQLiteDatabase db) {
		// クエリを格納する変数の定義
		Cursor q;
		String[] columns = {"CAR_ID AS _id", "CAR_NAME", "CURRENT_FUEL_MILEAGE", "FUELMILEAGE_LABEL", "CURRENT_RUNNING_COST", "RUNNINGCOST_LABEL"};
		//String where = "CAR_ID = ?";
		//String[] args = {String.valueOf(1)};
		//String groupBy = ""
		//String having = ""
		String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, null, null, null, null, orderBy);
		q.moveToFirst();

		return q;
	}

	/**
	 * CAR_IDに対応するクルマの名称を返す。
	 * @param db SQLiteDatabase型、
	 * @param carId
	 * @return
	 */
	protected String getCarNameById (SQLiteDatabase db, int carId) {
		// 戻り値を格納する変数
		String sRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_ID = ?";
		// DEFAULT_FLAG=1を検索するのだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(carId)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		sRet = q.getString(0);
		Log.i(CAR_MASTER, "Found specified car : " + sRet + " , related to CAR_ID : " + String.valueOf(carId));
		q.close();

		return sRet;
	}

	/**
	 * 燃費追加等でスピナーにセットするデータを取得する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return Cursor型、Cursorオブジェクトをそのまま返すので、Cursor#close()は行わない。
	 */
	protected Cursor getCarNameList(SQLiteDatabase db) {
		// クエリを格納する変数の定義
		Cursor q;
		String[] columns = {"CAR_ID AS _id", "CAR_NAME"};
		//String where = "CAR_ID = ?";
		//String[] args = {String.valueOf(1)};
		//String groupBy = ""
		//String having = ""
		String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, null, null, null, null, orderBy);
		q.moveToFirst();

		return q;
	}

	/**
	 * デフォルトカーフラグのあるCAR_IDを返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return int型、デフォルトカーフラグのあるクルマのcarId
	 */
	protected int getDefaultCarId(SQLiteDatabase db) {
		// 戻り値を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_ID"};
		String where = "DEFAULT_FLAG = ?";
		// DEFAULT_FLAG=1を検索するのだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(1)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		iRet = q.getInt(0);
		q.close();

		return iRet;
	}

	/**
	 * デフォルトカーフラグのあるCAR_NAMEを返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return String型、デフォルトカーフラグのあるクルマの名前
	 */
	protected String getDefaultCarName(SQLiteDatabase db) {
		// 戻り値を格納する変数
		String sRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "DEFAULT_FLAG = ?";
		// DEFAULT_FLAG=1を検索するのだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(1)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		sRet = q.getString(0);
		Log.i(CAR_MASTER, "Found default car : " + sRet);
		q.close();

		return sRet;
	}

	/**
	 * CAR_IDのクルマに対応する距離の単位を返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車の距離の単位
	 */
	protected String getDistanceUnitById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"DISTANCEUNIT"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_IDのクルマに対応する燃費の表示ラベルを返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車の燃費の表示ラベル
	 */
	protected String getFuelmileageLabelById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"FUELMILEAGE_LABEL"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_IDのクルマに対応する価格の単位を返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車の価格の単位
	 */
	protected String getPriceUnitById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"PRICEUNIT"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_IDのクルマに対応するランニングコストの表示ラベルを返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車のランニングコストの表示ラベル
	 */
	protected String getRunningcostLabelById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"RUNNINGCOST_LABEL"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_IDのクルマに対応する体積の単位を返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車の体積の単位
	 */
	protected String getVolumeUnitById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"VOLUMEUNIT"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_MASTERに有効なレコードがあるかを調べる。
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return boolean型、有効なレコードがあればtrue、なければfalse
	 */
	protected boolean hasCarRecords(SQLiteDatabase db) {
		// getCount()を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(CAR_MASTER, null, null, null, null, null, null);
		q.moveToFirst();
		iRet = q.getCount();
		q.close();

		if (iRet == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * COSTS_MASTERに有効なレコードがあるかを調べる
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return boolean型、有効なレコードがあればtrue、なければfalse
	 */
	protected boolean hasCostsRecords(SQLiteDatabase db) {
		// getCount()を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(COSTS_MASTER, null, null, null, null, null, null);
		q.moveToFirst();
		iRet = q.getCount();
		q.close();

		if (iRet == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * LUB_MASTERに有効なレコードがあるかを調べる
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return boolean型、有効なレコードがあればtrue、なければfalse
	 */
	protected boolean hasLubRecords(SQLiteDatabase db) {
		// getCount()を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(LUB_MASTER, null, null, null, null, null, null);
		q.moveToFirst();
		iRet = q.getCount();
		q.close();

		if (iRet == 0) {
			return false;
		} else {
			return true;
		}
	}

	protected boolean hasLubRecords(SQLiteDatabase db, int carId) {
		boolean ret = false;

		Cursor q;

		// SQL共通部分
		String selection = "CAR_ID = ?";
		String[] selectionArgs = {String.valueOf(carId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		String[] columns = null;

		q = db.query(LUB_MASTER, columns, selection, selectionArgs, groupBy, having, orderBy);
		q.moveToFirst();

		if (q.getCount() > 0) {
			ret = true;
		}

		q.close();
		return ret;
	}

	/**
	 * 重複チェックその２
	 *   すでにデフォルトカーフラグがセットされているか否かをチェックする。
	 *   @param db SQLiteDatabase型、操作するDBインスタンス
	 *   @return boolean型、trueだとチェック済み、falseはチェックなし
	 */
	protected boolean isExistDefaultCarFlag(SQLiteDatabase db) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		//String[] columns = {"DEFAULT_FLAG"};
		String where = "DEFAULT_FLAG = ?";
		String[] args = {"1"};

		q = db.query(CAR_MASTER, null, where, args, null, null, null);
		q.moveToFirst();

		int defaultFlag = q.getCount();
		q.close();

		if (defaultFlag == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 重複チェックその２
	 *   やっていることは上と同じ。
	 *   @param db SQLiteDatabase型、操作するDBインスタンス
	 *   @param carId int型、チェックするcarIdを指定する
	 *   @return boolean型、trueだとチェック済み、falseはチェックなし
	 */
	protected boolean isExistDefaultCarFlag(SQLiteDatabase db, int carId) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"DEFAULT_FLAG"};
		String where = "CAR_ID = ?";
		// CAR_IDはintだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(carId)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		int defaultFlag = q.getInt(0);
		q.close();

		if (defaultFlag == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 重複チェックその１
	 *   同一名称の車がないかをチェックする。
	 *   @param db SQLiteDatabase型、操作するDBインスタンス
	 *   @param carName String型、重複がないか調べるクルマの名前
	 *   @return boolean型、trueだと重複があり、falseだと重複はない。
	 */
	protected boolean isExistSameNameCar(SQLiteDatabase db, String carName) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_NAME = ?";
		String[] args = {carName};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		// 検索結果の総数を調査
		int count = q.getCount();
		q.close();

		// 総数が0（＝検索結果がない）の場合はfalseを、そうでない場合はtrueを返す
		if (count == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * DBがない場合にコンストラクタからコールされ、テーブルをDDLに従い作成する。
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// テーブル作成
		String create_lub_master;
		String create_costs_master;
		String create_car_master;

		// DDL
		//   LUB_MASTERテーブル
		create_lub_master = "CREATE TABLE IF NOT EXISTS LUB_MASTER " +
				"(RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"REFUEL_DATE REAL, " +
				"CAR_ID INTEGER, " +
				"LUB_AMOUNT REAL DEFAULT 0, " +
				"UNIT_PRICE REAL DEFAULT 0, " +
				"TRIPMETER REAL DEFAULT 0, " +
				"COMMENTS TEXT);";

		//   COSTS_MASTERテーブル
		create_costs_master = "CREATE TABLE IF NOT EXISTS COSTS_MASTER " +
				"(RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"CAR_ID INTEGER, " +
				"REFUEL_DATE REAL, " +
				"RUNNING_COST REAL DEFAULT 0);";

		//   CAR_MASTERテーブル
		create_car_master = "CREATE TABLE IF NOT EXISTS CAR_MASTER " +
				"(CAR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"CAR_NAME TEXT, " +
				"DEFAULT_FLAG INTEGER DEFAULT 0, " +
				"CURRENT_FUEL_MILEAGE REAL DEFAULT 0, " +
				"CURRENT_RUNNING_COST REAL DEFAULT 0, " +
				"PRICEUNIT TEXT, " +
				"DISTANCEUNIT TEXT, " +
				"VOLUMEUNIT TEXT, " +
				"FUELMILEAGE_LABEL TEXT, " +
				"RUNNINGCOST_LABEL TEXT);";

		// DDLをexecSQLで実行する
		db.execSQL(create_lub_master);
		db.execSQL(create_costs_master);
		db.execSQL(create_car_master);
	}

	/**
	 * DBのバージョンが既存のDBより大きい場合、コンストラクタからコールされるが、
	 * 初期リリースのためダミー処理を書いている。
	 * アプリケーションのバージョンアップを行う場合は、ここの処理を抜本的に
	 * 書き直す必要あり。
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param oldVersion int型、DBの古いバージョン番号、コンストラクタが決める？
	 * @param newVersion int型、DBの新しいバージョン番号、コンストラクタが決める？
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 自動生成されたメソッド・スタブ
		//   本来はALTER TABLEなどでテーブル構造を編集するべきだが、
		//   テスト用にいったんドロップしてテーブルを作り直す処理としておく
		Log.w("Car-Kei-Bo", "Updating database, which will destroy all old data.(for testing)");

		// テーブルをいったんすべてドロップ
		db.execSQL("DROP TABLE IF EXISTS LUB_MASTER;");
		db.execSQL("DROP TABLE IF EXISTS COSTS_MASTER;");
		db.execSQL("DROP TABLE IF EXISTS CAR_MASTER;");

		// onCreate()で作成しなおす
		onCreate(db);
	}

	/**
	 * データベースを再編成する。
	 * @param db SQLiteDatabase型、再編成対象のDBインスタンス
	 */
	protected void reorgDb(SQLiteDatabase db) {
		db.execSQL("vacuum");
	}

	/**
	 * 指定したクルマのCAR_IDに対する、すべての給油記録を返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、検索対象のクルマのCAR_ID
	 * @param invertOrder trueにすると日付を降順に、falseにすると日付を昇順にオーダーする
	 * @return Cursor型、検索結果
	 */
	protected Cursor getRefuelRecordsById(SQLiteDatabase db, int carId, boolean invertOrder) {
		Cursor q;
		String order;

		// 検索結果は登録時刻順で返すが、そのソート結果を決める
		if (invertOrder) {
			order = "DESC";
		} else {
			order = "";
		}

		// テーブルをまたぐのでrawQueryでSQL文を直接たたいている。
		// CursorAdapterに接続するとき、「_id」という名前のカラムがないと怒られるので、
		// 画面上必要はないがRECORD_IDを_idとして返している。
		q = db.rawQuery("SELECT LUB_MASTER.RECORD_ID as _id, datetime(LUB_MASTER.REFUEL_DATE) as DATE_OF_REFUEL," +
				" LUB_MASTER.LUB_AMOUNT, CAR_MASTER.VOLUMEUNIT FROM LUB_MASTER, CAR_MASTER" +
				" WHERE LUB_MASTER.CAR_ID = CAR_MASTER.CAR_ID" +
				" AND LUB_MASTER.CAR_ID = " + String.valueOf(carId) +
				" ORDER BY LUB_MASTER.REFUEL_DATE " + order + ";", null);
		q.moveToFirst();

		return q;
	}

	/**
	 * COSTS_MASTERに給油時のランニングコスト値を格納する。
	 * @param carId int型、ランニングコストを格納するクルマのCAR_ID
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param runningCosts
	 * @param gregorianDay
	 * @return
	 */
	protected long addRunningCostRecord(SQLiteDatabase db, int carId, double runningCosts, Calendar gregorianDay) {
		long ret = 0;

		// Calendar型の日付はユリウス通日に変換する
		DateManager dmngr = new DateManager();
		double julianDay = dmngr.toJulianDay(gregorianDay);

		// フィールドにセットする値を一式作成する
		ContentValues values = new ContentValues();
		values.put("CAR_ID", carId);
		values.put("REFUEL_DATE", julianDay);
		values.put("RUNNING_COST", runningCosts);

		db.beginTransaction();

		try {
			ret = db.insertOrThrow(COSTS_MASTER, null, values);

			db.setTransactionSuccessful();
		} catch (SQLException sqle) {
			Log.e("addRunningCostRecord", "SQLException occured, update failed");
		} catch (Exception e) {
			Log.e("addRunningCostRecord", "Other Exception occured, update failed");
		} finally {
			db.endTransaction();
		}

		return ret;
	}

	protected int updateRunningCostRecord(SQLiteDatabase db, int carId, double runningCosts, Calendar gregorianDay, double refuelJulianDay) {
		int ret = 0;

		// Calendar型の日付はユリウス通日に変換する
		DateManager dmngr = new DateManager();
		double jDay = dmngr.toJulianDay(gregorianDay);

		// フィールドにセットする値を一式作成する
		ContentValues values = new ContentValues();
		values.put("CAR_ID", carId);
		values.put("REFUEL_DATE", jDay);
		values.put("RUNNING_COST", runningCosts);

		String whereClause = "REFUEL_DATE = ?";
		String[] whereArgs = {String.valueOf(refuelJulianDay)};

		db.beginTransaction();

		try {
			ret = db.update(COSTS_MASTER, values, whereClause, whereArgs);

			db.setTransactionSuccessful();
		} catch (SQLException sqle) {
			Log.e("addRunningCostRecord", "SQLException occured, update failed");
		} catch (Exception e) {
			Log.e("addRunningCostRecord", "Other Exception occured, update failed");
		} finally {
			db.endTransaction();
		}

		return ret;
	}

	/**
	 * クルマのCAR_IDに対応した「燃費全レコードの平均」を更新する。
	 * 「燃費全レコードの平均」は、メソッド内部で計算する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、更新するクルマのCAR_ID
	 * @return int型、UPDATEしたレコード数(普通なら1が返る)
	 */
	protected int updateCurrentFuelMileageById(SQLiteDatabase db, int carId) {
		int ret = 0;
		Cursor qOil, qDst;

		// SQL共通部分
		String selection = "CAR_ID = ?";
		String[] selectionArgs = {String.valueOf(carId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		// 給油量の合計を取得する
		String[] clmsOil = {"sum(LUB_AMOUNT)"};
		qOil = db.query(LUB_MASTER, clmsOil, selection, selectionArgs, groupBy, having, orderBy);
		qOil.moveToFirst();
		double totalOil = qOil.getDouble(0);
		qOil.close();

		// 走行距離の合計を取得する
		String[] clmsDst = {"sum(TRIPMETER)"};
		qDst = db.query(LUB_MASTER, clmsDst, selection, selectionArgs, groupBy, having, orderBy);
		qDst.moveToFirst();
		double totalDst = qDst.getDouble(0);
		qDst.close();

		// トータル走行距離をトータル給油量でべたに割って平均を求める
		double cur = totalDst / totalOil;

		// 小数点2ケタに丸める
		BigDecimal bd = new BigDecimal(cur);
		double current = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

		// 計算したトータル燃費でCAR_MASTER.CURRENT_FUEL_MILEAGEを書き換える
		ContentValues values = new ContentValues();
		values.put("CURRENT_FUEL_MILEAGE", current);
		String whereClause = "CAR_ID = ?";
		String[] whereArgs = {String.valueOf(carId)};

		db.beginTransaction();

		try {
			ret = db.update(CAR_MASTER, values, whereClause, whereArgs);

			db.setTransactionSuccessful();
		} catch (SQLException sqle) {
			Log.e("updateCurrentFuelMileageById", "SQLException occured, update failed");
		} catch (Exception e) {
			Log.e("updateCurrentFuelMileageById", "Other Exception occured, update failed");
		} finally {
			db.endTransaction();
		}

		return ret;
	}

	/**
	 * そのクルマの給油記録全体に対するランニングコストを更新する。
	 * 「給油記録全体に対するランニングコスト」は内部で計算する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、更新するクルマのCAR_ID
	 * @return int型、UPDATEしたレコード数
	 */
	protected int updateCurrentRunningCostById(SQLiteDatabase db, int carId) {
		int ret = 0;
		Cursor qOil, qDst, qPrice;

		// SQL共通部分
		String selection = "CAR_ID = ?";
		String[] selectionArgs = {String.valueOf(carId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		// 給油単価の平均を取得する
		String[] clmsPrice = {"avg(UNIT_PRICE)"};
		qPrice = db.query(LUB_MASTER, clmsPrice, selection, selectionArgs, groupBy, having, orderBy);
		qPrice.moveToFirst();
		double avgPrice = qPrice.getDouble(0);
		qPrice.close();

		// 給油量の合計を取得する
		String[] clmsOil = {"sum(LUB_AMOUNT)"};
		qOil = db.query(LUB_MASTER, clmsOil, selection, selectionArgs, groupBy, having, orderBy);
		qOil.moveToFirst();
		double totalOil = qOil.getDouble(0);
		qOil.close();

		// 走行距離の合計を取得する
		String[] clmsDst = {"sum(TRIPMETER)"};
		qDst = db.query(LUB_MASTER, clmsDst, selection, selectionArgs, groupBy, having, orderBy);
		qDst.moveToFirst();
		double totalDst = qDst.getDouble(0);
		qDst.close();

		// トータル値でランニングコストを計算する
		double cur = totalOil * avgPrice / totalDst;

		// 小数点2ケタに丸める
		BigDecimal bd = new BigDecimal(cur);
		double current = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

		// 計算したトータル燃費でCAR_MASTER.CURRENT_RUNNING_COSTを書き換える
		ContentValues values = new ContentValues();
		values.put("CURRENT_RUNNING_COST", current);
		String whereClause = "CAR_ID = ?";
		String[] whereArgs = {String.valueOf(carId)};

		db.beginTransaction();

		try {
			ret = db.update(CAR_MASTER, values, whereClause, whereArgs);

			db.setTransactionSuccessful();
		} catch (SQLException sqle) {
			Log.e("updateCurrentRunningCostById", "SQLException occured, update failed");
		} catch (Exception e) {
			Log.e("updateCurrentRunningCostById", "Other Exception occured, update failed");
		} finally {
			db.endTransaction();
		}

		return ret;
	}

	/**
	 * そのクルマのトータルの燃費を取得する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、トータルの燃費を取得するクルマのCAR_ID
	 * @return double型、そのクルマのトータルの燃費
	 */
	protected double getCurrentMileageById (SQLiteDatabase db, int carId) {
		double ret = 0;
		Cursor q;

		// SQL共通部分
		String selection = "CAR_ID = ?";
		String[] selectionArgs = {String.valueOf(carId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		String[] columns = {"CURRENT_FUEL_MILEAGE"};

		q = db.query(CAR_MASTER, columns, selection, selectionArgs, groupBy, having, orderBy);
		q.moveToFirst();

		ret = q.getDouble(0);
		q.close();

		return ret;
	}

	/**
	 * そのクルマのトータルのランニングコストを取得する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、トータルの燃費を取得するクルマのCAR_ID
	 * @return double型、そのクルマのトータルの燃費
	 */
	protected double getCurrentRunningCostById (SQLiteDatabase db, int carId) {
		double ret = 0;
		Cursor q;

		// SQL共通部分
		String selection = "CAR_ID = ?";
		String[] selectionArgs = {String.valueOf(carId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		String[] columns = {"CURRENT_RUNNING_COST"};

		q = db.query(CAR_MASTER, columns, selection, selectionArgs, groupBy, having, orderBy);
		q.moveToFirst();

		ret = q.getDouble(0);
		q.close();

		return ret;
	}

	/**
	 * そのクルマの給油情報を取得する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、給油情報を取得するクルマのCAR_ID
	 * @param rowId int型、レコードの一貫番号
	 * @return Cursor型、
	 */
	protected Cursor getRefuelRecordById (SQLiteDatabase db, int carId, int rowId) {
		Cursor ret;

		// 複数の検索条件を書く必要があるので、rawQueryにした
		String sql = "SELECT * FROM LUB_MASTER " +
					"WHERE CAR_ID = " + String.valueOf(carId) + " " +
					"AND RECORD_ID = " + String.valueOf(rowId) + ";";

		ret = db.rawQuery(sql, null);
		ret.moveToFirst();

		return ret;
	}

	/**
	 * 指定した期間の給油量合計を返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、給油情報を取得するクルマのCAR_ID
	 * @param startJd double型、検索範囲の最初を示すユリウス通日
	 * @param endJd double型、検索範囲の最後を示すユリウス通日
	 * @return float型、指定した期間の給油量合計
	 */
	protected float getSubtotalOfRefuelById(SQLiteDatabase db, int carId, double startJd, double endJd) {
		float ret = 0;
		Cursor q;

		// SQLを組み立てる
		String sql = "SELECT SUM(LUB_AMOUNT) FROM LUB_MASTER " +
					"WHERE CAR_ID = " + String.valueOf(carId) + " " +
					"AND REFUEL_DATE BETWEEN " + String.valueOf(startJd) + " AND " + String.valueOf(endJd) + ";";
		q = db.rawQuery(sql, null);
		q.moveToFirst();

		// Cursorから値を返す
		ret = q.getFloat(0);
		q.close();

		return ret;
	}

	/**
	 * そのクルマの累計給油量を返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId SQLiteDatabase型、操作するDBインスタンス
	 * @return float型、そのクルマの累計給油量
	 */
	protected float getTotalOfRefuelById(SQLiteDatabase db, int carId) {
		float ret = 0;
		Cursor q;

		// SQL共通部分
		String selection = "CAR_ID = ?";
		String[] selectionArgs = {String.valueOf(carId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		String[] columns = {"SUM(LUB_AMOUNT)"};

		q = db.query(LUB_MASTER, columns, selection, selectionArgs, groupBy, having, orderBy);
		q.moveToFirst();

		// Cursorから値を返す
		//   Cursorの中身がnull（＝該当レコードなし）の場合は、0を返す
		if (q.isNull(0)) {
			ret = 0;
		} else {
			ret = q.getFloat(0);
		}

		q.close();
		return ret;
	}

	/**
	 * 指定したクルマの、指定した期間の燃費記録を返す。トリップメーターが0の場合は、統計から除外する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、給油情報を取得するクルマのCAR_ID
	 * @param startJd double型、検索範囲の最初を示すユリウス通日
	 * @param endJd double型、検索範囲の最後を示すユリウス通日
	 * @return float型、指定した期間の燃費
	 */
	protected float getSubtotalOfMileageById(SQLiteDatabase db, int carId, double startJd, double endJd) {
		float ret = 0;
		Cursor q;

		// SQLを組み立てる
		String sql = "SELECT SUM(LUB_AMOUNT), SUM(TRIPMETER) FROM LUB_MASTER " +
					"WHERE CAR_ID = " + String.valueOf(carId) + " " +
					"AND TRIPMETER > 0 " +
					"AND REFUEL_DATE BETWEEN " + String.valueOf(startJd) + " AND " + String.valueOf(endJd) + ";";
		q = db.rawQuery(sql, null);
		q.moveToFirst();

		// Cursorから値を取り出してCursorを閉じる
		float lubAmount = q.getFloat(0);
		float trip = q.getFloat(1);

		// 燃費を計算する
		//   Cursorの中身がnull（＝該当レコードなし）の場合は、0を返す
		if (q.isNull(0)) {
			ret = 0;
		} else {
			ret = trip / lubAmount;
		}
		Log.i("getSubtotalOfMileageById", "current Subtotal of mileage is " + String.valueOf(ret));

		q.close();
		return ret;
	}

	/**
	 * 指定したクルマの、指定した期間のランニングコストを返す。トリップメーターが0の場合は、統計から除外する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、給油情報を取得するクルマのCAR_ID
	 * @param startJd double型、検索範囲の最初を示すユリウス通日
	 * @param endJd double型、検索範囲の最後を示すユリウス通日
	 * @return float型、指定した期間のランニングコスト
	 */
	protected float getSubtotalOfRunningCostsById(SQLiteDatabase db, int carId, double startJd, double endJd) {
		float ret = 0;
		Cursor q;

		// SQLを組み立てる
		String sql = "SELECT SUM(LUB_AMOUNT), SUM(TRIPMETER), AVG(UNIT_PRICE) FROM LUB_MASTER " +
					"WHERE CAR_ID = " + String.valueOf(carId) + " " +
					"AND TRIPMETER > 0 " +
					"AND REFUEL_DATE BETWEEN " + String.valueOf(startJd) + " AND " + String.valueOf(endJd) + ";";
		q = db.rawQuery(sql, null);
		q.moveToFirst();

		// Cursorから値を取り出してCursorを閉じる
		float lubAmount = q.getFloat(0);
		float trip = q.getFloat(1);
		float price = q.getFloat(2);

		// 燃費を計算する
		//   Cursorの中身がnull（＝該当レコードなし）の場合は、0を返す
		if (q.isNull(0)) {
			ret = 0;
		} else {
			ret = lubAmount * price / trip;
		}
		Log.i("getSubtotalOfRunningCostsById", "current Subtotal of Running costs is " + String.valueOf(ret));

		q.close();
		return ret;
	}

	/**
	 * そのクルマの最初の給油実施日を、ユリウス通日で返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、給油情報を取得するクルマのCAR_ID
	 * @return そのクルマの最初の給油実施日を表すユリウス通日
	 */
	protected double getOldestRefuelDateById(SQLiteDatabase db, int carId) {
		double ret = 0;
		Cursor q;

		// SQL共通部分
		String selection = "CAR_ID = ?";
		String[] selectionArgs = {String.valueOf(carId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		String[] columns = {"MIN(REFUEL_DATE)"};

		q = db.query(LUB_MASTER, columns, selection, selectionArgs, groupBy, having, orderBy);
		q.moveToFirst();

		ret = q.getDouble(0);
		q.close();
		return ret;
	}

	/**
	 * そのクルマの最後の給油実施日を、ユリウス通日で返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、給油情報を取得するクルマのCAR_ID
	 * @return そのクルマの最後の給油実施日を表すユリウス通日
	 */
	protected double getLatestRefuelDateById(SQLiteDatabase db, int carId) {
		double ret = 0;
		Cursor q;

		// SQL共通部分
		String selection = "CAR_ID = ?";
		String[] selectionArgs = {String.valueOf(carId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		String[] columns = {"MAX(REFUEL_DATE)"};

		q = db.query(LUB_MASTER, columns, selection, selectionArgs, groupBy, having, orderBy);
		q.moveToFirst();

		ret = q.getDouble(0);
		q.close();
		return ret;
	}

	/**
	 * レコード修正のために、特定のRECORD_IDの給油記録を呼び出す。
	 * @param  SQLiteDatabase型、操作するDBインスタンス
	 * @param recordId int型、給油記録を取得するRECORD_ID
	 * @return 編集する給油レコード(1レコード分)
	 */
	protected Cursor getLUBRecordByRecordId(SQLiteDatabase db, int recordId) {
		Cursor q;

		// SQL共通部分
		String selection = "RECORD_ID = ?";
		String[] selectionArgs = {String.valueOf(recordId)};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		// columnsを空にして、1レコード丸ごと引っこ抜く
		String[] columns = null;

		q = db.query(LUB_MASTER, columns, selection, selectionArgs, groupBy, having, orderBy);
		q.moveToFirst();

		return q;
	}

	/**
	 * 現在のテーブルデータを丸ごと取得する（バックアップ用）。
	 * @param tableName String型、テーブルデータを取得するテーブル名
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return Cursor型、取得したレコードのCursorオブジェクト
	 */
	protected Cursor getWholeRecords(String tableName, SQLiteDatabase db) {
		Cursor q;

		// SQL共通部分
		String selection = null;
		String[] selectionArgs = {};
		String groupBy = null;
		String having = null;
		String orderBy = null;

		// columnsを空にして、1レコード丸ごと引っこ抜く
		String[] columns = null;

		q = db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy);
		q.moveToFirst();

		return q;
	}

	/**
	 * テキストファイルからSQLiteにテーブル情報をインポートする。
	 * @param importFilename String型、インポートするファイルの名称
	 * @param tableName String型、インポートするテーブル名称
	 * @param separator String型、セパレータ記号、nullを指定すると「,」を仮定する
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 */
	protected void importTableFromFile(String importFilename, String tableName, String separator, SQLiteDatabase db) {
		// システム定義されている改行コードを取得する
		String lineSeparator;
		try {
			lineSeparator = System.getProperty("line.separator");
		} catch (SecurityException se) {
			// セキュリティ権限で取得できなかったときは、CRLFにする
			se.printStackTrace();
			lineSeparator = "\r\n";
		}

		// separator引数がnull、または""の場合は、「,」を仮定する
		String sp;
		if (TextUtils.isEmpty(separator)) {
			sp = ",";
		} else {
			sp = separator;
		}

		// SQLiteに引き渡すSQLステートメントを組み立てる
		String sql = ".sepatator " + sp + lineSeparator
				+ ".import " + importFilename + " " + tableName;

		try {
			db.execSQL(sql);
		} catch (SQLException sqle) {
			// TODO 自動生成された catch ブロック
			sqle.printStackTrace();
		}
	}
}
