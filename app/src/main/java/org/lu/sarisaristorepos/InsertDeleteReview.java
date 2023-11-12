package org.lu.sarisaristorepos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class InsertDeleteReview extends AppCompatActivity {

    Button add,delete,review;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_delete_review);

        add = findViewById(R.id.btnAdd);
        delete = findViewById(R.id.btnRemove);
        review = findViewById(R.id.btnReview);

        add.setOnClickListener(view -> setAdd());
        delete.setOnClickListener(view -> setDelete());
    }

    private void setAdd(){
        Intent intent = new Intent(this, AddProducts.class);
        startActivity(intent);
    }
    private void setDelete(){
        Intent intent = new Intent(this, DeleteProducts.class);
        startActivity(intent);
    }

}