package org.lu.sarisaristorepos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private ArrayList<String> selectedItems = new ArrayList<>(); // Initialize the variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Retrieve data from the intent
        if (getIntent().hasExtra("selectedItems")) {
            selectedItems = getIntent().getStringArrayListExtra("selectedItems");
        }
        double totalCost = getIntent().getDoubleExtra("totalCost", 0.0);

        // Display selected items in CartActivity's layout
        TextView selectedItemsTextView = findViewById(R.id.selectedItemsTextView);

        // Format the selected items as a string
        StringBuilder itemsText = new StringBuilder();
        for (String item : selectedItems) {
            itemsText.append(item).append("\n");
        }

        selectedItemsTextView.setText(itemsText.toString());

        TextView totalCostTextView = findViewById(R.id.totalCostTextView);
        // Display the overall total cost
        totalCostTextView.setText(getString(R.string.total_cost_label, totalCost));

        // Calculate and display the cost for each item
        calculateCostPerItem();

        // After updating the cart and calculating the total cost
        Button purchaseBtn = findViewById(R.id.btnPurchased);
        purchaseBtn.setOnClickListener(view -> {
            // Prepare the result intent with updated information
            openPurchasedActivity(totalCost);
            finish();
        });
    }

    private void calculateCostPerItem() {
        // Iterate through selected items and calculate and display the cost for each item
        for (String selectedItem : selectedItems) {
            // Assuming the format is "Product Name - Price"
            String[] parts = selectedItem.split(" = ");
            if (parts.length == 2) {
                String productName = parts[0];
                double price = Double.parseDouble(parts[1]);

                // Find the TextView corresponding to the item and display the cost
                int resID = getResources().getIdentifier(productName.toLowerCase().replaceAll("\\s+", ""), "id", getPackageName());
                TextView itemCostTextView = findViewById(resID);
                if (itemCostTextView != null) {
                    itemCostTextView.setText(String.format("Cost for %s: ₱%s", productName, String.format("%.2f", price)));
                }
            }
        }
    }

    private void openPurchasedActivity(double totalCost) {
        Intent intent = new Intent(this, PurchasedActivity.class);
        intent.putStringArrayListExtra("selectedItems", selectedItems);
        intent.putExtra("totalCost", totalCost);
        startActivity(intent);
    }
}
