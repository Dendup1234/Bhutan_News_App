package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
class WebViewActivity: AppCompatActivity() {
    private lateinit var webView : WebView
    private lateinit var progressBar : ProgressBar
    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // Getting the web view from the layout file
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

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
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                //Injecting the custom javascript to remove the header and the footer
                when {
                    url?.contains("thebhutanese.bt") == true ->{
                        injectBhutanese()
                    }
                    url?.contains("kuenselonline") == true ->{
                        injectKuensel()
                    }
                }

            }
        }
    }

    fun injectKuensel(){
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
                        var ads = document.querySelector('ads');
                        if (ads){
                           ads.style.display = 'none';
                        }
                    })();
                    """.trimIndent()
            ){
                // Hiding the prpgress bar that have been shown
                progressBar.visibility = View.GONE
            }

        },1000)
    }

    fun injectBhutanese(){
        webView.postDelayed({
            webView.evaluateJavascript(
                """
                    (function() {
                        // Hide the related_posts section
                        var relatedPostsSection = document.querySelector('#related_posts');
                        if (relatedPostsSection) {
                            relatedPostsSection.style.display = 'none';
                        }

                        // Hide the check-also-box section
                        var checkAlsoBoxSection = document.querySelector('#check-also-box');
                        if (checkAlsoBoxSection) {
                            checkAlsoBoxSection.style.display = 'none';
                        }
                        
                        // Hiding the reply
                        var comment = document.querySelector('#comments');
                        if (comment) {
                            comment.style.display = 'none';
                        }
                        
                        var sidebar = document.querySelector('#sidebar');
                        if (sidebar) {
                            sidebar.style.display = 'none';
                        }
                        
                        var themefooter = document.querySelector('#theme-footer');
                        if (themefooter) {
                            themefooter.style.display = 'none';
                        }
                        
                        var themeheader = document.querySelector('#theme-header');
                        if (themeheader) {
                            themeheader.style.display = 'none';
                        }
                        
                    })();
                    """.trimIndent()
            ){
                // Hiding the prpgress bar that have been shown
                progressBar.visibility = View.GONE
            }
        },1000)
    }

}