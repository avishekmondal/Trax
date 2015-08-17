package com.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Pref {

	private SharedPreferences spref;
	private static final String PREF_FILE = "com.count";
	private Editor _editorSpref;

	public SharedPreferences getSharedPreferencesInstance() {
		return spref;
	}

	public Editor getSharedPreferencesEditorInstance() {
		return _editorSpref;
	}

	@SuppressLint("CommitPrefEdits")
	public Pref(Context _thisContext) {
		// TODO Auto-generated constructor stub
		//this._activity = (Activity)_thisContext;
		spref = _thisContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
		_editorSpref = spref.edit();
	}

    public void saveDeviecId(String device_id) {
        _editorSpref.putString("device_id", device_id);
        _editorSpref.commit();
    }

    public String getDeviceId() {
        return spref.getString("device_id", "");
    }

    public void saveDate(String date) {
        _editorSpref.putString("date", date);
        _editorSpref.commit();
    }

    public String getDate() {
        return spref.getString("date", "");
    }

    public void saveLoginFlag(String login_flag) {
        _editorSpref.putString("login_flag", login_flag);
        _editorSpref.commit();
    }

    public String getLoginFlag() {
        return spref.getString("login_flag", "");
    }

    public void saveMobileNo(String mobile) {
        _editorSpref.putString("mobile", mobile);
        _editorSpref.commit();
    }

    public String getMobileNo() {
        return spref.getString("mobile", "");
    }

    public void saveAgentId(String id) {
        _editorSpref.putString("id", id);
        _editorSpref.commit();
    }

    public String getAgentId() {
        return spref.getString("id", "");
    }

    public void saveAccessToken(String access_token) {
        _editorSpref.putString("access_token", access_token);
        _editorSpref.commit();
    }

    public String getAccessToken() {
        return spref.getString("access_token", "");
    }

    public void saveName(String name) {
        _editorSpref.putString("name", name);
        _editorSpref.commit();
    }

    public String getName() {
        return spref.getString("name", "");
    }

    public void saveEmailId(String emailid) {
        _editorSpref.putString("emailid", emailid);
        _editorSpref.commit();
    }

    public String getEmailId() {
        return spref.getString("emailid", "");
    }

    public void saveAddress(String address) {
        _editorSpref.putString("address", address);
        _editorSpref.commit();
    }

    public String getAddress() {
        return spref.getString("address", "");
    }

    public void saveProfileImage(String image) {
        _editorSpref.putString("image", image);
        _editorSpref.commit();
    }

    public String getProfileImage() {
        return spref.getString("image", "");
    }

    public void saveIntransitShipmentId(String shipment_id) {
        _editorSpref.putString("shipment_id", shipment_id);
        _editorSpref.commit();
    }

    public String getIntransitShipmentId() {
        return spref.getString("shipment_id", "");
    }

}
