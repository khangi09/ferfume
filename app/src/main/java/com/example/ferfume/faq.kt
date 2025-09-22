package com.example.ferfume

import android.content.Intent
import com.google.android.material.card.MaterialCardView
import android.widget.Toast
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ferfume.GeneralAccountFaqActivity
class Faq : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq) // Replace with your actual layout name

        val cardGeneral = findViewById<MaterialCardView>(R.id.card_general)
        val cardOrders = findViewById<MaterialCardView>(R.id.card_orders)
        val cardProducts = findViewById<MaterialCardView>(R.id.card_products)
        val cardPayments = findViewById<MaterialCardView>(R.id.card_payments)
        val cardReturns = findViewById<MaterialCardView>(R.id.card_returns)
        val cardTechnical = findViewById<MaterialCardView>(R.id.card_technical)

        cardGeneral.setOnClickListener {
            val intent = Intent(this, GeneralAccountFaqActivity::class.java)
            startActivity(intent)
        }

        cardOrders.setOnClickListener {
            Toast.makeText(this, "Clicked Orders & Shipping", Toast.LENGTH_SHORT).show()
        }

        cardProducts.setOnClickListener {
            Toast.makeText(this, "Clicked Products & Inventory", Toast.LENGTH_SHORT).show()
        }

        cardPayments.setOnClickListener {
            Toast.makeText(this, "Clicked Payments & Billing", Toast.LENGTH_SHORT).show()
        }

        cardReturns.setOnClickListener {
            Toast.makeText(this, "Clicked Returns & Refunds", Toast.LENGTH_SHORT).show()
        }

        cardTechnical.setOnClickListener {
            Toast.makeText(this, "Clicked Technical Issues / App Usage", Toast.LENGTH_SHORT).show()
        }
    }
}


