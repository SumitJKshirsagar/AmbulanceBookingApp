package com.example.ambulanceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HospitalAddressAdapter extends RecyclerView.Adapter<HospitalAddressAdapter.ViewHolder> {
    private List<String> hospitalAddresses;

    public HospitalAddressAdapter(List<String> hospitalAddresses) {
        this.hospitalAddresses = hospitalAddresses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hospital_address_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String address = hospitalAddresses.get(position);
        holder.textAddress.setText(address);
    }

    @Override
    public int getItemCount() {
        return hospitalAddresses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textAddress = itemView.findViewById(R.id.text_address);
        }
    }
}
