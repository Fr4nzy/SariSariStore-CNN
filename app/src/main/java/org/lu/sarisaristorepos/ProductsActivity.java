package org.lu.sarisaristorepos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProductsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Button SelectImage, UploadProduct, UploadImage;
    private TextView ShowProducts;
    private EditText Pcat,Pname,Pdesc;
    private ImageView ImageView;
    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseFirestore fireStore;
    private StorageReference mStorageref;
    private String imageUrl, CurrentUserId;
    private FirebaseAuth firebaseAuth;
    private DocumentId DocId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);

        Pname = findViewById(R.id.productName);
        Pcat = findViewById(R.id.productCat);
        Pdesc = findViewById(R.id.productDesc);
        SelectImage = findViewById(R.id.picImg);
        ShowProducts = findViewById(R.id.showProductViewer);

        UploadProduct = findViewById(R.id.btnEnter);
        ImageView = findViewById(R.id.imgView);

        //Instances
        fireStore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorageref = storage.getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        CurrentUserId = firebaseAuth.getCurrentUser().getUid();

        SelectImage.setOnClickListener(v -> {
            if (openFileChooser()) {
                Upload();
            }
        });


        ShowProducts.setOnClickListener(v -> {
        });

        UploadProduct.setOnClickListener(v -> {
            UploadProductInfo();
        });

    }

    private boolean openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ImageView.setImageURI(imageUri);
        }
    }

    //Uploading image into Firebase Firestore

    private void Upload(){

        //check ImageUri
        if (imageUri != null) {
            //create storage instances
            final StorageReference myRef = mStorageref.child("uploads/" + imageUri.getLastPathSegment());
            myRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                //need to getdownloadurl to store in String
                myRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (uri != null){
                            imageUrl = uri.toString();
                            Log.d("ImageUrl", imageUrl); // Add this line to check the imageUrl value
                            UploadProductInfo();
                        }
                    }
                }).addOnFailureListener(e -> Toast.makeText(ProductsActivity.this, "Error Uploading Image", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> {

            });
        }
    }

    // Upload other info of the product
    private void UploadProductInfo() {
        // Check if an image has been selected
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = Pname.getText().toString().trim();
        String category = Pcat.getText().toString().trim();
        String description = Pdesc.getText().toString().trim();

        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(category) && TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {

            DocumentReference myRef = fireStore.collection("Products").document();
            // This will set the necessary data into the Product Class

            ProductClass product = new ProductClass(name, category, description, imageUrl, "", CurrentUserId);
            myRef.set(product, SetOptions.merge()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.isSuccessful()) {
                        String DocId = myRef.getId();
                        product.setDocId(DocId);
                        myRef.set(product, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ProductsActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProductsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProductsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}