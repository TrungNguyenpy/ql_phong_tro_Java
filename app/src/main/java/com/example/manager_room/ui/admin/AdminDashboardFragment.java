package com.example.manager_room.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.manager_room.R;
import com.example.manager_room.data.entity.Invoice;
import com.example.manager_room.data.entity.TenantWithUser;
import com.example.manager_room.databinding.FragmentAdminDashboardBinding;
import com.example.manager_room.databinding.ItemSheetRowBinding;
import com.example.manager_room.databinding.SheetListBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.manager_room.util.LandlordInfo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class AdminDashboardFragment extends Fragment {
    private FragmentAdminDashboardBinding binding;
    private AdminViewModel adminViewModel;

    private int availableCount = 0, rentedCount = 0;
    private List<TenantWithUser> tenantList = new ArrayList<>();
    private List<Invoice> invoiceList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        adminViewModel.getRoomCountByStatus("AVAILABLE").observe(getViewLifecycleOwner(), count -> {
            availableCount = count != null ? count : 0;
            binding.tvAvailableRooms.setText(String.valueOf(availableCount));
            binding.tvTotalRooms.setText(String.valueOf(availableCount + rentedCount));
        });

        adminViewModel.getRoomCountByStatus("RENTED").observe(getViewLifecycleOwner(), count -> {
            rentedCount = count != null ? count : 0;
            binding.tvRentedRooms.setText(String.valueOf(rentedCount));
            binding.tvTotalRooms.setText(String.valueOf(availableCount + rentedCount));
        });

        adminViewModel.getUnpaidInvoices().observe(getViewLifecycleOwner(), unpaidList -> {
            if (unpaidList != null) {
                binding.tvUnpaidSummary.setText("Hiện có " + unpaidList.size() + " hóa đơn chưa thu tiền");
            }
        });

        adminViewModel.getAllInvoices().observe(getViewLifecycleOwner(), invoices -> {
            invoiceList = invoices != null ? invoices : new ArrayList<>();
            double revenue = 0;
            for (Invoice inv : invoiceList) {
                if (inv.isPaid()) revenue += inv.getTotalAmount();
            }
            binding.tvRevenue.setText(formatCompact(revenue));
        });

        adminViewModel.getAllTenantsWithUsers().observe(getViewLifecycleOwner(), tenants -> {
            if (tenants != null) tenantList = tenants;
        });

        binding.cardRented.setOnClickListener(v -> showTenantsSheet());
        binding.cardRevenue.setOnClickListener(v -> showRevenueSheet());
    }

    private String formatCompact(double value) {
        if (value >= 1_000_000_000) return String.format(Locale.US, "%.1fB", value / 1_000_000_000);
        if (value >= 1_000_000) return String.format(Locale.US, "%.1fM", value / 1_000_000);
        if (value >= 1_000) return String.format(Locale.US, "%.0fK", value / 1_000);
        return String.format(Locale.US, "%.0f", value);
    }

    private String money(double v) {
        return String.format(new Locale("vi", "VN"), "%,.0fđ", v);
    }

    private void showTenantsSheet() {
        SheetListBinding sb = SheetListBinding.inflate(getLayoutInflater());
        sb.tvSheetTitle.setText("Người thuê đã đăng ký");
        sb.tvSheetSubtitle.setText(tenantList.size() + " tài khoản");
        sb.tvSheetEmpty.setVisibility(tenantList.isEmpty() ? View.VISIBLE : View.GONE);

        for (TenantWithUser item : tenantList) {
            ItemSheetRowBinding row = ItemSheetRowBinding.inflate(getLayoutInflater(), sb.llSheetItems, false);
            String name = (item.user != null && item.user.getFullName() != null) ? item.user.getFullName() : "Chưa đặt tên";
            String phone = (item.user != null && item.user.getPhone() != null) ? item.user.getPhone() : "Chưa cập nhật";
            String cccd = (item.tenant != null && item.tenant.getIdentityCard() != null) ? item.tenant.getIdentityCard() : "Chưa cập nhật";

            row.tvRowAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
            row.tvRowTitle.setText(name);
            row.tvRowSub.setText("SĐT: " + phone + "\nCCCD: " + cccd);
            row.tvRowAmount.setVisibility(View.GONE);
            sb.llSheetItems.addView(row.getRoot());
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(sb.getRoot());
        dialog.show();
    }

    private void showRevenueSheet() {
        SheetListBinding sb = SheetListBinding.inflate(getLayoutInflater());

        // key = năm*100 + tháng, mới nhất trước; value = {đã thu, chưa thu, số hóa đơn}
        Map<Integer, double[]> byMonth = new TreeMap<>(Collections.reverseOrder());
        double totalPaid = 0;
        for (Invoice inv : invoiceList) {
            int key = (int) (inv.getYear() * 100 + inv.getMonth());
            double[] agg = byMonth.get(key);
            if (agg == null) {
                agg = new double[3];
                byMonth.put(key, agg);
            }
            if (inv.isPaid()) {
                agg[0] += inv.getTotalAmount();
                totalPaid += inv.getTotalAmount();
            } else {
                agg[1] += inv.getTotalAmount();
            }
            agg[2] += 1;
        }

        sb.tvSheetTitle.setText("Lịch sử doanh thu");
        sb.tvSheetSubtitle.setText("Tổng đã thu: " + money(totalPaid));
        sb.tvSheetEmpty.setVisibility(byMonth.isEmpty() ? View.VISIBLE : View.GONE);

        int green = ContextCompat.getColor(requireContext(), R.color.status_green);
        for (Map.Entry<Integer, double[]> e : byMonth.entrySet()) {
            double[] agg = e.getValue();
            ItemSheetRowBinding row = ItemSheetRowBinding.inflate(getLayoutInflater(), sb.llSheetItems, false);
            row.tvRowAvatar.setVisibility(View.GONE);
            row.tvRowTitle.setText("Tháng " + (e.getKey() % 100) + "/" + (e.getKey() / 100));
            row.tvRowSub.setText((int) agg[2] + " hóa đơn" + (agg[1] > 0 ? " · chưa thu " + money(agg[1]) : ""));
            row.tvRowAmount.setText(money(agg[0]));
            row.tvRowAmount.setTextColor(green);
            sb.llSheetItems.addView(row.getRoot());
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(sb.getRoot());
        dialog.show();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) return;
        String ownerName = new LandlordInfo(requireContext()).get(LandlordInfo.NAME);
        if (!ownerName.isEmpty()) binding.tvOwnerName.setText(ownerName);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}