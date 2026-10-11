package com.shokirjon.sonettube.ui.youtube

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.zIndex

private const val YOUTUBE_HOME_URL = "https://m.youtube.com"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeBrowserScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> webView?.let { view ->
                    view.evaluateJavascript(
                        "document.querySelectorAll('video').forEach(video => video.pause())",
                        null,
                    )
                    view.onPause()
                    view.pauseTimers()
                }

                Lifecycle.Event.ON_RESUME -> webView?.let { view ->
                    view.resumeTimers()
                    view.onResume()
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun closeFullscreen(notifyWebView: Boolean) {
        val view = customView
        if (view != null) {
            (view.parent as? ViewGroup)?.removeView(view)
            customView = null
            val callback = customViewCallback
            customViewCallback = null
            if (notifyWebView) callback?.onCustomViewHidden()
        }
        isFullscreen = false
    }

    BackHandler(enabled = canGoBack || isFullscreen) {
        if (isFullscreen) {
            closeFullscreen(notifyWebView = true)
        } else {
            webView?.goBack()
            canGoBack = webView?.canGoBack() == true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { webContext ->
                WebView(webContext).also { view ->
                    webView = view
                    view.settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = true
                        javaScriptCanOpenWindowsAutomatically = false
                        setSupportMultipleWindows(false)
                        allowFileAccess = false
                        allowContentAccess = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            safeBrowsingEnabled = true
                        }
                    }

                    CookieManager.getInstance().apply {
                        setAcceptCookie(true)
                        setAcceptThirdPartyCookies(view, true)
                    }

                    view.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean {
                            if (!request.isForMainFrame) return false
                            val uri = request.url
                            if (uri.isTrustedYouTubeUrl()) return false

                            if (uri.scheme == "https" || uri.scheme == "http") {
                                openExternalUrl(view.context, uri)
                            }
                            return true
                        }

                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: android.graphics.Bitmap?,
                        ) {
                            loadError = false
                            progress = 0
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            progress = 100
                            canGoBack = view?.canGoBack() == true
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError,
                        ) {
                            if (request.isForMainFrame) {
                                loadError = true
                                progress = 0
                            }
                        }

                        override fun onReceivedHttpError(
                            view: WebView,
                            request: WebResourceRequest,
                            response: WebResourceResponse,
                        ) {
                            if (request.isForMainFrame && response.statusCode >= 400) {
                                loadError = true
                                progress = 0
                            }
                        }
                    }

                    view.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                            canGoBack = view?.canGoBack() == true
                        }

                        override fun onShowCustomView(
                            view: View,
                            callback: CustomViewCallback,
                        ) {
                            if (customView != null) {
                                callback.onCustomViewHidden()
                                return
                            }
                            val decor = context.findActivity()?.window?.decorView as? ViewGroup
                            if (decor == null) {
                                callback.onCustomViewHidden()
                                return
                            }
                            customView = view
                            customViewCallback = callback
                            decor.addView(
                                view,
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                ),
                            )
                            isFullscreen = true
                        }

                        override fun onHideCustomView() {
                            closeFullscreen(notifyWebView = false)
                        }
                    }

                    view.loadUrl(YOUTUBE_HOME_URL)
                }
            },
            onRelease = { releasedView ->
                closeFullscreen(notifyWebView = true)
                if (webView === releasedView) webView = null
                releasedView.stopLoading()
                releasedView.loadUrl("about:blank")
                releasedView.removeAllViews()
                releasedView.destroy()
            },
        )

        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f),
            )
        }

        if (loadError) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "YouTube yuklanmadi. Internet aloqasini tekshirib, qayta urinib ko‘ring.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = {
                            loadError = false
                            progress = 0
                            webView?.reload()
                        },
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        Text("Qayta yuklash")
                    }
                }
            }
        }
    }
}

private fun Uri.isTrustedYouTubeUrl(): Boolean {
    if (scheme != "https") return false
    val hostName = host?.lowercase() ?: return false
    return TRUSTED_WEB_HOSTS.any { hostName == it || hostName.endsWith(".$it") }
}

private fun openExternalUrl(context: Context, uri: Uri) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

private val TRUSTED_WEB_HOSTS = setOf(
    "youtube.com",
    "youtu.be",
    "youtube-nocookie.com",
    "google.com",
    "googlevideo.com",
    "ytimg.com",
    "ggpht.com",
    "googleapis.com",
    "googleusercontent.com",
)

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
