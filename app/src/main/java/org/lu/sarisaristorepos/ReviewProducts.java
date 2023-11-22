package org.lu.sarisaristorepos;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ReviewProducts extends AppCompatActivity {

    private TextView totalProductsTextView;
    private TextView totalTransactionsTextView;
    private FirebaseFirestore db;
    private String selectedTimePeriod = "Today";
    private List<TextView> categoryTextViews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_products);

        totalProductsTextView = findViewById(R.id.totalProducts);
        totalTransactionsTextView = findViewById(R.id.totalTransactions);

        db = FirebaseFirestore.getInstance();

        categoryTextViews = new ArrayList<>();
        categoryTextViews.add(findViewById(R.id.totalProductsCannedGoods));
        categoryTextViews.add(findViewById(R.id.totalProductsCondiments));
        categoryTextViews.add(findViewById(R.id.totalProductsInstantNoodles));
        categoryTextViews.add(findViewById(R.id.totalProductsPowderedBeverages));
        categoryTextViews.add(findViewById(R.id.totalProductsOthers));

        Spinner timePeriodSpinner = findViewById(R.id.timePeriodSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_periods_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        timePeriodSpinner.setAdapter(adapter);

        // Inside onCreate or where you set up your Spinner
        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // Get the selected item from the Spinner
                selectedTimePeriod = adapterView.getItemAtPosition(position).toString();

                // Call the displayTotalProductsAndTransactions method to update the UI based on the selected time period
                displayTotalProductsAndTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case where nothing is selected, if needed
            }
        });

        // Call the displayTotalProductsAndTransactions method to show the data for "Today" by default
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
            for (int i = 0; i < querySnapshotsList.size(); i++) {
                QuerySnapshot snapshot = (QuerySnapshot) querySnapshotsList.get(i);
                int categoryCount = snapshot.size();
                totalProducts.addAndGet(categoryCount);
                updateTotalProductsTextView(i, categoryCount);
            }

            // Check the selected time period
            if ("Today".equals(selectedTimePeriod) || "This Week".equals(selectedTimePeriod)) {
                // Calculate the start date for the past 7 days
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                Date sevenDaysAgo = calendar.getTime();

                // Create a new task to get the total transaction count for the past 7 days
                Task<QuerySnapshot> transactionTask = db.collection("transactions")
                        .whereGreaterThanOrEqualTo("date", sevenDaysAgo)
                        .get();

                transactionTask.addOnSuccessListener(transactionSnapshot -> {
                    totalTransactions.set(getTransactionCount(transactionSnapshot));

                    // Add these log statements
                    Log.d("ReviewProducts", "Total Products: " + totalProducts.get());
                    Log.d("ReviewProducts", "Total Transactions: " + totalTransactions.get());

                    // Display the total number of products and transactions
                    totalProductsTextView.setText(getString(R.string.total_products_label, totalProducts.get()));
                    totalTransactionsTextView.setText(getString(R.string.total_transactions_label, totalTransactions.get()));
                }).addOnFailureListener(e -> Log.e("ReviewProducts", "Error getting transactions", e));
            } else {
                // If the selected time period is not "Today" or "This Week," clear the total transactions TextView
                totalTransactionsTextView.setText("");
            }
        }).addOnFailureListener(e -> Log.e("ReviewProducts", "Error getting products", e));
    }


    private void updateTotalProductsTextView(int index, int count) {
        if (index < categoryTextViews.size()) {
            TextView categoryTextView = categoryTextViews.get(index);

            // Get the category name based on the index
            String[] categoryNames = {"Canned Goods", "Condiments", "Instant Noodles", "Powdered Beverages", "Others"};
            String categoryName = categoryNames[index];

            // Set the text to display the category name and count
            categoryTextView.setText(getString(R.string.category_count_label, categoryName, count));
        }
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
