package org.lu.sarisaristorepos;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ReviewProducts extends AppCompatActivity {

    private TextView totalProductsTextView;
    private TextView totalTransactionsTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_products);

        totalProductsTextView = findViewById(R.id.totalProducts);
        totalTransactionsTextView = findViewById(R.id.totalTransactions);

        db = FirebaseFirestore.getInstance();

        // Call the method to fetch and display total products and transactions
        displayTotalProductsAndTransactions();
    }

    private void displayTotalProductsAndTransactions() {
        // List of collections to load products from
        String[] allCollections = {"Canned Goods", "Condiments", "Powdered Beverages", "Instant Noodles", "Others"};

        // Counter to track the number of products and transactions
        AtomicInteger totalProducts = new AtomicInteger();
        AtomicInteger totalTransactions = new AtomicInteger();

        // Create a list to store all tasks
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // Iterate through each collection and create a task for each
        for (String collection : allCollections) {
            Task<QuerySnapshot> task = db.collection(collection).get();
            tasks.add(task);
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(querySnapshotsList -> {
            // Iterate through each query snapshot list and update product count
            for (Object querySnapshots : querySnapshotsList) {
                if (querySnapshots instanceof QuerySnapshot) {
                    QuerySnapshot snapshot = (QuerySnapshot) querySnapshots;
                    totalProducts.addAndGet(snapshot.size());
                }
            }

            // Create a new task to get the transaction count
            Task<QuerySnapshot> transactionTask = db.collection("transactions").get();
            transactionTask.addOnSuccessListener(transactionSnapshot -> {
                totalTransactions.set(getTransactionCount(transactionSnapshot));

                // Display the total number of products and transactions
                totalProductsTextView.setText("Total Products: " + totalProducts);
                totalTransactionsTextView.setText("Total Transactions: " + totalTransactions);
            }).addOnFailureListener(e -> {
                Log.e("ReviewProducts", "Error getting transactions", e);
            });
        }).addOnFailureListener(e -> {
            Log.e("ReviewProducts", "Error getting products", e);
        });
    }


    private int getTransactionCount(QuerySnapshot snapshot) {
        Log.d("TransactionCountDebug", "DocumentChanges count: " + snapshot.getDocumentChanges().size());

        Set<String> uniqueTransactions = new HashSet<>();

        for (DocumentChange documentChange : snapshot.getDocumentChanges()) {
            // Check if the "transactionId" field exists in the document
            if (documentChange.getDocument().contains("transactionId")) {
                String transactionId = documentChange.getDocument().getString("transactionId");

                // Log the document details for debugging
                Log.d("TransactionCountDebug", "Document ID: " + documentChange.getDocument().getId());
                Log.d("TransactionCountDebug", "Document Data: " + documentChange.getDocument().getData());

                // Add the transactionId to the set if it's not null
                if (transactionId != null) {
                    Log.d("TransactionCountDebug", "TransactionId: " + transactionId);
                    uniqueTransactions.add(transactionId);
                }
            } else {
                // Log a message if the "transactionId" field is missing in the document
                Log.d("TransactionCountDebug", "Document ID: " + documentChange.getDocument().getId() +
                        " does not contain 'transactionId' field.");
            }
        }

        int count = uniqueTransactions.size();
        Log.d("TransactionCountDebug", "Unique Transactions count: " + count);

        return count;
    }



}
