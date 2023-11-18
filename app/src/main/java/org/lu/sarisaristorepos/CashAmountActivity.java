package org.lu.sarisaristorepos;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CashAmountActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_amount);

        db = FirebaseFirestore.getInstance();

        // Retrieve the total cost from the previous activity
        double totalCost = getIntent().getDoubleExtra("totalCost", 0.0);

        // Find views
        TextView totalCostTextView = findViewById(R.id.totalCostTextView);
        EditText cashAmountEditText = findViewById(R.id.editTextNumberDecimal2);
        Button finalizeButton = findViewById(R.id.finalizeButton);

        // Display the total cost
        totalCostTextView.setText("Total Cost: ₱" + String.format("%.2f", totalCost));

        // Handle the finalization of the transaction
        finalizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the cash amount entered by the user
                String cashAmountStr = cashAmountEditText.getText().toString();
                if (!cashAmountStr.isEmpty()) {
                    double cashAmount = Double.parseDouble(cashAmountStr);
                    double change = cashAmount - totalCost;

                    if (change >= 0) {
                        // The transaction is successful
                        storeTransaction(totalCost, cashAmount, change);
                    } else {
                        // Handle insufficient funds
                        Toast.makeText(CashAmountActivity.this, "Insufficient funds. Please enter a valid amount.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where no cash amount is entered
                    // You might want to show an error message
                    Toast.makeText(CashAmountActivity.this, "Please enter the cash amount.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeTransaction(double totalCost, double cashAmount, double change) {
        // Generate a unique transaction ID
        String transactionId = generateTransactionId();

        // Get the current date and time
        String currentDate = getCurrentDate();

        // Create a map to store transaction details
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("transactionId", transactionId);
        transactionData.put("date", currentDate);
        transactionData.put("totalCost", totalCost);
        transactionData.put("cashAmount", cashAmount);
        transactionData.put("change", change);

        // Store the transaction in Firestore
        db.collection("transactions").document(transactionId)
                .set(transactionData)
                .addOnSuccessListener(aVoid -> {
                    // Transaction stored successfully, now show the receipt dialog
                    showReceiptDialog(transactionId, currentDate, totalCost, cashAmount, change);
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to store the transaction
                    Toast.makeText(CashAmountActivity.this, "Error storing transaction. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showReceiptDialog(String transactionId, String currentDate, double totalCost, double cashAmount, double change) {
        // Create a custom dialog
        Dialog receiptDialog = new Dialog(this);
        receiptDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        receiptDialog.setContentView(R.layout.dialog_receipt);

        // Find views in the dialog
        TextView receiptTextView = receiptDialog.findViewById(R.id.receiptTextView);
        Button closeDialogButton = receiptDialog.findViewById(R.id.closeDialogButton);
        Button confirmDialogButton = receiptDialog.findViewById(R.id.confirmDialogButton);

        // Set receipt details
        String receiptMessage = "Transaction ID: " + transactionId + "\n\n" +
                "Date: " + currentDate + "\n\n" +
                "Total Cost: ₱" + String.format("%.2f", totalCost) + "\n\n" +
                "Cash Amount: ₱" + String.format("%.2f", cashAmount) + "\n\n" +
                "Change: ₱" + String.format("%.2f", change);

        receiptTextView.setText(receiptMessage);

        // Set a listener for the close button
        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                receiptDialog.dismiss();

                // Return to the previous activity
                Intent intent = new Intent(CashAmountActivity.this, PurchasedActivity.class);
                startActivity(intent);
            }
        });

        // Set a listener for the confirm button
        confirmDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                receiptDialog.dismiss();
                BrowseProducts();
                finish();
                // Show a confirmation message or perform additional actions if needed
                Toast.makeText(CashAmountActivity.this, "Transaction confirmed!", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        receiptDialog.show();
    }

    private String generateTransactionId() {
        // Use a combination of timestamp and a UUID to create a unique transaction ID
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();

        // Concatenate the timestamp and UUID, and return as the transaction ID
        return timeStamp + "_" + uuid;
    }

    private String getCurrentDate() {
        // Get the current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(calendar.getTime());
    }

    private void BrowseProducts(){
        Intent intent = new Intent(this, BrowseProducts.class);
        startActivity(intent);
    }
}
