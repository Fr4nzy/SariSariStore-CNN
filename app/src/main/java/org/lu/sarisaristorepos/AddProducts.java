package org.lu.sarisaristorepos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddProducts extends AppCompatActivity {
    private EditText productNameEditText, productPriceEditText, productQuantityEditText;
    private Spinner categorySpinner;
    private ImageView previewImageView;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri selectedImageUri = null;

    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        productNameEditText = findViewById(R.id.productName);
        productPriceEditText = findViewById(R.id.productPrice);
        productQuantityEditText = findViewById(R.id.productQuantity);
        Button submitButton = findViewById(R.id.btnInsert);
        categorySpinner = findViewById(R.id.categorySelect);
        previewImageView = findViewById(R.id.previewImg);
        Button selectImageButton = findViewById(R.id.btnImgSelect);

        // Initialize Firebase Storage
        // Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize the spinner with the category options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        displaySelectedImage();
                    }
                });

        selectImageButton.setOnClickListener(v -> {
            // Start an image picker intent
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent); // Use the image picker launcher
        });

        submitButton.setOnClickListener(v -> {
            String productName = productNameEditText.getText().toString();
            String productPrice = productPriceEditText.getText().toString();
            String productQuantity = productQuantityEditText.getText().toString();
            String selectedCategory = categorySpinner.getSelectedItem().toString();

            // Check if the user is authenticated
            if (auth.getCurrentUser() != null) {
                // Generate a unique image filename (e.g., using UUID)
                String imageFileName = UUID.randomUUID().toString();

                // Create a StorageReference with the image filename
                StorageReference imageRef = storageReference.child("images/" + imageFileName);

                // Upload the selected image to Firebase Storage
                UploadTask uploadTask = imageRef.putFile(selectedImageUri);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully

                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageURL = uri.toString();

                        // Create a new document in the "products" collection
                        DocumentReference productRef = db.collection("products").document();

                        // Create a Map to store the product data
                        Map<String, Object> productData = new HashMap<>();
                        productData.put("name", productName);
                        productData.put("price", productPrice);
                        productData.put("quantity", productQuantity);
                        productData.put("category", selectedCategory);
                        productData.put("imageURL", imageURL); // Store the image URL

                        // Set the data in Firestore
                        productRef.set(productData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Data inserted successfully
                                        productNameEditText.setText("");
                                        productPriceEditText.setText("");
                                        productQuantityEditText.setText("");
                                        previewImageView.setImageResource(R.drawable.baseline_image_24); // Reset image view
                                    } else {
                                        // Handle the error
                                        Toast.makeText(this, "Fail Insertion", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                });
            }
        });
    }

    // Helper method to display the selected image
    private void displaySelectedImage() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            Glide.with(this)
                    .load(selectedImageUri)
                    .apply(new RequestOptions().centerCrop())
                    .into(previewImageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
