package org.lu.sarisaristorepos;

import static java.lang.String.format;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
        totalCostTextView.setText(getString(R.string.total_cost_label, totalCost));

        // Handle the finalization of the transaction
        finalizeButton.setOnClickListener(v -> {
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
                    Toast.makeText(CashAmountActivity.this, R.string.insufficient_funds_message, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where no cash amount is entered
                // You might want to show an error message
                Toast.makeText(CashAmountActivity.this, R.string.error_storing_transaction_message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeTransaction(double totalCost, double cashAmount, double change) {
        // Generate a unique transaction ID
        String transactionId = generateTransactionId();

        // Get the current date and time as a Timestamp
        com.google.firebase.Timestamp currentDateTimestamp = getCurrentTimestamp();

        // Convert the Timestamp to a string format suitable for the ARIMA model
        String formattedDate = formatDateForFirestore(currentDateTimestamp.toDate());

        // Create a map to store transaction details
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("transactionId", transactionId);
        transactionData.put("date", formattedDate); // Store as a string
        transactionData.put("totalCost", totalCost);
        transactionData.put("cashAmount", cashAmount);
        transactionData.put("change", change);

        // Store the transaction in Firestore
        db.collection("transactions").document(transactionId)
                .set(transactionData)
                .addOnSuccessListener(aVoid -> {
                    // Transaction stored successfully, now show the receipt dialog
                    showReceiptDialog(transactionId, currentDateTimestamp.toDate(), totalCost, cashAmount, change);
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to store the transaction
                    Toast.makeText(CashAmountActivity.this, "Error storing transaction. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    // Add this method to convert the date to a string format suitable for storage in Firestore
    private String formatDateForFirestore(Date date) {
        // Format the date as a string with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }


    private void showReceiptDialog(String transactionId, Date currentDate, double totalCost, double cashAmount, double change) {
        // Create a custom dialog
        Dialog receiptDialog = new Dialog(this);
        receiptDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        receiptDialog.setContentView(R.layout.dialog_receipt);

        // Find views in the dialog
        TextView receiptTextView = receiptDialog.findViewById(R.id.receiptTextView);
        Button closeDialogButton = receiptDialog.findViewById(R.id.closeDialogButton);
        Button confirmDialogButton = receiptDialog.findViewById(R.id.confirmDialogButton);

        // Set receipt details
        String receiptMessage = String.format("Transaction ID: %s\n\nDate: %s\n\nTotal Cost: ₱%s\n\nCash Amount: ₱%s\n\nChange: ₱%s", transactionId, formatDate(currentDate), format("%.2f", totalCost), format("%.2f", cashAmount), format("%.2f", change));

        receiptTextView.setText(receiptMessage);

        // Set a listener for the close button
        closeDialogButton.setOnClickListener(v -> {
            // Close the dialog
            receiptDialog.dismiss();

            // Return to the previous activity
            Intent intent = new Intent(CashAmountActivity.this, PurchasedActivity.class);
            startActivity(intent);
        });

        // Set a listener for the confirm button
        confirmDialogButton.setOnClickListener(v -> {
            // Close the dialog
            receiptDialog.dismiss();

            // Start the BrowseProducts activity and clear the back stack
            Intent intent = new Intent(CashAmountActivity.this, BrowseProducts.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            Toast.makeText(this, "Transaction Complete", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });

        // Show the dialog
        receiptDialog.show();
    }

    private com.google.firebase.Timestamp getCurrentTimestamp() {
        // Get the current date and time as a Timestamp
        return com.google.firebase.Timestamp.now();
    }

    private String formatDate(Date date) {
        // Format the date as a string with the default locale
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(date);
    }


    private String generateTransactionId() {
        // Use a combination of timestamp and a UUID to create a unique transaction ID
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();

        // Concatenate the timestamp and UUID, and return as the transaction ID
        return timeStamp + "_" + uuid;
    }

}
