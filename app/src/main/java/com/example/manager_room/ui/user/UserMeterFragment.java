package com.example.manager_room.ui.user;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;


import androidx.core.content.ContextCompat;
import com.example.manager_room.R;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.example.manager_room.data.entity.MeterReading;
import com.example.manager_room.databinding.FragmentUserMeterBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserMeterFragment extends Fragment {
    private FragmentUserMeterBinding binding;
    private UserViewModel userViewModel;
    private MeterHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserMeterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Khởi tạo RecyclerView Lịch sử
        adapter = new MeterHistoryAdapter();
        binding.rvMeterHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMeterHistory.setAdapter(adapter);

        userViewModel.getActiveContract().observe(getViewLifecycleOwner(), contract -> {
            if (contract != null) {
                userViewModel.getMeterReadingsByRoom(contract.getRoomId()).observe(getViewLifecycleOwner(), readings -> {
                    if (readings != null && !readings.isEmpty()) {
                        // 1. Update Card Tóm tắt tháng mới nhất (Thực tế)
                        updateLatestSummaryCard(readings.get(0));

                        // 2. Render Biểu đồ Cột Đôi Song Song (Dual Axis)
                        setupDualAxisGroupedChart(readings);

                        // 3. Render Danh sách lịch sử chi tiết
                        adapter.setList(readings);
                    }
                });
            }
        });
    }

    // Bind dữ liệu thực tế vào 2 Card tóm tắt nhanh ở đầu trang
    private void updateLatestSummaryCard(MeterReading latest) {
        int electricUsed = latest.getNewElectricity() - latest.getOldElectricity();
        int waterUsed = latest.getNewWater() - latest.getOldWater();

        binding.tvElectricTitle.setText("⚡ Điện (T" + latest.getMonth() + "/" + latest.getYear() + "):");
        binding.tvElectricValue.setText(electricUsed + " kWh");

        binding.tvWaterTitle.setText("💧 Nước (T" + latest.getMonth() + "/" + latest.getYear() + "):");
        binding.tvWaterValue.setText(waterUsed + " m³");
    }

    // Dựng Biểu đồ Cột Đôi Song Song 2 Trục Y tách biệt
    private void setupDualAxisGroupedChart(List<MeterReading> rawReadings) {
        BarChart chart = binding.barChartMeter;
        int electricColor = ContextCompat.getColor(requireContext(), R.color.primary_slate_blue);
        int waterColor = ContextCompat.getColor(requireContext(), R.color.stat_blue);
        int textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int gridColor = ContextCompat.getColor(requireContext(), R.color.stroke_light);
        ValueFormatter intFormatter = new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry entry) {
                return String.valueOf((int) entry.getY());
            }
        };
        chart.clear();
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);

        // Đảo ngược danh sách để vẽ theo thứ tự thời gian từ cũ tới mới (Trái sang Phải)
        List<MeterReading> readings = new ArrayList<>(rawReadings);
        Collections.reverse(readings);

        ArrayList<BarEntry> electricEntries = new ArrayList<>();
        ArrayList<BarEntry> waterEntries = new ArrayList<>();
        ArrayList<String> xLabels = new ArrayList<>();

        for (int i = 0; i < readings.size(); i++) {
            MeterReading mr = readings.get(i);
            int electricUsed = mr.getNewElectricity() - mr.getOldElectricity();
            int waterUsed = mr.getOldWater() > 0 ? (mr.getNewWater() - mr.getOldWater()) : mr.getNewWater();

            electricEntries.add(new BarEntry(i, electricUsed));
            waterEntries.add(new BarEntry(i, waterUsed));
            xLabels.add("T" + mr.getMonth());
        }

        // Dataset Điện -> Gán Trục Y bên Trái (LEFT)
        BarDataSet set1 = new BarDataSet(electricEntries, "Điện (kWh)");
        set1.setColor(electricColor);
        set1.setValueTextColor(textColor);
        set1.setValueTextSize(10f);
        set1.setValueFormatter(intFormatter);

        // Dataset Nước -> Gán Trục Y bên Phải (RIGHT)
        BarDataSet set2 = new BarDataSet(waterEntries, "Nước (m³)");
        set2.setColor(waterColor);
        set2.setValueTextColor(textColor);
        set2.setValueTextSize(10f);
        set2.setValueFormatter(intFormatter);

        float groupSpace = 0.30f;
        float barSpace = 0.05f;
        float barWidth = 0.30f; // (0.30 * 2) + 0.05 + 0.30 = 1.00 (Tổng nhóm)

        BarData data = new BarData(set1, set2);
        data.setBarWidth(barWidth);
        chart.setData(data);

        // Định dạng Trục X (Labels Tháng)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(readings.size());

        // Định dạng Trục Y Trái (Điện)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        // Định dạng Trục Y Phải (Nước)
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);


        xAxis.setTextColor(textColor);
        xAxis.setAxisLineColor(gridColor);

        leftAxis.setTextColor(electricColor);
        leftAxis.setGridColor(gridColor);
        leftAxis.setDrawAxisLine(false);

        rightAxis.setTextColor(waterColor);
        rightAxis.setDrawAxisLine(false);

        chart.getLegend().setTextColor(textColor);
        // Kích hoạt tính năng Cột Đôi Song Song (Grouped Bars)
        chart.groupBars(0f, groupSpace, barSpace);
        chart.invalidate(); // Render lại
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}