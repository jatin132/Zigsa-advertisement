package com.zigsaadvertisement

import android.content.Context
import android.content.SharedPreferences

class SessionManager (context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val LOCATION_ID = "location_id";
        const val LOCATION_UUID = "location_uuid";

        const val SHOW_SYNONYM = false; //synonym
        const val BG_COLOR = "#D9E3F0"
        const val TEXT_COLOR = "#555555"
        const val WEBVIEW_URL = "webview_url"


    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Function to save auth token
     */
    fun saveWebViewURL(webviewurl: String) {
        val editor = prefs.edit()
        editor.putString(WEBVIEW_URL, webviewurl)
        editor.apply()
    }

    /**
     * Function to save location_id
     */
    fun saveLocationId(location_id: Int) {
        val editor = prefs.edit()
        editor.putInt(LOCATION_ID, location_id)
        editor.apply()
    }

    fun saveLocationUUID(location_uuid: String) {
        val editor = prefs.edit()
        editor.putString(LOCATION_UUID, location_uuid)
        editor.apply()
    }

    /**
     * Function to save for display customer name or synonym
     */
    fun saveShowSynonym(show_name: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(SHOW_SYNONYM.toString(), show_name)
        editor.apply()
    }

    /**
     * Function to save background color
     */
    fun saveBackgroundColor(bg_color: String) {
        val editor = prefs.edit()
        editor.putString(BG_COLOR, bg_color)
        editor.apply()
    }

    /**
     * Function to save text color
     */
    fun saveTextColor(text_color: String) {
        val editor = prefs.edit()
        editor.putString(TEXT_COLOR, text_color)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Function to fetch location id
     */
    fun getLocationId(): Int? {
        return prefs.getInt(LOCATION_ID, 0)
    }

    /**
     * Function to fetch location uuid
     */
    fun getLocationUUID(): String? {
        return prefs.getString(LOCATION_UUID, null)
    }

    /**
     * Function to fetch location id
     */
    fun getWebviewURL(): String? {
        return prefs.getString(WEBVIEW_URL, null)
    }


    /**
     * Function to fetch auth token
     */
    fun getShowSynonym(): Boolean {
        return prefs.getBoolean(SHOW_SYNONYM.toString(), false)
    }

    /**
     * Function to get background color
     */
    fun getBackgroundColor(): String? {
        return prefs.getString(BG_COLOR, null)
    }

    /**
     * Function to get text color
     */
    fun getTextColor(): String? {
        return prefs.getString(TEXT_COLOR, null)
    }


    /**
     * Function to remove store data  // auth token
     */
    fun removeStorage(): Boolean {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
        return false //"No Token"
    }


}