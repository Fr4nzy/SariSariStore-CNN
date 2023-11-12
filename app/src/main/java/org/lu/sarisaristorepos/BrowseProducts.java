package org.lu.sarisaristorepos;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class BrowseProducts extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_products);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();

        // Load products from Firestore
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        String name = documentChange.getDocument().getString("name");
                        String price = documentChange.getDocument().getString("price");
                        String imageURL = documentChange.getDocument().getString("imageURL");
                        String stocks = documentChange.getDocument().getString("stocks");

                        productList.add(new Product(name, price, imageURL, stocks));
                    }
                    productAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
