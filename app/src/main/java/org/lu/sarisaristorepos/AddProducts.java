package org.lu.sarisaristorepos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.lu.sarisaristorepos.ml.ModelProducts;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AddProducts extends AppCompatActivity {
    private EditText productNameEditText, productPriceEditText, productBrandEditText;
    private Spinner categorySpinner;
    private ImageView previewImageView;
    private TextView classifyLbl, percentLbl;


    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri selectedImageUri = null;
    private StorageReference storageReference;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        productNameEditText = findViewById(R.id.productName);
        productPriceEditText = findViewById(R.id.productPrice);
        productBrandEditText = findViewById(R.id.productBrand);
        Button submitButton = findViewById(R.id.btnInsert);
        categorySpinner = findViewById(R.id.categorySelect);
        previewImageView = findViewById(R.id.previewImg);
        classifyLbl = findViewById(R.id.classifyLbl);
        percentLbl = findViewById(R.id.percentLbl);
        Button selectImageButton = findViewById(R.id.btnImgSelect);
        Button camera = findViewById(R.id.btnImgCapture);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Firebase Storage
        // Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize the spinner with the category options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        camera.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent,3);
                }else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        selectImageButton.setOnClickListener(v -> {
            // Start an image picker intent
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1);
        });


        // Initialize the image picker launcher
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        displaySelectedImage();
                    }
                });

        submitButton.setOnClickListener(v -> {
            String productName = productNameEditText.getText().toString();
            String productPrice = productPriceEditText.getText().toString();
            String productBrand = productBrandEditText.getText().toString();
            String selectedCategory = categorySpinner.getSelectedItem().toString();

            // Check if an image is selected
            if (!productName.isEmpty() && !productPrice.isEmpty() && !productBrand.isEmpty()) {
                // Check if the user is authenticated
                if (auth.getCurrentUser() != null) {
                    // Check if any of the fields is empty
                    if (selectedImageUri != null) {
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

                                // Create a new document in the selected category collection
                                DocumentReference productRef = db.collection(selectedCategory).document(); // Use the selected category

                                // Create a Map to store the product data
                                Map<String, Object> productData = new HashMap<>();
                                productData.put("name", productName);
                                productData.put("price", productPrice);
                                productData.put("brand", productBrand);
                                productData.put("category", selectedCategory);
                                productData.put("imageURL", imageURL); // Store the image URL

                                // Set the data in Firestore
                                productRef.set(productData)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Data inserted successfully
                                                productNameEditText.setText("");
                                                productPriceEditText.setText("");
                                                productBrandEditText.setText("");
                                                previewImageView.setImageResource(R.drawable.baseline_image_24); // Reset image view
                                            } else {
                                                // Handle the error
                                                Toast.makeText(this, "Fail Insertion", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            });

                        });
                    } else {
                        // Notify the user that an image must be selected
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // Notify the user that fields can't be empty
                Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_SHORT).show();
            }
        });


    }


    // Helper method to classify the selected image
    public void classifyImage(Bitmap image){
        try {
            ModelProducts model = ModelProducts.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());

            int pixel = 0;
            // Iterates over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (int i=0;i<imageSize;i++){
                for(int j=0;j<imageSize;j++){
                    int val = intValues[pixel++]; // RGB Values
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelProducts.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // finds the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++){
                if (confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {
                    "Carne Norte", "Tuna Adobo", "Tuna Afritada", "Tuna Caldereta", "Tuna Mechado", "Alaska Evaporada",
                    "Argentina Corned Beef", "Argentina Meatloaf", "Baby Powder", "Birch Tree", "Camel Yellow", "Century Tuna 155g",
                    "CloseUp Sachet", "Commando Matches", "Cup Noodles Beef", "Cup Noodles Seafood", "Datu Puti Patis",
                    "Datu Puti Soysauce Bottle", "Datu Puti Soysauce Sachet", "Datu Puti Vinegar Sachet", "DelMonte TomatoSauce", "Energen Chocolate",
                    "Energen Vanilla", "Great Taste Brown", "Great Taste Classic", "Hapee Toothpaste", "Hokkaido", "Ketchup Sachet",
                    "Kopiko Black", "Kopiko Blanca", "Kopiko Brown", "Lucky 7 100g", "Lucky 7 150g", "LuckyMe Beef", "LuckyMe Chicken",
                    "LuckyMe Pancit Canton", "LuckyMe SpicyBeef", "Mang Tomas", "Marlboro Red", "Mega Sardines Green", "Mega Sardines Red",
                    "Mighty Green", "Milo", "Nescafe Creamylatte", "Nescafe Decaf", "Nescafe White", "Nissin Beef", "Nissin Seafood",
                    "Nissin Spicy Seafood", "Olivenza Matches", "Payless Pancit Canton", "Plus Juice", "San Marino Corned Tuna 100g", "San Marino Corned Tuna 150g",
                    "StarMargarin Sweetblend", "Wow Ulam Caldereta", "Wow Ulam Mechado"
            };
            classifyLbl.setText(classes[maxPos]);


            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // Handle the exception during model loading
            Log.e("ModelLoadingError", "Error loading the TensorFlow Lite model: " + e.getMessage());
            Toast.makeText(this, "Error loading the model. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 3) { // Handles the image being taken by the camera
                Bitmap image = null;
                if (data != null && data.getExtras() != null) {
                    image = (Bitmap) data.getExtras().get("data");
                }
                if (image != null) {
                    int dimension = Math.min(image.getWidth(), image.getHeight());
                    image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                    previewImageView.setImageBitmap(image);

                    try {
                        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                        classifyImage(image); // This proceeds to resize it
                    } catch (Exception e) {
                        Log.e("ImageProcessingError", "Error processing the image: " + e.getMessage());
                        Toast.makeText(this, "Error processing the image. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Uri dat = Objects.requireNonNull(data).getData();
                if (dat != null) {
                    Bitmap image;
                    try {
                        image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                        previewImageView.setImageBitmap(image); // This handles the image in the gallery

                        try {
                            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                            classifyImage(image); // Then resize it
                        } catch (Exception e) {
                            Log.e("ImageProcessingError", "Error processing the image: " + e.getMessage());
                            Toast.makeText(this, "Error processing the image. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e("ImageRetrievalError", "Error retrieving the image: " + e.getMessage());
                        Toast.makeText(this, "Error retrieving the image. Please try again.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }else {
                    // Handle the case when the 'data' extra is null or the image is not provided
                    Toast.makeText(this, "Loading Image Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // Helper method to display the selected image
    private void displaySelectedImage() {
        Glide.with(this)
                .load(selectedImageUri)
                .apply(new RequestOptions().centerCrop())
                .into(previewImageView);
    }


}
