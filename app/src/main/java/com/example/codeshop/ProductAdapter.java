package com.example.codeshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.codeshop.model.ProductModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private ArrayList<ProductModel> products;

    public ProductAdapter(Context context, ArrayList<ProductModel> products){
        this.context = context;
        this.products = products;
    }


    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.title.setText(products.get(position).getProdName());
        holder.price.setText("\u20b9"+products.get(position).getProdPrice());
        Picasso.get().load(products.get(position).getProdImageLink()).into(holder.imageView);
        holder.quantity.setText(String.valueOf(products.get(position).getQuantity()));
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.quantity.setText(String.valueOf(products.get(position).increaseQuantity()));
            }
        });
        holder.subtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.quantity.setText(String.valueOf(products.get(position).decreaseQuantity()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public TextView title;
        public TextView price;
        public TextView quantity;
        public ImageView add,subtract;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImage);
            title = itemView.findViewById(R.id.prodName);
            price = itemView.findViewById(R.id.prodPrice);
            quantity = itemView.findViewById(R.id.quantity);
            add = itemView.findViewById(R.id.add);
            subtract = itemView.findViewById(R.id.subtract);

        }
    }
}
