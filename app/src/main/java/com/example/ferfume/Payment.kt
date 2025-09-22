package com.example.ferfume
import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class Payment : AppCompatActivity() {

    private lateinit var webView: WebView
    private val backendUrl =
        "https://44fcea99d050.ngrok-free.app/api/paystack/generate-payment-url"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        webView = findViewById(R.id.webview_payment)

        // ✅ WebView settings
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        // ✅ WebView client for handling redirects
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.d("WebView", "Navigating to: $url")

                if (url.startsWith("https://yourapp.com/payment-complete")) {
                    val reference = Uri.parse(url).getQueryParameter("reference")
                    Toast.makeText(
                        this@Payment,
                        "Payment finished: $reference",
                        Toast.LENGTH_SHORT
                    ).show()

                    // ✅ Redirect to Success screen
                    val intent = Intent(this@Payment, Check::class.java)
                    intent.putExtra("reference", reference)
                    startActivity(intent)

                    // ✅ Optionally finish this activity
                    finish()
                    return true
                }

                return false
            }

        }

        // ✅ Extract amount and email from intent
        val amount = intent.getIntExtra("amount", 0)
        val email = intent.getStringExtra("email") ?: "fallback@example.com"

        Log.d("Payment", "Received email: $email, amount: $amount")

        // ✅ Start payment with actual values
        generatePaymentUrl(email, amount)
    }

    private fun generatePaymentUrl(email: String, amount: Int) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("email", email)
            put("amount", amount)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(backendUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Payment", "Request failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@Payment, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                Log.d("Payment", "Response body: $bodyStr")

                if (response.isSuccessful) {
                    try {
                        val url = JSONObject(bodyStr!!).getString("url")
                        Log.d("Payment", "Loading URL: $url")

                        runOnUiThread {
                            Toast.makeText(this@Payment, "Redirecting...", Toast.LENGTH_SHORT).show()
                            webView.loadUrl(url)
                        }
                    } catch (e: Exception) {
                        Log.e("Payment", "Failed to parse URL: ${e.message}")
                        runOnUiThread {
                            Toast.makeText(this@Payment, "Error parsing payment URL", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Log.e("Payment", "Server error: $bodyStr")
                    runOnUiThread {
                        Toast.makeText(this@Payment, "Server Error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
