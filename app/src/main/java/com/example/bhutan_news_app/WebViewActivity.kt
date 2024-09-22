package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
class WebViewActivity: AppCompatActivity() {
    private lateinit var webView : WebView
    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // Getting the web view from the layout file
        webView = findViewById(R.id.webView)

        // Enabling the javascript in the webview
        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        // Making the url to open in the webView rather in the default broswer
        webView.webViewClient = WebViewClient()

        // Getting the url to passed from the main activity
        val url = intent.getStringExtra("url")
        if (url != null){
            webView.loadUrl(url)
        }
        //Injecting the javascript after loading the webview
        webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                //Injecting the custom javascript to remove the header and the footer
                webView.postDelayed({
                    webView.evaluateJavascript(
                        """
                        (function() {
                        // Hide the header
                        var header = document.querySelector('header'); 
                        if (header) {
                            header.style.display = 'none';
                        }
                        // Hide the footer
                        var footer = document.querySelector('footer'); 
                        if (footer) {
                            footer.style.display = 'none';
                        }
                        // Hide the nav
                        var nav = document.querySelector('nav');
                        if (nav){
                           nav.style.display = 'none';
                        }
                        // Hide the ads
                        var nav = document.querySelector('ads');
                        if (ads){
                           ads.style.display = 'none';
                        }
                    })();
                    """.trimIndent()
                    ){}
                },1000)
            }
        }
    }

}