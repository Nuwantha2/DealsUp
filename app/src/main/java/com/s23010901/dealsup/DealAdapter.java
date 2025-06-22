package com.s23010901.dealsup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    private final Context context;
    private List<Deal> dealList;

    public DealAdapter(Context context, List<Deal> dealList) {
        this.context = context;
        this.dealList = dealList;
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_deal, parent, false);
        return new DealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        Deal deal = dealList.get(position);

        holder.tvTitle.setText(deal.getTitle());
        holder.tvDescription.setText(deal.getDescription());

        // Load logo image
        Glide.with(context)
                .load(deal.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imgLogo);

        // Handle arrow icon click
        holder.arrowIcon.setOnClickListener(v -> {
            Intent intent = new Intent(context, dealMap.class);
            intent.putExtra("title", deal.getTitle());
            intent.putExtra("description", deal.getDescription());
            intent.putExtra("imageUrl", deal.getImageUrl());
            intent.putExtra("latitude", deal.getLatitude());
            intent.putExtra("longitude", deal.getLongitude());
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, dealMap.class);
            intent.putExtra("title", deal.getTitle());
            intent.putExtra("description", deal.getDescription());
            intent.putExtra("imageUrl", deal.getImageUrl());
            intent.putExtra("latitude", deal.getLatitude());
            intent.putExtra("longitude", deal.getLongitude());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dealList.size();
    }

    public void updateList(List<Deal> filteredList) {
        this.dealList = filteredList;
        notifyDataSetChanged();
    }

    static class DealViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo, arrowIcon;
        TextView tvTitle, tvDescription;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.imgLogo);
            arrowIcon = itemView.findViewById(R.id.imgArrow);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
