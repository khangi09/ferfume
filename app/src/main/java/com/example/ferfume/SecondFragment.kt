package com.example.ferfume

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ferfume.databinding.FragmentSecondBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView
import android.widget.TextView

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()
    private val items = mutableListOf<Product>() // products fetched from Firestore
    private lateinit var itemAdapter: ItemAdapter // adapter for products

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun fetchProductsByCategory(selectedCategory: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .whereEqualTo("brand", selectedCategory)
            .get()
            .addOnSuccessListener { result ->
                items.clear()
                for (document in result) {
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val brand = document.getString("brand") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val scent = document.getString("scent") ?: ""
                    // Ignore imageUrl completely
                    val product = Product(id, name, brand, price, scent, "")
                    items.add(product)
                }
                itemAdapter.notifyDataSetChanged()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Categories RecyclerView
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
                binding.textView8.text = selectedCategory
                fetchProductsByCategory(selectedCategory)
            }
            adapter = categoryAdapter
        }

        // Products RecyclerView
        binding.productRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            itemAdapter = ItemAdapter(items) { product ->
                val intent = Intent(activity, ItemViewActivity::class.java).apply {
                    putExtra("productId", product.id)
                    putExtra("name", product.name)
                    putExtra("brand", product.brand)
                    putExtra("price", product.price)
                    putExtra("scent", product.scent)
                    // Always send empty image url
                    putExtra("imageUrl", "")
                }
                startActivity(intent)
            }
            adapter = itemAdapter
        }

        fetchCategories()
    }

    private fun fetchCategories() {
        val db = FirebaseFirestore.getInstance()
        db.collection("categoriesM")
            .get()
            .addOnSuccessListener { result ->
                categories.clear()
                for (document in result) {
                    val category = document.toObject(Category::class.java)
                    categories.add(category)
                }
                categoryAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// -------------------- ItemAdapter --------------------

class ItemAdapter(
    private val items: List<Product>,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textViewProductName)
        val brandText: TextView = itemView.findViewById(R.id.textViewProductBrand)
        val priceText: TextView = itemView.findViewById(R.id.textViewProductPrice)
        val imageView: ImageView = itemView.findViewById(R.id.productImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val product = items[position]
        holder.nameText.text = product.name
        holder.brandText.text = product.brand
        holder.priceText.text = product.price.toString()

        // Always use placeholder image
        holder.imageView.setImageResource(R.drawable.img_45)

        holder.itemView.setOnClickListener { onClick(product) }
    }

    override fun getItemCount() = items.size
}
