package org.lu.sarisaristorepos;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
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
    Button pos, productInsertDeleteReview, logout;

    private static final String TAG = "MainActivity";
    private LineChart lineChart;
    private ArrayList<Entry> entries; // Store original entries for reset
    private int selectedPeriod = 1; // Set a default value for selectedPeriod
    private boolean dataFetched = false; // Flag to track whether data has been fetched

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = findViewById(R.id.lineChart);
        Spinner periodSpinner = findViewById(R.id.periodSpinner);
        pos = findViewById(R.id.posBtn);
        productInsertDeleteReview = findViewById(R.id.productsBtn);
        logout = findViewById(R.id.logoutBtn);

        pos.setOnClickListener(v -> PointOfSale());
        productInsertDeleteReview.setOnClickListener(view -> InsertDeleteReview());
        logout.setOnClickListener(v -> logout());


        // Fetch data from Firestore and perform SMA analysis
        fetchDataFromFirestore();

        // Set up the Spinner with period options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.period_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapter);

        // Set up Spinner item selection listener
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle selection change, update the chart accordingly
                selectedPeriod = Integer.parseInt(parentView.getItemAtPosition(position).toString());
                updateChart(entries, selectedPeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
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

    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transactions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Initialize entries ArrayList
                        entries = new ArrayList<>();

                        // Extract and sort data by date
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dateString = document.getString("Date");
                            Date date = parseDateString(dateString);

                            // Check if "Sales" field is not null
                            Double salesValue = document.getDouble("Sales");
                            if (date != null && salesValue != null) {
                                double totalCost = salesValue;
                                entries.add(new Entry(date.getTime(), (float) totalCost));
                            }
                        }


                        // Store original entries for reset
                        MainActivity.this.entries = new ArrayList<>(entries);

                        // Sort entries by date
                        Collections.sort(entries, (entry1, entry2) -> Float.compare(entry1.getX(), entry2.getX()));

                        // Perform SMA analysis
                        ArrayList<Entry> smaEntries = calculateSMA(entries, selectedPeriod);

                        // Display data in the line chart
                        displayLineChart(smaEntries);

                        // Set the flag to true, indicating that data has been fetched
                        dataFetched = true;
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(MainActivity.this, "Error fetching data from Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private Date parseDateString(String dateString) {
        try {
            if (dateString != null && !dateString.isEmpty()) {
                // Assuming the date format is dd/MM/yy
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

    private ArrayList<Entry> calculateSMA(ArrayList<Entry> entries, int period) {
        if (entries == null || entries.size() == 0) {
            return new ArrayList<>(); // Return an empty list if entries is null or empty
        }

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

    private void updateChart(ArrayList<Entry> entries, int selectedPeriod) {
        if (entries != null) {
            // Perform SMA analysis with the selected period
            ArrayList<Entry> smaEntries = calculateSMA(entries, selectedPeriod);

            // Display updated data in the line chart
            displayLineChart(smaEntries);
        }
    }

    private void displayLineChart(ArrayList<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "");

        // Set chart content offsets to increase padding
        lineChart.setViewPortOffsets(80f, 100f, 80f, 100f);

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
        lineChart.setBackgroundColor(Color.DKGRAY);

        // Customize XAxis (bottom axis) text color and label
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getXAxis().setValueFormatter(new DateValueFormatter()); // Use a custom DateValueFormatter for formatting dates
        lineChart.getXAxis().setGranularity(250f);

        // Adjust the spacing of X-axis labels
        lineChart.getXAxis().setLabelCount(15, true); // Set the label count to the number of entries
        lineChart.getXAxis().setGranularity(1f); // Set the minimum interval between axis values to 1

        // Rotate X-axis labels for better readability
        lineChart.getXAxis().setLabelRotationAngle(-45f);


        // Customize YAxis (left axis) text color, position, and grid granularity
        YAxis leftYAxis = lineChart.getAxisLeft();
        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setEnabled(false);
        leftYAxis.setTextColor(Color.WHITE);
        leftYAxis.setYOffset(15f); // Adjust the offset as needed
        leftYAxis.setGranularity(500f); // Set the granularity to control the spacing of grid lines


        // Refresh the chart
        lineChart.invalidate();
    }

    public boolean isDataFetched() {
        return dataFetched;
    }

    public void setDataFetched(boolean dataFetched) {
        this.dataFetched = dataFetched;
    }
}