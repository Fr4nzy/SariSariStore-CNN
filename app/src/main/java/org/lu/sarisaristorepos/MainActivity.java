package org.lu.sarisaristorepos;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

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
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Entry> entries = new ArrayList<>();

                        // Extract and sort data by date
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dateString = document.getString("Date");
                            Date date = parseDateString(dateString);
                            if (date != null) {
                                double totalCost = document.getDouble("Sales");
                                entries.add(new Entry(date.getTime(), (float) totalCost));
                            }
                        }

                        // Sort entries by date
                        Collections.sort(entries, (entry1, entry2) -> Float.compare(entry1.getX(), entry2.getX()));

                        // Perform SMA analysis
                        ArrayList<Entry> smaEntries = calculateSMA(entries); // Adjust the period as needed

                        // Display data in the line chart
                        displayLineChart(smaEntries);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(MainActivity.this, "Error fetching data from Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Date parseDateString(String dateString) {
        try {
            if (dateString != null && !dateString.isEmpty()) {
                // Assuming the date format is MM/dd/yy
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.US);
                return dateFormat.parse(dateString);
            } else {
                // Handle the case where dateString is null or empty
                Log.e(TAG, "Date string is null or empty");
                return null;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date string: " + dateString, e);
            return null;
        }
    }

    private ArrayList<Entry> calculateSMA(ArrayList<Entry> entries) {
        ArrayList<Entry> smaEntries = new ArrayList<>();
        float sum = 0;

        for (int i = 0; i < entries.size(); i++) {
            sum += entries.get(i).getY();

            if (i >= 3 - 1) {
                float smaValue = sum / 3;
                smaEntries.add(new Entry(entries.get(i).getX(), smaValue));
                sum -= entries.get(i - 3 + 1).getY();
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

        // Customize XAxis (bottom axis) text color and label
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getXAxis().setValueFormatter(new DateValueFormatter()); // Use a custom DateValueFormatter for formatting dates

        // Customize YAxis (left axis) text color
        lineChart.getAxisLeft().setTextColor(Color.WHITE);

        // Refresh the chart
        lineChart.invalidate();
    }




}
