package com.wvxid.planidentifier

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.wvxid.planidentifier.databinding.FragmentIdentificationBinding
import java.io.ByteArrayOutputStream

class IdentificationFragment : Fragment() {

    private var _binding: FragmentIdentificationBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val CAMERA_REQUEST_CODE = 102
        private const val GALLERY_REQUEST_CODE = 103
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdentificationBinding.inflate(inflater, container, false)

        binding.cameraButton.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        binding.galleryButton.setOnClickListener {
            openGallery()
        }

        return binding.root
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            var imageUri: Uri? = null

            if (requestCode == GALLERY_REQUEST_CODE) {
                imageUri = data?.data
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                val bitmap = data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    imageUri = getImageUriFromBitmap(requireContext(), bitmap)
                }
            }

            if (imageUri != null) {
                val intent = Intent(requireContext(), ResultActivity::class.java)
                intent.putExtra("image_uri", imageUri.toString())
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Failed to get image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}