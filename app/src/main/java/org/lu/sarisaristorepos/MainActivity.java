package org.lu.sarisaristorepos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button pos, productInsertDeleteReview, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pos = findViewById(R.id.posBtn);
        productInsertDeleteReview = findViewById(R.id.productsBtn);

        logout = findViewById(R.id.logoutBtn);

        pos.setOnClickListener(v -> PointOfSale());
        productInsertDeleteReview.setOnClickListener(view -> InsertDeleteReview());

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
}
