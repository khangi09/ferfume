package com.example.ferfume

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ferfume.databinding.ActivityItemViewBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.example.ferfume.ui.dashboard.DashboardFragment
import android.util.Log
import android.widget.RadioButton
import android.view.View

class ItemViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemViewBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var quantity = 1 // Default quantity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityItemViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val productId = intent.getStringExtra("productId") ?: ""

        if (productId.isNotEmpty()) {
            fetchProductDetails(productId)
            setupRatingBar(productId)
        } else {
            displayIntentData()
        }

        setupQuantitySelector()

        binding.learnMoreButton.setOnClickListener {
            val productName = binding.nameTextView.text.toString()
            val productPrice = binding.priceTextView.text.toString().removePrefix("R").toDoubleOrNull() ?: 0.0
            val productScent = binding.scentTextView.text.toString()
            addToCart(productName, productPrice, productScent, quantity)
        }
    }

    private fun fetchProductDetails(productId: String) {
        db.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val productName = document.getString("name") ?: "Unknown"
                    val productPrice = document.getDouble("price") ?: 0.0
                    val productScent = document.getString("scent") ?: "Unknown"
                    val productDescription = document.getString("description") ?: "No description available."

                    // Populate sizes
                    val sizes = document.get("sizes") as? Map<String, Double> ?: emptyMap()
                    populateSizeRadioGroup(sizes)

                    binding.nameTextView.text = productName
                    binding.priceTextView.text = "R$productPrice"
                    binding.scentTextView.text = productScent
                    binding.descriptionTextView.text = productDescription

                    // 🔹 Always show placeholder instead of Firestore image URL
                    binding.productImageView.setImageResource(R.drawable.img_45)
                } else {
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = "Product details not found."
                }
            }
            .addOnFailureListener { e ->
                binding.errorTextView.visibility = View.VISIBLE
                binding.errorTextView.text = "Error fetching product details: ${e.message}"
            }
    }

    private fun populateSizeRadioGroup(sizes: Map<String, Double>) {
        binding.sizeRadioGroup.removeAllViews()
        sizes.forEach { (size, price) ->
            val radioButton = RadioButton(this).apply {
                text = "$size - R$price"
                id = View.generateViewId()
            }
            binding.sizeRadioGroup.addView(radioButton)
        }
    }

    private fun displayIntentData() {
        val productName = intent.getStringExtra("product_name") ?: "Unknown"
        val productPrice = intent.getDoubleExtra("product_price", 0.0)
        val productScent = intent.getStringExtra("product_scent") ?: "Unknown"
        val productDescription = intent.getStringExtra("product_description") ?: "No description available."

        binding.nameTextView.text = productName
        binding.priceTextView.text = "R$productPrice"
        binding.scentTextView.text = productScent
        binding.descriptionTextView.text = productDescription

        // 🔹 Always show placeholder instead of Intent image
        binding.productImageView.setImageResource(R.drawable.img_45)

        binding.errorTextView.visibility = if (productName.isBlank()) View.VISIBLE else View.GONE
    }

    private fun setupRatingBar(productId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You need to log in to rate.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("products").document(productId)
            .collection("ratings").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userRating = document.getDouble("rating")?.toFloat() ?: 0f
                binding.ratingBar.rating = userRating
            }

        calculateAverageRating(productId)

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            saveUserRating(productId, userId, rating)
        }
    }

    private fun saveUserRating(productId: String, userId: String, rating: Float) {
        val ratingData = mapOf("rating" to rating)
        db.collection("products").document(productId)
            .collection("ratings").document(userId)
            .set(ratingData)
            .addOnSuccessListener {
                Toast.makeText(this, "Rating submitted successfully!", Toast.LENGTH_SHORT).show()
                calculateAverageRating(productId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error submitting rating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateAverageRating(productId: String) {
        db.collection("products").document(productId)
            .collection("ratings")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val ratings = querySnapshot.documents.mapNotNull { it.getDouble("rating") }
                binding.ratingBar.rating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error calculating average rating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupQuantitySelector() {
        binding.minusButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.quantityTextView.text = quantity.toString()
            } else {
                Toast.makeText(this, "Minimum quantity is 1.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.plusButton.setOnClickListener {
            quantity++
            binding.quantityTextView.text = quantity.toString()
        }

        binding.quantityTextView.text = quantity.toString()
    }

    private fun addToCart(productName: String, productPrice: Double, productScent: String, quantity: Int) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You need to log in to add items to the cart.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton = findViewById<RadioButton>(binding.sizeRadioGroup.checkedRadioButtonId)
        val selectedSize = selectedRadioButton?.text.toString().split(" - ").firstOrNull() ?: "Unknown"

        val productId = intent.getStringExtra("productId") ?: ""

        Log.d("AddToCart", "Product ID: $productId")

        if (productId.isNotEmpty()) {
            val product = Products(
                name = productName,
                price = productPrice.toString(),
                scent = productScent,
                size = selectedSize,
                quantity = quantity,
                imageUrl = "local_resource" // 🔹 dummy placeholder reference
            )

            db.collection("users").document(userId).collection("cart").document(productId)
                .set(product)
                .addOnSuccessListener {
                    Toast.makeText(this, "$productName has been added to your cart.", Toast.LENGTH_SHORT).show()

                    // Hide UI then show cart
                    binding.main.visibility = View.GONE
                    binding.fragmentContainer.visibility = View.VISIBLE

                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContainer.id, DashboardFragment())
                        .commit()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error adding to cart: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: Product ID is missing.", Toast.LENGTH_SHORT).show()
        }
    }

    data class Products(
        val name: String,
        val price: String,
        val scent: String,
        val size: String,
        val quantity: Int,
        val imageUrl: String // kept for Firestore schema, but always "local_resource"
    )
}
