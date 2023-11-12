package org.lu.sarisaristorepos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button pos, product, browse, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pos = findViewById(R.id.posBtn);
        product = findViewById(R.id.productsBtn);
        browse = findViewById(R.id.browseProducts);
        logout = findViewById(R.id.logoutBtn);

        pos.setOnClickListener(v -> PointOfSale());
        product.setOnClickListener(view -> ManageProducts());
        browse.setOnClickListener(view -> BrowseProducts());
        logout.setOnClickListener(v -> finish());

    }

    private void PointOfSale() {
        Intent intent = new Intent(this, PointOfSaleActivity.class);
        startActivity(intent);

    }
    private void ManageProducts(){
        Intent intent = new Intent(this, ManageProducts.class);
        startActivity(intent);
    }

    private void BrowseProducts(){
        Intent intent = new Intent(this, BrowseProducts.class);
        startActivity(intent);
    }
}