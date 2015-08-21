package com.utility;

import java.util.ArrayList;
import com.bean.ShipmentItem;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

	static final String KEY_ROW_ID = "id";
    static final String KEY_RECORD_TYPE = "recordType";

	static final String KEY_ShipmentId = "shipmentId";
	static final String KEY_shipmentType = "shipmentType";
	static final String KEY_ShipmentTrakingNo = "shipmentTrakingNo";
	static final String KEY_ShipmentVendorRefNo = "shipmentVendorRefNo";
	static final String KEY_ShipmentDescription = "shipmentDescription";
	static final String KEY_ShipmentClientCode = "shipmentClientCode";
	static final String KEY_ShipmentNoOfCheques = "shipmentNoOfCheques";
	static final String KEY_ShipmentNoOfPackages = "shipmentNoOfPackages";
	static final String KEY_ShipmentTotalAmount = "shipmentTotalAmount";
	static final String KEY_ShipmentCashReceived = "shipmentCashReceived";
	static final String KEY_ShipmentStatus = "shipmentStatus";
	static final String KEY_ShipmentPickupDate = "shipmentPickupDate";
	static final String KEY_ShipmentPickupTime = "shipmentPickupTime";

    static final String KEY_PickupId = "pickupId";
    static final String KEY_PickupName = "pickupName";
    static final String KEY_PickupContactNo = "pickupContactNo";
    static final String KEY_PickupEmailId = "pickupEmailId";
    static final String KEY_PickupHomeName = "pickupHomeName";
    static final String KEY_PickupStreetName = "pickupStreetName";
    static final String KEY_PickupLocationName = "pickupLocationName";
    static final String KEY_PickupCityName = "pickupCityName";
    static final String KEY_PickupStateName = "pickupStateName";
    static final String KEY_PickupCountryName = "pickupCountryName";
    static final String KEY_PickupPincode = "pickupPincode";
    static final String KEY_PickupLandmark = "pickupLandmark";
    static final String KEY_PickupRemarks = "pickupRemarks";
    static final String KEY_PickupLongValue = "pickupLongValue";
    static final String KEY_PickupLatValue = "pickupLatValue";

    static final String KEY_DeliveryId = "deliveryId";
    static final String KEY_DeliveryName = "deliveryName";
    static final String KEY_DeliveryContactNo = "deliveryContactNo";
    static final String KEY_DeliveryEmailId = "deliveryEmailId";
    static final String KEY_DeliveryHomeName = "deliveryHomeName";
    static final String KEY_DeliveryStreetName = "deliveryStreetName";
    static final String KEY_DeliveryLocationName = "deliveryLocationName";
    static final String KEY_DeliveryCityName = "deliveryCityName";
    static final String KEY_DeliveryStateName = "deliveryStateName";
    static final String KEY_DeliveryCountryName = "deliveryCountryName";
    static final String KEY_DeliveryPincode = "deliveryPincode";
    static final String KEY_DeliveryLandmark = "deliveryLandmark";
    static final String KEY_DeliveryRemarks = "deliveryRemarks";
    static final String KEY_DeliveryLongValue = "deliveryLongValue";
    static final String KEY_DeliveryLatValue = "deliveryLatValue";

	static final String TAG = "DBAdapter";
	static final String DATABASE_NAME = "ShipmentMasterDB";
	static final String DATABASE_TABLE = "ShipmentTable";
	static final int DATABASE_VERSION = 1;

	static final String DATABASE_CREATE_TABLE = "create table ShipmentTable (id integer primary key autoincrement, recordType text not null, "
			+ "shipmentId text not null, shipmentType text not null, shipmentTrakingNo text not null, shipmentVendorRefNo text not null, shipmentDescription text not null, shipmentClientCode text not null, shipmentNoOfCheques text not null, shipmentNoOfPackages text not null, shipmentTotalAmount text not null, shipmentCashReceived text not null, shipmentStatus text not null, shipmentPickupDate text not null, shipmentPickupTime text not null, "
            + "pickupId text not null, pickupName text not null, pickupContactNo text not null, pickupEmailId text not null, pickupHomeName text not null, pickupStreetName text not null, pickupLocationName text not null, pickupCityName text not null, pickupStateName text not null, pickupCountryName text not null, pickupPincode text not null, pickupLandmark text not null, pickupRemarks text not null, pickupLongValue text not null, pickupLatValue text not null,"
            + "deliveryId text not null, deliveryName text not null, deliveryContactNo text not null, deliveryEmailId text not null, deliveryHomeName text not null, deliveryStreetName text not null, deliveryLocationName text not null, deliveryCityName text not null, deliveryStateName text not null, deliveryCountryName text not null, deliveryPincode text not null, deliveryLandmark text not null, deliveryRemarks text not null, deliveryLongValue text not null, deliveryLatValue text not null);";

	final Context context;

	DatabaseHelper DBHelper;

	SQLiteDatabase db;

    String[] col = new String[] { KEY_ROW_ID, KEY_RECORD_TYPE,
            KEY_ShipmentId, KEY_shipmentType, KEY_ShipmentTrakingNo, KEY_ShipmentVendorRefNo, KEY_ShipmentDescription, KEY_ShipmentClientCode, KEY_ShipmentNoOfCheques, KEY_ShipmentNoOfPackages, KEY_ShipmentTotalAmount, KEY_ShipmentCashReceived, KEY_ShipmentStatus, KEY_ShipmentPickupDate, KEY_ShipmentPickupTime ,
            KEY_PickupId, KEY_PickupName, KEY_PickupContactNo, KEY_PickupEmailId, KEY_PickupHomeName, KEY_PickupStreetName, KEY_PickupLocationName, KEY_PickupCityName, KEY_PickupStateName, KEY_PickupCountryName, KEY_PickupPincode, KEY_PickupLandmark, KEY_PickupRemarks, KEY_PickupLongValue, KEY_PickupLatValue,
            KEY_DeliveryId, KEY_DeliveryName, KEY_DeliveryContactNo, KEY_DeliveryEmailId, KEY_DeliveryHomeName, KEY_DeliveryStreetName, KEY_DeliveryLocationName, KEY_DeliveryCityName, KEY_DeliveryStateName, KEY_DeliveryCountryName, KEY_DeliveryPincode, KEY_DeliveryLandmark, KEY_DeliveryRemarks, KEY_DeliveryLongValue, KEY_DeliveryLatValue};

	public DBAdapter(Context ctx) {

		this.context = ctx;

		DBHelper = new DatabaseHelper(context);

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {

			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			try {

				db.execSQL(DATABASE_CREATE_TABLE);

			} catch (SQLException e) {

				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.v(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS ShipmentTable");

			onCreate(db);
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {

			Log.v(TAG, "Downgrading database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS ShipmentTable");

			onCreate(db);
		}

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// ---opens the database---
	public DBAdapter open() throws SQLException {

		DBHelper = new DatabaseHelper(context);

		db = DBHelper.getWritableDatabase();

		return this;

	}

	// ---closes the database---
	public void close() {

		DBHelper.close();

	}

	// ---insert a contact into the database---
	public long insertValue(String recordType, String shipmentId, String shipmentType, String shipmentTrakingNo, String shipmentVendorRefNo,String shipmentDescription, String shipmentClientCode,String shipmentNoOfCheques, String shipmentNoOfPackages,String shipmentTotalAmount, String shipmentCashReceived,String shipmentStatus, String shipmentPickupDate, String shipmentPickupTime,
            String pickupId, String pickupName, String pickupContactNo,String pickupEmailId, String pickupHomeName,String pickupStreetName, String pickupLocationName,String pickupCityName, String pickupStateName,String pickupCountryName, String pickupPincode, String pickupLandmark, String pickupRemarks, String pickupLongValue, String pickupLatValue,
            String deliveryId, String deliveryName, String deliveryContactNo,String deliveryEmailId, String deliveryHomeName,String deliveryStreetName, String deliveryLocationName,String deliveryCityName, String deliveryStateName,String deliveryCountryName, String deliveryPincode, String deliveryLandmark, String deliveryRemarks, String deliveryLongValue, String deliveryLatValue) {

		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_RECORD_TYPE, recordType);
		initialValues.put(KEY_ShipmentId, shipmentId);
		initialValues.put(KEY_shipmentType, shipmentType);
		initialValues.put(KEY_ShipmentTrakingNo, shipmentTrakingNo);
		initialValues.put(KEY_ShipmentVendorRefNo, shipmentVendorRefNo);
		initialValues.put(KEY_ShipmentDescription, shipmentDescription);
		initialValues.put(KEY_ShipmentClientCode, shipmentClientCode);
		initialValues.put(KEY_ShipmentNoOfCheques, shipmentNoOfCheques);
		initialValues.put(KEY_ShipmentNoOfPackages, shipmentNoOfPackages);
		initialValues.put(KEY_ShipmentTotalAmount, shipmentTotalAmount);
		initialValues.put(KEY_ShipmentCashReceived, shipmentCashReceived);
		initialValues.put(KEY_ShipmentStatus, shipmentStatus);
		initialValues.put(KEY_ShipmentPickupDate, shipmentPickupDate);
		initialValues.put(KEY_ShipmentPickupTime, shipmentPickupTime);

        initialValues.put(KEY_PickupId, pickupId);
        initialValues.put(KEY_PickupName, pickupName);
        initialValues.put(KEY_PickupContactNo, pickupContactNo);
        initialValues.put(KEY_PickupEmailId, pickupEmailId);
        initialValues.put(KEY_PickupHomeName, pickupHomeName);
        initialValues.put(KEY_PickupStreetName, pickupStreetName);
        initialValues.put(KEY_PickupLocationName, pickupLocationName);
        initialValues.put(KEY_PickupCityName, pickupCityName);
        initialValues.put(KEY_PickupStateName, pickupStateName);
        initialValues.put(KEY_PickupCountryName, pickupCountryName);
        initialValues.put(KEY_PickupPincode, pickupPincode);
        initialValues.put(KEY_PickupLandmark, pickupLandmark);
        initialValues.put(KEY_PickupRemarks, pickupRemarks);
        initialValues.put(KEY_PickupLongValue, pickupLongValue);
        initialValues.put(KEY_PickupLatValue, pickupLatValue);

        initialValues.put(KEY_DeliveryId, deliveryId);
        initialValues.put(KEY_DeliveryName, deliveryName);
        initialValues.put(KEY_DeliveryContactNo, deliveryContactNo);
        initialValues.put(KEY_DeliveryEmailId, deliveryEmailId);
        initialValues.put(KEY_DeliveryHomeName, deliveryHomeName);
        initialValues.put(KEY_DeliveryStreetName, deliveryStreetName);
        initialValues.put(KEY_DeliveryLocationName, deliveryLocationName);
        initialValues.put(KEY_DeliveryCityName, deliveryCityName);
        initialValues.put(KEY_DeliveryStateName, deliveryStateName);
        initialValues.put(KEY_DeliveryCountryName, deliveryCountryName);
        initialValues.put(KEY_DeliveryPincode, deliveryPincode);
        initialValues.put(KEY_DeliveryLandmark, deliveryLandmark);
        initialValues.put(KEY_DeliveryRemarks, deliveryRemarks);
        initialValues.put(KEY_DeliveryLongValue, deliveryLongValue);
        initialValues.put(KEY_DeliveryLatValue, deliveryLatValue);

		return db.insert(DATABASE_TABLE, null, initialValues);

	}

    public int updateRecord(String shipmentId, String newType) {

        ContentValues initialValues = new ContentValues();

        initialValues.put(KEY_RECORD_TYPE, newType);

        return db.update(DATABASE_TABLE, initialValues, KEY_ShipmentId + "='" + shipmentId + "'", null);

    }

    public int updateStatus(String shipmentId) {

        ContentValues initialValues = new ContentValues();

        initialValues.put(KEY_ShipmentStatus, "703");

        return db.update(DATABASE_TABLE, initialValues, KEY_ShipmentId + "='" + shipmentId + "'", null);

    }

    // ---deletes all record---
    public boolean deleteAllRecord() {

        return db.delete(DATABASE_TABLE, null, null) > 0;

    }

	// ---deletes a particular record---
	public boolean deleteRecord(String shipmentId) {
		return db.delete(DATABASE_TABLE, KEY_ShipmentId + "=" + shipmentId, null) > 0;
	}

	// ---retrieves all records---

	public ArrayList<ShipmentItem> getRecords(String getType) {
		// TODO Auto-generated method stub

        ArrayList<ShipmentItem> shipmentList = new ArrayList<ShipmentItem>();
        shipmentList.clear();

        Cursor c;
        c = db.rawQuery("select * from ShipmentTable where recordType = '" + getType + "'" , null);

		int row_id_pos = c.getColumnIndex(KEY_ROW_ID);
		int type_pos = c.getColumnIndex(KEY_RECORD_TYPE);

		int shipmentId_pos = c.getColumnIndex(KEY_ShipmentId);
		int shipmentType_pos = c.getColumnIndex(KEY_shipmentType);
		int shipmentTrakingNo_pos = c.getColumnIndex(KEY_ShipmentTrakingNo);
        int shipmentVendorRefNo_pos = c.getColumnIndex(KEY_ShipmentVendorRefNo);
        int shipmentDescription_pos = c.getColumnIndex(KEY_ShipmentDescription);
        int shipmentClientCode_pos = c.getColumnIndex(KEY_ShipmentClientCode);
        int shipmentNoOfCheques_pos = c.getColumnIndex(KEY_ShipmentNoOfCheques);
        int shipmentNoOfPackages_pos = c.getColumnIndex(KEY_ShipmentNoOfPackages);
        int shipmentTotalAmount_pos = c.getColumnIndex(KEY_ShipmentTotalAmount);
        int shipmentCashReceived_pos = c.getColumnIndex(KEY_ShipmentCashReceived);
        int shipmentStatus_pos = c.getColumnIndex(KEY_ShipmentStatus);
        int shipmentPickupDate_pos = c.getColumnIndex(KEY_ShipmentPickupDate);
        int shipmentPickupTime_pos = c.getColumnIndex(KEY_ShipmentPickupTime);

        int pickupId_pos = c.getColumnIndex(KEY_PickupId);
        int pickupName_pos = c.getColumnIndex(KEY_PickupName);
        int pickupContactNo_pos = c.getColumnIndex(KEY_PickupContactNo);
        int pickupEmailId_pos = c.getColumnIndex(KEY_PickupEmailId);
        int pickupHomeName_pos = c.getColumnIndex(KEY_PickupHomeName);
        int pickupStreetName_pos = c.getColumnIndex(KEY_PickupStreetName);
        int pickupLocationName_pos = c.getColumnIndex(KEY_PickupLocationName);
        int pickupCityName_pos = c.getColumnIndex(KEY_PickupCityName);
        int pickupStateName_pos = c.getColumnIndex(KEY_PickupStateName);
        int pickupCountryName_pos = c.getColumnIndex(KEY_PickupCountryName);
        int pickupPincode_pos = c.getColumnIndex(KEY_PickupPincode);
        int pickupLandmark_pos = c.getColumnIndex(KEY_PickupLandmark);
        int pickupRemarks_pos = c.getColumnIndex(KEY_PickupRemarks);
        int pickupLongValue_pos = c.getColumnIndex(KEY_PickupLongValue);
        int pickupLatValue_pos = c.getColumnIndex(KEY_PickupLatValue);

        int deliveryId_pos = c.getColumnIndex(KEY_DeliveryId);
        int deliveryName_pos = c.getColumnIndex(KEY_DeliveryName);
        int deliveryContactNo_pos = c.getColumnIndex(KEY_DeliveryContactNo);
        int deliveryEmailId_pos = c.getColumnIndex(KEY_DeliveryEmailId);
        int deliveryHomeName_pos = c.getColumnIndex(KEY_DeliveryHomeName);
        int deliveryStreetName_pos = c.getColumnIndex(KEY_DeliveryStreetName);
        int deliveryLocationName_pos = c.getColumnIndex(KEY_DeliveryLocationName);
        int deliveryCityName_pos = c.getColumnIndex(KEY_DeliveryCityName);
        int deliveryStateName_pos = c.getColumnIndex(KEY_DeliveryStateName);
        int deliveryCountryName_pos = c.getColumnIndex(KEY_DeliveryCountryName);
        int deliveryPincode_pos = c.getColumnIndex(KEY_DeliveryPincode);
        int deliveryLandmark_pos = c.getColumnIndex(KEY_DeliveryLandmark);
        int deliveryRemarks_pos = c.getColumnIndex(KEY_DeliveryRemarks);
        int deliveryLongValue_pos = c.getColumnIndex(KEY_DeliveryLongValue);
        int deliveryLatValue_pos = c.getColumnIndex(KEY_DeliveryLatValue);


		for (c.moveToFirst(); !(c.isAfterLast()); c.moveToNext()) {

            ShipmentItem shipmentItem = new ShipmentItem();

			shipmentItem.setShipmentId(c.getString(shipmentId_pos));
            shipmentItem.setShipmentType(c.getString(shipmentType_pos));
            shipmentItem.setShipmentTrakingNo(c.getString(shipmentTrakingNo_pos));
            shipmentItem.setShipmentVendorRefNo(c.getString(shipmentVendorRefNo_pos));
            shipmentItem.setShipmentDescription(c.getString(shipmentDescription_pos));
            shipmentItem.setShipmentClientCode(c.getString(shipmentClientCode_pos));
            shipmentItem.setShipmentNoOfCheques(c.getString(shipmentNoOfCheques_pos));
            shipmentItem.setShipmentNoOfPackages(c.getString(shipmentNoOfPackages_pos));
            shipmentItem.setShipmentTotalAmount(c.getString(shipmentTotalAmount_pos));
            shipmentItem.setShipmentCashReceived(c.getString(shipmentCashReceived_pos));
            shipmentItem.setShipmentStatus(c.getString(shipmentStatus_pos));
            shipmentItem.setShipmentPickupDate(c.getString(shipmentPickupDate_pos));
            shipmentItem.setShipmentPickupTime(c.getString(shipmentPickupTime_pos));

            shipmentItem.setPickupId(c.getString(pickupId_pos));
            shipmentItem.setPickupName(c.getString(pickupName_pos));
            shipmentItem.setPickupContactNo(c.getString(pickupContactNo_pos));
            shipmentItem.setPickupEmailId(c.getString(pickupEmailId_pos));
            shipmentItem.setPickupHomeName(c.getString(pickupHomeName_pos));
            shipmentItem.setPickupStreetName(c.getString(pickupStreetName_pos));
            shipmentItem.setPickupLocationName(c.getString(pickupLocationName_pos));
            shipmentItem.setPickupCityName(c.getString(pickupCityName_pos));
            shipmentItem.setPickupStateName(c.getString(pickupStateName_pos));
            shipmentItem.setPickupCountryName(c.getString(pickupCountryName_pos));
            shipmentItem.setPickupPincode(c.getString(pickupPincode_pos));
            shipmentItem.setPickupLandmark(c.getString(pickupLandmark_pos));
            shipmentItem.setPickupRemarks(c.getString(pickupRemarks_pos));
            shipmentItem.setPickupLongValue(c.getString(pickupLongValue_pos));
            shipmentItem.setPickupLatValue(c.getString(pickupLatValue_pos));

            shipmentItem.setDeliveryId(c.getString(deliveryId_pos));
            shipmentItem.setDeliveryName(c.getString(deliveryName_pos));
            shipmentItem.setDeliveryContactNo(c.getString(deliveryContactNo_pos));
            shipmentItem.setDeliveryEmailId(c.getString(deliveryEmailId_pos));
            shipmentItem.setDeliveryHomeName(c.getString(deliveryHomeName_pos));
            shipmentItem.setDeliveryStreetName(c.getString(deliveryStreetName_pos));
            shipmentItem.setDeliveryLocationName(c.getString(deliveryLocationName_pos));
            shipmentItem.setDeliveryCityName(c.getString(deliveryCityName_pos));
            shipmentItem.setDeliveryStateName(c.getString(deliveryStateName_pos));
            shipmentItem.setDeliveryCountryName(c.getString(deliveryCountryName_pos));
            shipmentItem.setDeliveryPincode(c.getString(deliveryPincode_pos));
            shipmentItem.setDeliveryLandmark(c.getString(deliveryLandmark_pos));
            shipmentItem.setDeliveryRemarks(c.getString(deliveryRemarks_pos));
            shipmentItem.setDeliveryLongValue(c.getString(deliveryLongValue_pos));
            shipmentItem.setDeliveryLatValue(c.getString(deliveryLatValue_pos));

            shipmentList.add(shipmentItem);
            Log.v("ShipmentList", String.valueOf(shipmentList.size()));

		}
		c.close();

		return shipmentList;

	}

}
