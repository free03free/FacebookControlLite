package com.salim.facebookcontrol

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.userAgentString = web.settings.userAgentString + " FacebookControlLite/1.0"
        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (!isAuth(url)) applyControls(view)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (!isAuth(url)) applyControls(view)
            }
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                return if (!isAuth(url) && Controller.blockUrl(this@MainActivity, url)) true else false
            }
        }
        if (savedInstanceState == null) web.loadUrl("https://m.facebook.com/") else web.restoreState(savedInstanceState)
    }

    private fun isAuth(url: String?): Boolean = Controller.isAuth(url ?: "")

    private fun applyControls(view: WebView?) {
        view ?: return
        view.postDelayed({ Controller.inject(this, view) }, 100)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("⚙ الإعدادات").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title.toString().contains("الإعدادات")) {
            startActivity(android.content.Intent(this, SettingsActivity::class.java)); return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
