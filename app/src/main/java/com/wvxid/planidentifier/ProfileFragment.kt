package com.wvxid.planidentifier

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wvxid.planidentifier.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            // Handle case where user is not logged in
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return binding.root
        }

        loadUserProfile()

        binding.setBioButton.setOnClickListener {
            showSetBioDialog()
        }

        binding.createPostButton.setOnClickListener {
            startActivity(Intent(requireContext(), CreatePostActivity::class.java))
        }

        binding.historyButton.setOnClickListener {
            startActivity(Intent(requireContext(), HistoryActivity::class.java))
        }

        binding.myFavoriteButton.setOnClickListener {
            startActivity(Intent(requireContext(), MyFavoritesActivity::class.java))
        }

        // You can add a sign-out button functionality here if you want

        return binding.root
    }

    private fun loadUserProfile() {
        firestore.collection("users").document(currentUserId!!).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    binding.usernameTextView.text = user?.name
                    binding.bioTextView.text = user?.bio ?: "Bio not set"
                    binding.emailTextView.text = user?.email
                } else {
                    Toast.makeText(requireContext(), "Profile data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSetBioDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set Bio")

        val input = EditText(requireContext())
        input.setText(binding.bioTextView.text.toString().takeIf { it != "Bio not set" } ?: "")
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newBio = input.text.toString()
            updateBioInFirestore(newBio)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun updateBioInFirestore(newBio: String) {
        firestore.collection("users").document(currentUserId!!)
            .update("bio", newBio)
            .addOnSuccessListener {
                binding.bioTextView.text = newBio
                Toast.makeText(requireContext(), "Bio updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update bio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}