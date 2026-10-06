package com.shokirjon.sonettube.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

interface YouTubePlayerController {
    fun exitFullscreen()
}

@Composable
fun YouTubePlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    onFullscreenChanged: (Boolean) -> Unit = {},
    onLoadError: () -> Unit = {},
    onControllerReady: (YouTubePlayerController) -> Unit = {},
) {
    val latestFullscreenCallback by rememberUpdatedState(onFullscreenChanged)
    val latestLoadErrorCallback by rememberUpdatedState(onLoadError)
    val latestControllerCallback by rememberUpdatedState(onControllerReady)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            YouTubeWebViewContainer(context).also { created ->
                created.onFullscreenChanged = { latestFullscreenCallback(it) }
                created.onLoadError = { latestLoadErrorCallback() }
                created.loadVideo(videoId)
                latestControllerCallback(created)
            }
        },
        update = { view ->
            view.onFullscreenChanged = { latestFullscreenCallback(it) }
            view.onLoadError = { latestLoadErrorCallback() }
            view.loadVideo(videoId)
        },
        onRelease = { it.release() },
    )
}

@SuppressLint("SetJavaScriptEnabled")
private class YouTubeWebViewContainer(context: Context) : FrameLayout(context), YouTubePlayerController {
    private val webView = WebView(context)
    private var loadedVideoId: String? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var released = false
    var onFullscreenChanged: (Boolean) -> Unit = {}
    var onLoadError: () -> Unit = {}

    init {
        setBackgroundColor(Color.BLACK)
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        configureWebView()
    }

    override fun exitFullscreen() {
        hideCustomView()
    }

    fun loadVideo(videoId: String) {
        if (released || loadedVideoId == videoId) return
        if (!VIDEO_ID_PATTERN.matches(videoId)) {
            onLoadError()
            return
        }
        loadedVideoId = videoId
        // YouTube requires an identifying Referer for Android WebView embeds.
        webView.loadUrl(
            "https://www.youtube.com/embed/$videoId?autoplay=0&controls=1&fs=1&playsinline=1",
            mapOf("Referer" to "https://${context.packageName}/"),
        )
    }

    fun release() {
        if (released) return
        released = true
        hideCustomView()
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.removeAllViews()
        webView.destroy()
    }

    private fun configureWebView() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = false
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.scheme != "https") return true
                val host = request.url.host?.lowercase() ?: return true
                return host != "youtube.com" && !host.endsWith(".youtube.com") &&
                    host != "googlevideo.com" && !host.endsWith(".googlevideo.com")
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                if (request.isForMainFrame) {
                    Log.w(TAG, "YouTube player load error: ${error.errorCode}")
                    onLoadError()
                }
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                response: WebResourceResponse,
            ) {
                if (request.isForMainFrame) {
                    Log.w(TAG, "YouTube player HTTP error: ${response.statusCode}")
                    onLoadError()
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
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
                webView.visibility = INVISIBLE
                onFullscreenChanged(true)
            }

            override fun onHideCustomView() {
                hideCustomView()
            }
        }
    }

    private fun hideCustomView() {
        val view = customView ?: return
        (view.parent as? ViewGroup)?.removeView(view)
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        webView.visibility = VISIBLE
        onFullscreenChanged(false)
    }

    private companion object {
        const val TAG = "SonetTubePlayer"
        val VIDEO_ID_PATTERN = Regex("^[A-Za-z0-9_-]{11}$")
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is android.content.ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
