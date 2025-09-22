package com.example.ferfume

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class Contact : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSubmit: Button
    private val client = OkHttpClient()

    // Set the recipient address here
    private val recipientEmail = "khangi@silversurge.co.za"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etMessage = findViewById(R.id.et_message)
        btnSubmit = findViewById(R.id.btn_submit)

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val message = etMessage.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                sendEmailToServer(name, email, message)
            }
        }
    }

    private fun sendEmailToServer(name: String, email: String, message: String) {
        val json = """
            {
                "userName": "$name",
                "userEmail": "$email",
                "subject": "Contact Form Message from $name",
                "body": "$message",
                "recipientEmail": "khangalekhangale026@gmail.com"
            }
        """.trimIndent()

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json
        )

        val request = Request.Builder()
            .url("https://95ee-105-245-97-237.ngrok-free.app/api/email/send")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@Contact, "Failed to send message: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@Contact, "Message sent successfully!", Toast.LENGTH_LONG).show()
                        etName.text?.clear()
                        etEmail.text?.clear()
                        etMessage.text?.clear()
                    } else {
                        Toast.makeText(this@Contact, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
