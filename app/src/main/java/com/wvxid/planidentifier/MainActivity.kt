package com.wvxid.planidentifier

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // This function will move your local plant data to the online database
        migratePlantsToFirestoreIfNeeded()

        navView = findViewById(R.id.bottom_navigation)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val fragmentToLoad = intent.getStringExtra("fragmentToLoad")
        if (fragmentToLoad == "feed") {
            navView.selectedItemId = R.id.navigation_feed
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FeedFragment()).commit()
        } else if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FeedFragment()).commit()
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var selectedFragment: Fragment = FeedFragment()
        when (item.itemId) {
            R.id.navigation_feed -> selectedFragment = FeedFragment()
            R.id.navigation_species -> selectedFragment = SpeciesFragment()
            R.id.navigation_identification -> selectedFragment = IdentificationFragment()
            R.id.navigation_chat -> selectedFragment = ChatFragment()
            R.id.navigation_profile -> selectedFragment = ProfileFragment()
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()
        true
    }

    private fun migratePlantsToFirestoreIfNeeded() {
        val firestore = FirebaseFirestore.getInstance()
        val plantsCollection = firestore.collection("plants")

        plantsCollection.limit(1).get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                // If the collection is empty, populate it from the local JSON file.
                Log.d("Migration", "Plant data is empty. Migrating from JSON...")
                Toast.makeText(this, "Setting up plant catalog...", Toast.LENGTH_SHORT).show()

                try {
                    val jsonString = assets.open("plants.json").bufferedReader().use { it.readText() }
                    val plantsArray = JSONArray(jsonString)
                    val batch = firestore.batch()

                    for (i in 0 until plantsArray.length()) {
                        val plantObject = plantsArray.getJSONObject(i)
                        val plantData = hashMapOf(
                            "commonName" to plantObject.getString("commonName"),
                            "family" to plantObject.getString("family"),
                            "genus" to plantObject.getString("genus"),
                            "scientificName" to plantObject.getString("scientificName"),
                            "canFound" to plantObject.getString("canFound"),
                            "description" to plantObject.getString("description"),
                            "category" to plantObject.getString("category").lowercase(),
                            "imageName" to plantObject.getString("imageName"),
                            "healthStatus" to plantObject.optString("healthStatus"),
                            "suggestion" to plantObject.optString("suggestion")
                        )
                        val docRef = plantsCollection.document() // Auto-generate ID
                        batch.set(docRef, plantData)
                    }

                    batch.commit().addOnSuccessListener {
                        Log.d("Migration", "Plant data migrated successfully!")
                        Toast.makeText(this, "Plant catalog setup complete!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Log.e("Migration", "Error during batch commit", e)
                    }

                } catch (e: Exception) {
                    Log.e("Migration", "Error reading or parsing plants.json", e)
                }
            } else {
                Log.d("Migration", "Plant data already exists. No migration needed.")
            }
        }
    }
}