package com.wvxid.planidentifier

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val TAG = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val signupButton = findViewById<Button>(R.id.signup_button)
        val usernameEditText = findViewById<EditText>(R.id.username_edit_text)
        val emailEditText = findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirm_password_edit_text)

        signupButton.setOnClickListener {
            Log.d(TAG, "Sign Up button clicked.")
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                if (firebaseUser != null) {
                                    // Create a user map to store in Firestore
                                    val userMap = hashMapOf(
                                        "id" to firebaseUser.uid,
                                        "name" to username,
                                        "email" to email,
                                        "bio" to ""
                                    )

                                    firestore.collection("users").document(firebaseUser.uid)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "User data saved to Firestore. Navigating to MainActivity.")
                                            Toast.makeText(this, "Signup Successful & Logged In", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, MainActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Firestore Error: ", e)
                                            Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                     Log.w(TAG, "Firebase user is null after creation.")
                                }
                            } else {
                                Log.e(TAG, "Signup Failed: ", task.exception)
                                Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.w(TAG, "Passwords do not match.")
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w(TAG, "One or more fields are empty.")
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.login_text).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}