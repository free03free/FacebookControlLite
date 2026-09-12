package com.salim.facebookcontrol

import android.content.Context

object Prefs {
    private const val FILE = "control"
    private fun p(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    const val LIKE = "like"
    const val COMMENTS = "comments"
    const val SHARE = "share"
    const val FOLLOW = "follow"
    const val PROFILES = "profiles"
    const val MESSENGER = "messenger"
    const val STORIES = "stories"
    const val REELS = "reels"
    const val SEARCH = "search"
    const val POST = "post"
    const val UPLOAD = "upload"
    const val MARKETPLACE = "marketplace"
    const val GROUPS = "groups"
    const val ALL_BUTTONS = "all_buttons"
    const val CUSTOM_JS_DISABLED = "custom_js_disabled"
    const val CUSTOM_JS = "custom_js"
    const val CUSTOM_CSS = "custom_css"
    const val PIN_HASH = "pin_hash"

    fun get(c: Context, key: String) = p(c).getBoolean(key, false)
    fun set(c: Context, key: String, value: Boolean) = p(c).edit().putBoolean(key, value).apply()
    fun customJs(c: Context): String? = if (get(c, CUSTOM_JS_DISABLED)) null else p(c).getString(CUSTOM_JS, null)
    fun customCss(c: Context): String = p(c).getString(CUSTOM_CSS, "") ?: ""
    fun pin(c: Context): String? = p(c).getString(PIN_HASH, null)
    fun setPin(c: Context, hash: String) = p(c).edit().putString(PIN_HASH, hash).apply()
}
