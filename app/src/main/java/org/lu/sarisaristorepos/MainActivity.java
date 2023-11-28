package org.lu.sarisaristorepos;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = findViewById(R.id.lineChart);

        // Fetch data from Firestore and perform SMA analysis
        fetchDataFromFirestore();
    }

    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transactions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Entry> entries = new ArrayList<>();

                            // Extract and sort data by date
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Date date = document.getDate("date");
                                double totalCost = document.getDouble("totalCost");
                                entries.add(new Entry(date.getTime(), (float) totalCost));
                            }

                            // Sort entries by date
                            Collections.sort(entries, new Comparator<Entry>() {
                                @Override
                                public int compare(Entry entry1, Entry entry2) {
                                    return Float.compare(entry1.getX(), entry2.getX());
                                }
                            });

                            // Perform SMA analysis
                            ArrayList<Entry> smaEntries = calculateSMA(entries, 3); // Adjust the period as needed

                            // Display data in the line chart
                            displayLineChart(smaEntries);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(MainActivity.this, "Error fetching data from Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private ArrayList<Entry> calculateSMA(ArrayList<Entry> entries, int period) {
        ArrayList<Entry> smaEntries = new ArrayList<>();
        float sum = 0;

        for (int i = 0; i < entries.size(); i++) {
            sum += entries.get(i).getY();

            if (i >= period - 1) {
                float smaValue = sum / period;
                smaEntries.add(new Entry(entries.get(i).getX(), smaValue));
                sum -= entries.get(i - period + 1).getY();
            }
        }

        return smaEntries;
    }

    private void displayLineChart(ArrayList<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "SMA Data");

        // Set line color to white
        dataSet.setColor(Color.WHITE);

        // Customize legend appearance
        dataSet.setFormLineWidth(0f); // Hide the legend form
        dataSet.setFormSize(0f); // Hide the legend form
        dataSet.setDrawValues(true); // Show legend values

        // Set legend text color to white
        dataSet.setValueTextColor(Color.WHITE);

        // Customize chart appearance
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize chart description
        Description description = new Description();
        description.setText("SMA Analysis");

        // Set description text color to white
        description.setTextColor(Color.WHITE);

        lineChart.setDescription(description);

        // Set chart background color to black
        lineChart.setBackgroundColor(Color.BLACK);

        // Refresh the chart
        lineChart.invalidate();
    }


}
