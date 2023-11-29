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

            // Call calculateTotalCost without any parameters
            calculateTotalCost();

            // Pass the selected items as an extra to CartActivity
            intent.putStringArrayListExtra("selectedItems", selectedItems);

            // You might want to calculate the total cost again here if needed
            double totalCost = 0.0;
            for (Product product : productList) {
                if (product.isSelected()) {
                    totalCost += Double.parseDouble(product.getPrice()) * product.getQuantity();
                }
            }

            // Pass the total cost to CartActivity
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
            if (data != null) {
                String productName = data.getStringExtra("productName");
                String productPrice = data.getStringExtra("productPrice");
                String productCategory = data.getStringExtra("productCategory");
                int productQuantity = data.getIntExtra("productQuantity", 1);

                // Find the selected product in the productList and update its quantity
                for (Product product : productList) {
                    if (product.getName().equals(productName) && product.getPrice().equals(productPrice)) {
                        product.setQuantity(product.getQuantity() + productQuantity);
                    }
                }

                // Update the UI or notify the adapter about the change
                productAdapter.notifyDataSetChanged();
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
        calculateTotalCost();
    }

    public void updateCartIndicator() {
        // No need for cart indicator updates in this context
        // Remove the related logic

        // You may also remove the following lines if not needed
        double totalCost = 0.0;

        for (Product product : productList) {
            if (product.isSelected()) {
                totalCost += Double.parseDouble(product.getPrice()) * product.getQuantity();
            }
        }

        // Update the total cost TextView in CartActivity if needed
        // (You need to pass the totalCost to CartActivity when starting it)
        // Example: intent.putExtra("totalCost", totalCost);
    }



    private void calculateTotalCost() {
        // Iterate through selected items and calculate the total cost based on quantity
        double totalCost = 0.0;

        for (Product product : productList) {

            if (product.isSelected()) {
                totalCost += Double.parseDouble(product.getPrice()) * product.getQuantity();
            }
        }

        // Update the total cost TextView in CartActivity
        // (You need to pass the totalCost to CartActivity when starting it)
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
