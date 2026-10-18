package ru.repetitor.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Тонкая оболочка: показывает веб-интерфейс из assets/index.html.
 * Запросы к API делаются нативно (в обход CORS), результат возвращается в JS.
 */
class MainActivity : Activity() {

    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = false
        web.addJavascriptInterface(Bridge(web), "Android")
        web.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    private class Bridge(private val web: WebView) {

        @JavascriptInterface
        fun ask(id: String, key: String, body: String) {
            Thread {
                var ok = false
                var out: String
                try {
                    val c = URL("https://api.anthropic.com/v1/messages").openConnection() as HttpURLConnection
                    c.requestMethod = "POST"
                    c.connectTimeout = 20000
                    c.readTimeout = 120000
                    c.doOutput = true
                    c.setRequestProperty("content-type", "application/json")
                    c.setRequestProperty("x-api-key", key)
                    c.setRequestProperty("anthropic-version", "2023-06-01")
                    c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                    val code = c.responseCode
                    val stream = if (code in 200..299) c.inputStream else c.errorStream
                    out = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                    ok = code in 200..299
                } catch (e: Exception) {
                    out = JSONObject().put(
                        "error", JSONObject().put("message", "Нет соединения: " + (e.message ?: ""))
                    ).toString()
                }
                val js = "window.__onAnswer(${JSONObject.quote(id)}, $ok, ${JSONObject.quote(out)})"
                web.post { web.evaluateJavascript(js, null) }
            }.start()
        }
    }
}
