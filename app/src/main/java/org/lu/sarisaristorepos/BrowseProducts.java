package org.lu.sarisaristorepos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BrowseProducts extends AppCompatActivity implements ProductAdapter.ProductSelectionListener {

    private TextView cartIndicatorTextView;
    private EditText searchEditText;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    private ArrayList<String> selectedItems; // Store selected items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_products);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        cartIndicatorTextView = findViewById(R.id.cartIndicator);
        searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setVerticalScrollBarEnabled(false); // Hide vertical scrollbar

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList,null); // Remove the listener
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

        // Initial load of products based on the default selected category
        loadProducts("All Products"); // Load all products by default

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

        // Initial load of products based on the default selected category
        if (categorySpinner.getSelectedItem() != null) {  // Check if selectedItem is not null
            loadProducts(categorySpinner.getSelectedItem().toString());
        }

        cartIndicatorTextView.setOnClickListener(v -> {
            // Create an intent to open CartActivity
            Intent intent = new Intent(BrowseProducts.this, CartActivity.class);

            // Pass the selected items and their total cost as extras
            double totalCost = calculateTotalCost(selectedItems);

            intent.putStringArrayListExtra("selectedItems", selectedItems);
            intent.putExtra("totalCost", totalCost);

            Log.d("BrowseProducts", "Selected items: " + selectedItems.toString());
            Log.d("BrowseProducts", "Total cost: " + totalCost);

            startActivity(intent);
        });

        // Add a click listener to the search button
        searchButton.setOnClickListener(v -> searchProducts());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Update the cartIndicator and other relevant information based on the result
            if (data != null) {
                String productName = data.getStringExtra("productName");
                String productPrice = data.getStringExtra("productPrice");
                String productCategory = data.getStringExtra("productCategory");

                // Add the selected item to the cart
                selectedItems.add(productName + " - " + productPrice);

                // Update your cartIndicatorTextView and other UI elements as needed
                // For example:
                updateCartIndicator();
            }
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

    @Override
    public void onProductSelectionChanged() {
        updateCartIndicator();
    }

    public void updateCartIndicator() {
        selectedItems.clear(); // Clear the list of selected items
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
        cartIndicatorTextView.setText(getString(R.string.cart_label, selectedProductCount));
    }

    private double calculateTotalCost(ArrayList<String> selectedItems) {
        double totalCost = 0.0;

        for (String selectedItem : selectedItems) {
            // Parse the price from the selected item string and add to the total cost
            String[] parts = selectedItem.split(" - ");
            if (parts.length == 2) {
                double price = Double.parseDouble(parts[1]);
                totalCost += price;
            }
        }

        return totalCost;
    }

    private void searchProducts() {
        String query = searchEditText.getText().toString().trim().toLowerCase();

        if (!query.isEmpty()) {
            // Filter the product list based on the search query
            List<Product> filteredList = new ArrayList<>();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query)) {
                    filteredList.add(product);
                }
            }

            // Update the RecyclerView to display the filtered list
            productAdapter.setProductList(filteredList);
            productAdapter.notifyDataSetChanged();
        } else {
            // If the query is empty, display the original product list
            productAdapter.setProductList(productList);
            productAdapter.notifyDataSetChanged();
        }
    }
}
