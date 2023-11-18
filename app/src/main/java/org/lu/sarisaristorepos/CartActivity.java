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
        selectedItems = getIntent().getStringArrayListExtra("selectedItems");
        double totalCost = getIntent().getDoubleExtra("totalCost", 0.0);

        // Check if there is a result from PlaceOrderActivity
        if (getIntent().hasExtra("enteredQuantity")) {
            TextView selectedItemsTextView = findViewById(R.id.selectedItemsTextView);

            // Retrieve the entered quantity and product details
            String productName = getIntent().getStringExtra("productName");
            int enteredQuantity = getIntent().getIntExtra("enteredQuantity", 1);
            double productPricePerUnit = getIntent().getDoubleExtra("productPrice", 0.0);

            // Update the quantity in your selected items list or display it as needed
            // For example, you can append it to the selectedItemsTextView
            selectedItemsTextView.append("\nProduct: " + productName + ", \nQuantity: " + enteredQuantity + ", \nPrice per Unit: ₱" + productPricePerUnit);

            // Calculate the updated total cost
            double productTotalCost = productPricePerUnit * enteredQuantity;

            // Append the total cost for this item to the selectedItemsTextView
            selectedItemsTextView.append("\nTotal Cost for " + productName + ": ₱" + String.format("%.2f", productTotalCost));
        }

        // Display selected items in CartActivity's layout
        TextView selectedItemsTextView = findViewById(R.id.selectedItemsTextView);

        // Format the selected items as a string
        StringBuilder itemsText = new StringBuilder();
        for (String item : selectedItems) {
            itemsText.append(item).append("\n");
        }

        selectedItemsTextView.setText(itemsText.toString());

        // Display the overall total cost
        TextView totalCostTextView = findViewById(R.id.totalCostTextView);
        totalCostTextView.setText("Total Cost: ₱" + String.format("%.2f", totalCost));

        // Go to the Purchase Activity once the user is done with the Cart
        Button purchaseBtn = findViewById(R.id.btnPurchased);
        purchaseBtn.setOnClickListener(view -> openPurchasedActivity(totalCost));
    }

    private void openPurchasedActivity(double totalCost) {
        Intent intent = new Intent(this, PurchasedActivity.class);
        intent.putStringArrayListExtra("selectedItems", selectedItems);
        intent.putExtra("totalCost", totalCost);
        startActivity(intent);
    }
}