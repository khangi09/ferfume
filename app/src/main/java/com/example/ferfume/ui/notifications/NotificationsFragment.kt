package com.example.ferfume.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ferfume.Contact
import com.example.ferfume.Faq
import com.example.ferfume.databinding.FragmentNotificationsBinding
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsFragment : Fragment() {

    // View Binding variable
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    // Firestore instance (you can remove if unused)
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupClickListeners()

        return root
    }

    private fun setupClickListeners() {
        // Navigate to FAQ Activity
        binding.faq.setOnClickListener {
            startActivity(Intent(requireContext(), Faq::class.java))
        }

        // Navigate to Contact Activity
        binding.contacti.setOnClickListener {
            startActivity(Intent(requireContext(), Contact::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
