package com.shokirjon.sonettube.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

enum class PlaybackState {
    UNKNOWN,
    PLAYING,
    PAUSED,
    ENDED,
}

interface YouTubePlayerController {
    fun exitFullscreen()
}

@Composable
fun YouTubePlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    onFullscreenChanged: (Boolean) -> Unit = {},
    onPlaybackStateChanged: (PlaybackState) -> Unit = {},
    onControllerReady: (YouTubePlayerController) -> Unit = {},
) {
    val latestFullscreenCallback by rememberUpdatedState(onFullscreenChanged)
    val latestPlaybackCallback by rememberUpdatedState(onPlaybackStateChanged)
    val latestControllerCallback by rememberUpdatedState(onControllerReady)
    var container by remember { mutableStateOf<YouTubeWebViewContainer?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            YouTubeWebViewContainer(context).also { created ->
                container = created
                created.onFullscreenChanged = { latestFullscreenCallback(it) }
                created.onPlaybackStateChanged = { latestPlaybackCallback(it) }
                created.loadVideo(videoId)
                latestControllerCallback(created)
            }
        },
        update = { view ->
            view.onFullscreenChanged = { latestFullscreenCallback(it) }
            view.onPlaybackStateChanged = { latestPlaybackCallback(it) }
            view.loadVideo(videoId)
        },
    )

    DisposableEffect(container) {
        onDispose { container?.release() }
    }
}

@SuppressLint("SetJavaScriptEnabled", "DEPRECATION")
private class YouTubeWebViewContainer(context: Context) : FrameLayout(context), YouTubePlayerController {
    private val webView = WebView(context)
    private var loadedVideoId: String? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var released = false
    var onFullscreenChanged: (Boolean) -> Unit = {}
    var onPlaybackStateChanged: (PlaybackState) -> Unit = {}

    init {
        setBackgroundColor(Color.BLACK)
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        configureWebView()
    }

    override fun exitFullscreen() {
        hideCustomView()
    }

    fun loadVideo(videoId: String) {
        if (released || videoId.isBlank() || loadedVideoId == videoId) return
        loadedVideoId = videoId
        webView.loadUrl("file:///android_asset/youtube_player.html?videoId=${Uri.encode(videoId)}")
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

    fun release() {
        if (released) return
        released = true
        if (webView.parent != null) {
            (webView.parent as? ViewGroup)?.removeView(webView)
        }
        hideCustomView()
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
            allowFileAccess = true
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
        }
        webView.addJavascriptInterface(PlayerBridge(), "SonetTubeBridge")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val host = request.url.host?.lowercase() ?: return true
                return host != "youtube.com" && !host.endsWith(".youtube.com") &&
                    host != "googlevideo.com" && !host.endsWith(".googlevideo.com")
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                if (customView != null) {
                    callback.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                val decor = (context.findActivity()?.window?.decorView as? ViewGroup)
                decor?.addView(
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

    private inner class PlayerBridge {
        @JavascriptInterface
        fun onPlayerState(state: String) {
            val playbackState = when (state) {
                "playing" -> PlaybackState.PLAYING
                "paused" -> PlaybackState.PAUSED
                "ended" -> PlaybackState.ENDED
                else -> PlaybackState.UNKNOWN
            }
            post { onPlaybackStateChanged(playbackState) }
        }
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
