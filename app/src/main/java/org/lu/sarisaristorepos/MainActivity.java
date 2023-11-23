package org.lu.sarisaristorepos;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    Button pos, productInsertDeleteReview, predict, logout;
    LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pos = findViewById(R.id.posBtn);
        productInsertDeleteReview = findViewById(R.id.productsBtn);
        predict = findViewById(R.id.predict_btn);
        logout = findViewById(R.id.logoutBtn);
        lineChart = findViewById(R.id.lineChart);

        pos.setOnClickListener(v -> PointOfSale());
        productInsertDeleteReview.setOnClickListener(view -> InsertDeleteReview());
        predict.setOnClickListener(view -> fetchAndDisplayData());
        logout.setOnClickListener(v -> logout());

        // After initializing the LineChart, log the chart reference
        Log.d("ChartReference", "LineChart: " + lineChart);

    }

    private void PointOfSale() {
        Intent intent = new Intent(this, PointOfSaleActivity.class);
        startActivity(intent);
    }

    private void InsertDeleteReview() {
        Intent intent = new Intent(this, InsertDeleteReview.class);
        startActivity(intent);
    }

    private void logout() {
        // Finish all activities in the back stack
        finishAffinity();
    }

    private void fetchAndDisplayData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the "predictions" collection without ordering or limiting
        db.collection("predictions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Retrieve the first document (assuming predictions are already in the desired order)
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                            List<Map<String, Object>> predictions = (List<Map<String, Object>>) document.get("predictions");
                            if (predictions != null) {
                                Log.d("FirestoreData", "Predictions Data: " + predictions);

                                // Convert predictions to data entries
                                List<Entry> entries = new ArrayList<>();
                                for (int i = 0; i < predictions.size(); i++) {
                                    Map<String, Object> prediction = predictions.get(i);
                                    float predictionValue = Float.parseFloat(((List<?>) prediction.get("prediction")).get(0).toString());
                                    entries.add(new Entry(i, predictionValue));
                                }

                                // Customize chart appearance if needed

                                // Create a LineDataSet from the entries
                                LineDataSet dataSet = new LineDataSet(entries, "Total Cost"); // Set label to "Total Cost"
                                dataSet.setValueTextColor(Color.WHITE); // Set text color to white
                                dataSet.setValueTextSize(12f); // Set text size

                                // Set x-axis and y-axis text color
                                lineChart.getXAxis().setTextColor(Color.WHITE);
                                lineChart.getAxisLeft().setTextColor(Color.WHITE);
                                lineChart.getAxisRight().setTextColor(Color.WHITE);

                                // Set legend label color
                                lineChart.getLegend().setTextColor(Color.WHITE);

                                // Set description
                                lineChart.getDescription().setText("Predicted Sales");
                                lineChart.getDescription().setTextColor(Color.WHITE);

                                LineData lineData = new LineData(dataSet);

                                // Set the data to the chart
                                lineChart.setData(lineData);

                                // Refresh the chart
                                lineChart.invalidate();
                            } else {
                                // Handle the case when predictions is null
                                Toast.makeText(MainActivity.this, "Predictions data is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "No documents found in the 'predictions' collection", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}
