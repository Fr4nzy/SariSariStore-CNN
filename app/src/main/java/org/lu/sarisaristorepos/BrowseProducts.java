package org.lu.sarisaristorepos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class BrowseProducts extends AppCompatActivity implements ProductAdapter.ProductSelectionListener {

    private RecyclerView recyclerView;
    private TextView cartIndicatorTextView;
    private EditText searchEditText;
    private Button searchButton;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    private ArrayList<String> selectedItems; // Store selected items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_products);

        recyclerView = findViewById(R.id.recyclerView);
        cartIndicatorTextView = findViewById(R.id.cartIndicator);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this); // Pass 'this' to indicate the current activity implements the interface
        recyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();

        selectedItems = new ArrayList<>(); // Initialize the list of selected items

        // Load products from Firestore
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        String name = documentChange.getDocument().getString("name");
                        String price = documentChange.getDocument().getString("price");
                        String imageURL = documentChange.getDocument().getString("imageURL");
                        String stocks = documentChange.getDocument().getString("stocks");

                        productList.add(new Product(name, price, imageURL, stocks));
                    }
                    productAdapter.notifyDataSetChanged();
                }
            }
        });

        cartIndicatorTextView.setOnClickListener(v -> {
            // Create an intent to open CartActivity
            Intent intent = new Intent(BrowseProducts.this, CartActivity.class);

            // Pass the selected items and their total cost as extras
            double totalCost = calculateTotalCost(selectedItems);

            intent.putStringArrayListExtra("selectedItems", selectedItems);
            intent.putExtra("totalCost", totalCost);

            startActivity(intent);
        });

        // Add a click listener to the search button
        searchButton.setOnClickListener(v -> searchProducts());

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
        cartIndicatorTextView.setText("Cart: " + selectedProductCount);
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
