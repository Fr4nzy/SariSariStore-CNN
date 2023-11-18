package org.lu.sarisaristorepos;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DeleteProducts extends AppCompatActivity implements ProductAdapter.ProductSelectionListener {

    private TextView selectionIndicatorTextView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    private ArrayList<String> selectedItems; // Store selected items for deletion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_products);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button deleteButton = findViewById(R.id.deleteButton);
        selectionIndicatorTextView = findViewById(R.id.selectionIndicator);

        // Make selection Indicator non-clickable
        selectionIndicatorTextView.setClickable(false);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);

        // Define the category list
        String[] categories = {"All Products", "Canned Goods", "Condiments", "Powdered Beverages", "Instant Noodles", "Others"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Get the selected category from the Spinner
        // Apply the adapter to the spinner
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        categorySpinner.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        selectedItems = new ArrayList<>(); // Initialize the list of selected items

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Load products from Firestore based on the selected category
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                loadProducts(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        // Add a click listener to the "Delete" button
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadProducts(String selectedCategory) {
        productList.clear(); // Clear the existing product list

        if ("All Products".equals(selectedCategory)) {
            // If "All Products" is selected, load products from multiple collections
            loadAllProducts();
        } else {
            // Load products from Firestore based on the selected category
            db.collection(selectedCategory).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                            String id = documentChange.getDocument().getId(); // Get the Firestore document ID
                            String name = documentChange.getDocument().getString("name");
                            String price = documentChange.getDocument().getString("price");
                            String imageURL = documentChange.getDocument().getString("imageURL");
                            String brand = documentChange.getDocument().getString("brand");
                            String category = documentChange.getDocument().getString("category");

                            productList.add(new Product(id, name, price, imageURL, brand, category));
                        }

                        productAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    // Load all products from the specified collections
    private void loadAllProducts() {
        // List of collections to load products from
        String[] allCollections = {"Canned Goods", "Condiments", "Powdered Beverages", "Instant Noodles", "Others"};

        // Counter to track the number of collections loaded
        AtomicInteger collectionsLoaded = new AtomicInteger();

        // Create a list to store all tasks
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // Iterate through each collection and create a task for each
        for (String collection : allCollections) {
            Task<QuerySnapshot> task = db.collection(collection).get();
            tasks.add(task);
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(querySnapshotsList -> {
            // Clear the existing product list before adding products from all collections
            productList.clear();

            // Iterate through each query snapshot list and add products to the list
            for (Object querySnapshots : querySnapshotsList) {
                if (querySnapshots instanceof QuerySnapshot) {
                    QuerySnapshot snapshot = (QuerySnapshot) querySnapshots;
                    for (DocumentChange documentChange : snapshot.getDocumentChanges()) {
                        String id = documentChange.getDocument().getId();
                        String name = documentChange.getDocument().getString("name");
                        String price = documentChange.getDocument().getString("price");
                        String imageURL = documentChange.getDocument().getString("imageURL");
                        String brand = documentChange.getDocument().getString("brand");
                        String category = documentChange.getDocument().getString("category");

                        productList.add(new Product(id, name, price, imageURL, brand, category));
                    }
                }
            }

            // Notify the adapter after all collections have been processed
            productAdapter.notifyDataSetChanged();
        });

    }

    @Override
    public void onProductSelectionChanged() {
        // Handle product selection changes if needed
        updateSelectionIndicator();
    }

    // Show a confirmation dialog before deleting selected items
    private void showDeleteConfirmationDialog() {
        // Check if there are selected items before showing the dialog
        if (!selectedItems.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Confirmation");
            builder.setMessage("Are you sure you want to delete the selected items?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // User confirmed deletion, call deleteSelectedProducts()
                deleteSelectedProducts();
            });
            builder.setNegativeButton("No", (dialog, which) -> {
                // User canceled, do nothing
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            // Notify the user that no items are selected
            Toast.makeText(this, "Please select items to delete", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteSelectedProducts() {
        if (!selectedItems.isEmpty()) {
            // Create a batch to delete selected products in a single transaction
            WriteBatch batch = db.batch();

            for (String selectedItem : selectedItems) {
                // Split the selected item string to get the product ID
                String[] parts = selectedItem.split(" - ");
                if (parts.length == 2) {
                    String productName = parts[0].trim();
                    String productPrice = parts[1].trim();

                    // Find the product with matching name and price
                    for (Product product : productList) {
                        if (product.getName().equals(productName) && product.getPrice().equals(productPrice)) {
                            // Delete the product from Firestore
                            DocumentReference productRef = db.collection(product.getCategory()).document(product.getId());
                            batch.delete(productRef);

                            // Delete the image from Firebase Storage
                            deleteImageFromStorage(product.getImageURL());

                            break; // Stop searching after deletion
                        }
                    }
                }
            }

            // Commit the batch to Firestore
            batch.commit().addOnSuccessListener(aVoid -> {
                // Deletion successful
                productAdapter.notifyDataSetChanged(); // Update the UI

                // Clear the selected items list
                selectedItems.clear();

                // Update the selection indicator after the deletion process is complete
                updateSelectionIndicator();
            }).addOnFailureListener(e -> {
                // Handle the error
                // You may want to implement error handling based on your requirements
            });
        }
    }

    // Function to delete the image from Firebase Storage
    private void deleteImageFromStorage(String imageURL) {
        // Get a reference to the image in Firebase Storage
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);

        // Delete the image
        imageRef.delete().addOnSuccessListener(aVoid -> {
            // Image deleted successfully
        }).addOnFailureListener(exception -> {
            // Handle any errors that may occur
            // For example, log the error
            Log.e("DeleteProducts", "Error deleting image: " + exception.getMessage());
        });
    }

    public void updateSelectionIndicator() {
        double totalCost = 0.0;

        int selectedProductCount = 0; // Track the count of selected products

        for (Product product : productList) {
            if (product.isSelected()) {
                selectedItems.add(product.getName() + " - " + product.getPrice());
                totalCost += Double.parseDouble(product.getPrice());
                selectedProductCount++;
            }
        }

        // Update the selection indicator text with the count of selected products
        selectionIndicatorTextView.setText("Cart: " + selectedProductCount);

        // Check if the deletion process is complete and update the selection indicator
        if (selectedProductCount == 0 && selectedItems.isEmpty()) {
            selectionIndicatorTextView.setText("Cart: " + selectedProductCount);
        }
    }

}
