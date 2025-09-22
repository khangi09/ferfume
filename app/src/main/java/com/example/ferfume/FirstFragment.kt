package com.example.ferfume

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ferfume.databinding.FragmentFirstBinding
import com.example.ferfume.databinding.ItemCategoryBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()
    private val items = mutableListOf<Product>()
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the fade-in animation for static views
        val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)

        // Apply the animation to the search card and text views
        binding.searchCard.startAnimation(fadeInAnimation)

        binding.textView8.startAnimation(fadeInAnimation)
        binding.textView8.text = "All"

        // Setup category RecyclerView
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
                _binding?.textView8?.text = selectedCategory
                fetchProductsByCategory(selectedCategory)
            }
            adapter = categoryAdapter
        }

        // Setup product RecyclerView
        binding.productRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            itemAdapter = ItemAdapter(items) { product ->
                val intent = Intent(activity, ItemViewActivity::class.java).apply {
                    putExtra("productId", product.id)
                    putExtra("name", product.name)
                    putExtra("brand", product.brand)
                    putExtra("price", product.price)
                    putExtra("scent", product.scent)
                    putExtra("imageUrl", product.imageUrl)
                }
                startActivity(intent)
            }
            adapter = itemAdapter
        }

        // Fetch categories and then fetch all products for the initial display
        fetchCategories()
        fetchAllProducts()
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
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAllProducts() {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                items.clear()
                for (document in result) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        brand = document.getString("brand") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        scent = document.getString("scent") ?: "",
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    items.add(product)
                }
                itemAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load all products", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchProductsByCategory(selectedCategory: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .whereEqualTo("brand", selectedCategory)
            .get()
            .addOnSuccessListener { result ->
                items.clear()
                for (document in result) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        brand = document.getString("brand") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        scent = document.getString("scent") ?: "",
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    items.add(product)
                }
                itemAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load products for category", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ------------------- Data Models -------------------

data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = ""
)

data class Product(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val price: Double = 0.0,
    val scent: String = "",
    val imageUrl: String = ""
)

// ------------------- Adapters -------------------

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.textView.text = category.name
            Picasso.get().load(category.imageUrl).into(binding.imageView)

            binding.root.setOnClickListener {
                onCategorySelected(category.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
        val fadeInAnim = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in)
        holder.itemView.startAnimation(fadeInAnim)
    }

    override fun getItemCount(): Int = categories.size
}
