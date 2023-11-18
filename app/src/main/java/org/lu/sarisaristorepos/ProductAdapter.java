package org.lu.sarisaristorepos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;
import android.widget.CompoundButton;
import android.widget.CheckBox;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private ProductSelectionListener productSelectionListener;

    // Modify the constructor to accept an optional listener
    public ProductAdapter(List<Product> productList, ProductSelectionListener listener) {
        this.productList = productList;
        this.productSelectionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Product product = productList.get(position); // Make 'product' final

        holder.productName.setText(product.getName());
        holder.productPrice.setText(product.getPrice());
        holder.productCategory.setText(product.getCategory());


        // Load the product image using Glide
        Glide.with(holder.productImage.getContext())
                .load(product.getImageURL())
                .apply(new RequestOptions().centerCrop())
                .into(holder.productImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start PlaceOrderActivity when the item is clicked
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, PlaceOrderActivity.class);
                product.setSelected(!product.isSelected());

                // Notify the listener when the selection changes
                if (productSelectionListener != null) {
                    productSelectionListener.onProductSelectionChanged();
                }
                // Pass the selected item's information as extras
                intent.putExtra("productName", product.getName());
                intent.putExtra("productPrice", product.getPrice());
                intent.putExtra("productImageURL", product.getImageURL());
                intent.putExtra("productCategory", product.getCategory());

                ((Activity) context).startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productCategory;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.categoryInfo);
        }
    }

    // Define an interface to handle product selection changes
    public interface ProductSelectionListener {
        void onProductSelectionChanged();
    }
    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

}