package org.lu.sarisaristorepos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        db.collection("transactions")
                .document("yourDocId") // replace with your actual document ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            List<Map<String, Object>> predictions = (List<Map<String, Object>>) document.get("predictions");
                            if (predictions != null){
                                // Convert predictions to data entries
                                List<Entry> entries = new ArrayList<>();
                                for (int i = 0; i < predictions.size(); i++) {
                                    Map<String, Object> prediction = predictions.get(i);
                                    float predictionValue = Float.parseFloat(prediction.get("prediction").toString());
                                    entries.add(new Entry(i, predictionValue));
                                }

                                // Create a LineChart
                                LineChart lineChart = findViewById(R.id.lineChart);

                                // Create a LineDataSet from the entries
                                LineDataSet dataSet = new LineDataSet(entries, "Label"); // You can set a label for your data
                                LineData lineData = new LineData(dataSet);

                                // Set the data to the chart
                                lineChart.setData(lineData);

                                // Customize chart appearance if needed
                                // ...

                                // Refresh the chart
                                lineChart.invalidate();

                            } else {
                                // Handle the case when predictions is null
                                Toast.makeText(MainActivity.this, "Predictions data is null", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
