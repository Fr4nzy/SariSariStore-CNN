package org.lu.sarisaristorepos;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class PlaceOrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        // Retrieve the selected item's information from the intent
        Intent intent = getIntent();
        String productName = intent.getStringExtra("productName");
        String productPrice = intent.getStringExtra("productPrice");
        String productImageURL = intent.getStringExtra("productImageURL");
        String productCategory = intent.getStringExtra("productCategory");

        // Set the information in the TextViews
        TextView productNameTextView = findViewById(R.id.productName);
        TextView productPriceTextView = findViewById(R.id.productPrice);
        TextView productCategoryTextView = findViewById(R.id.categorySelect);
        ImageView productImageView = findViewById(R.id.previewImg);


        productNameTextView.setText(productName);
        productPriceTextView.setText(productPrice);
        productCategoryTextView.setText(productCategory);

        // Load the product image using Glide
        Glide.with(this)
                .load(productImageURL)
                .into(productImageView);

    }


}