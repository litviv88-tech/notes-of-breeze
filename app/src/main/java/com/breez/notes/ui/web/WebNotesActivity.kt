package com.breez.notes.ui.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.breez.notes.bridge.BreezJsBridge
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebNotesActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(BreezJsBridge(this@WebNotesActivity), BreezJsBridge.JS_NAME)
            loadUrl("file:///android_asset/index.html#notes")
        }
        setContentView(webView)
    }
}
