package org.lu.sarisaristorepos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class PointOfSaleActivity extends AppCompatActivity {

    Button browseProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_of_sale);

        browseProducts = findViewById(R.id.browseProducts);

        browseProducts.setOnClickListener(view -> {
            BrowseProducts();
            finish();
        });
    }

    private void BrowseProducts(){
        Intent intent = new Intent(this, BrowseProducts.class);
        startActivity(intent);
    }
}