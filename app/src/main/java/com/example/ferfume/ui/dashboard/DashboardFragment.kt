package com.example.ferfume.ui.dashboard

import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.example.ferfume.Payment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ferfume.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private lateinit var checkoutButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val cartItems = mutableListOf<CartItem>()
    private lateinit var cartItemAdapter: CartItemAdapter
    private lateinit var fadeIn: Animation
    private lateinit var fadeOut: Animation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val cartRecyclerView: RecyclerView = root.findViewById(R.id.cartRecyclerView)
        checkoutButton = root.findViewById(R.id.checkoutButton)

        fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)

        setupRecyclerView(cartRecyclerView)

        checkoutButton.setOnClickListener {
            val subtotal = cartItems.sumOf {
                (it.price.toDoubleOrNull() ?: 0.0) * it.quantity
            }

            val email = auth.currentUser?.email ?: "testuser@example.com"
            val amountInCents = (subtotal * 100).toInt()

            val intent = Intent(requireContext(), Payment::class.java).apply {
                putExtra("amount", amountInCents)
                putExtra("email", email)
            }

            startActivity(intent)
        }

        fetchCartItems()

        return root
    }

    private fun setupRecyclerView(cartRecyclerView: RecyclerView) {
        cartItemAdapter = CartItemAdapter(cartItems) { product ->
            Toast.makeText(requireContext(), "Product clicked: ${product.name}", Toast.LENGTH_SHORT).show()
        }
        cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartItemAdapter
        }
    }

    private fun fetchCartItems() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("cart")
            .get()
            .addOnSuccessListener { result ->
                cartItems.clear()
                for (document in result) {
                    val item = document.toObject(CartItem::class.java)
                    cartItems.add(item)
                }
                cartItemAdapter.notifyDataSetChanged()
                calculateSubtotal()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load cart items.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateSubtotal() {
        val subtotal = cartItems.sumOf {
            (it.price.toDoubleOrNull() ?: 0.0) * it.quantity
        }
        val subtotalTextView: TextView = requireView().findViewById(R.id.cartSubtotalValue)
        subtotalTextView.text = "Subtotal: $${"%.2f".format(subtotal)}"
    }
}

//----------------------------------------------------------------------------------------------------------------------->>
// Cart item and adapter
data class CartItem(
    val productId: String = "",
    val name: String = "",
    val brand: String = "",
    val price: String = "",
    val scent: String = "",
    val imageUrl: String = "",
    val quantity: Int = 0
)

class CartItemAdapter(
    private val items: List<CartItem>,
    private val onProductClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.textViewProductName)
        private val productBrand: TextView = itemView.findViewById(R.id.textViewProductBrand)
        private val productPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        private val productScent: TextView = itemView.findViewById(R.id.textViewProductScent)
        private val productQuantity: TextView = itemView.findViewById(R.id.textViewProductQuantity)

        fun bind(cartItem: CartItem) {
            productName.text = cartItem.name
            productBrand.text = cartItem.brand
            productPrice.text = "$${cartItem.price}"
            productScent.text = cartItem.scent
            productQuantity.text = "Qty: ${cartItem.quantity}"

            if (cartItem.imageUrl.isEmpty()) {
                productImage.setImageResource(R.drawable.img_13)
            } else {
                Picasso.get()
                    .load(cartItem.imageUrl)
                    .placeholder(R.drawable.img_13)
                    .error(R.drawable.img_13)
                    .into(productImage)
            }

            itemView.setOnClickListener { onProductClick(cartItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}




