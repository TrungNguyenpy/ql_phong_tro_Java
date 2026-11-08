package com.example.manager_room.ui.user;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manager_room.data.entity.MeterReading;
import com.example.manager_room.databinding.ItemMeterHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class MeterHistoryAdapter extends RecyclerView.Adapter<MeterHistoryAdapter.MeterViewHolder> {

    private List<MeterReading> list = new ArrayList<>();

    public void setList(List<MeterReading> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMeterHistoryBinding binding = ItemMeterHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MeterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MeterViewHolder holder, int position) {
        MeterReading mr = list.get(position);
        int electricUsed = mr.getNewElectricity() - mr.getOldElectricity();
        int waterUsed = mr.getNewWater() - mr.getOldWater();

        holder.binding.tvMeterMonth.setText("T" + mr.getMonth() + "/" + mr.getYear());
        holder.binding.tvMeterUsage.setText(electricUsed + " kWh | " + waterUsed + " m³");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MeterViewHolder extends RecyclerView.ViewHolder {
        ItemMeterHistoryBinding binding;

        public MeterViewHolder(@NonNull ItemMeterHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}