package org.lu.sarisaristorepos;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DeleteProducts extends AppCompatActivity implements ProductAdapter.ProductSelectionListener {

    private TextView cartIndicatorTextView;
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
        cartIndicatorTextView = findViewById(R.id.cartIndicator);

        // Make cartIndicator non-clickable
        cartIndicatorTextView.setClickable(false);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();

        selectedItems = new ArrayList<>(); // Initialize the list of selected items

        // Load products from Firestore
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        String id = documentChange.getDocument().getId(); // Get the Firestore document ID
                        String name = documentChange.getDocument().getString("name");
                        String price = documentChange.getDocument().getString("price");
                        String imageURL = documentChange.getDocument().getString("imageURL");
                        String category = documentChange.getDocument().getString("category");

                        productList.add(new Product(id, name, price, imageURL, category));
                    }

                    productAdapter.notifyDataSetChanged();
                }
            }
        });

        // Add a click listener to the "Delete" button
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    @Override
    public void onProductSelectionChanged() {
        // Handle product selection changes if needed
        updateCartIndicator();
    }

    // Show a confirmation dialog before deleting selected items
    private void showDeleteConfirmationDialog() {
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
                            db.collection("products").document(product.getId()).delete();

                            // Remove the product from the local list
                            productList.remove(product);

                            // Delete the image from Firebase Storage
                            deleteImageFromStorage(product.getImageURL());

                            break; // Stop searching after deletion
                        }
                    }
                }
            }

            productAdapter.notifyDataSetChanged(); // Update the UI

            // Clear the selected items list
            selectedItems.clear();

            // Update the cart indicator after the deletion process is complete
            updateCartIndicator();
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

    public void updateCartIndicator() {
        double totalCost = 0.0;

        int selectedProductCount = 0; // Track the count of selected products

        for (Product product : productList) {
            if (product.isSelected()) {
                selectedItems.add(product.getName() + " - " + product.getPrice());
                totalCost += Double.parseDouble(product.getPrice());
                selectedProductCount++;
            }
        }

        // Update the cart indicator text with the count of selected products
        cartIndicatorTextView.setText("Cart: " + selectedProductCount);

        // Check if the deletion process is complete and update the cart indicator
        if (selectedProductCount == 0 && selectedItems.isEmpty()) {
            cartIndicatorTextView.setText("Cart: " + selectedProductCount);
        }
    }

}
