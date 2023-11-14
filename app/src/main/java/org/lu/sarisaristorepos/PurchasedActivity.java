package org.lu.sarisaristorepos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class PurchasedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased);

        // Retrieve data from the intent
        ArrayList<String> selectedItems = getIntent().getStringArrayListExtra("selectedItems");
        double totalCost = getIntent().getDoubleExtra("totalCost", 0.0);

        // Display selected items and total cost in CartActivity's layout
        TextView purchasedItems = findViewById(R.id.itemsPurchased);
        TextView totalCostPurchased = findViewById(R.id.totalCostPurchased);

        // Format the selected items as a string
        StringBuilder itemsText = new StringBuilder();
        for (String item : selectedItems) {
            itemsText.append(item).append("\n");
        }

        purchasedItems.setText(itemsText.toString());
        totalCostPurchased.setText("Total Cost: ₱" + String.format("%.2f", totalCost));

        // Handle cash payment when the "Cash" button is clicked
        Button cashButton = findViewById(R.id.btnCash);
        cashButton.setOnClickListener(v -> {
            // Start the CashAmountActivity to input cash amount
            Intent intent = new Intent(PurchasedActivity.this, CashAmountActivity.class);
            intent.putExtra("totalCost", totalCost);
            startActivity(intent);
        });
    }



}