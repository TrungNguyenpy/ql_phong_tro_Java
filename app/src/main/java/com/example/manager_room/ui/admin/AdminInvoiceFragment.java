package com.example.manager_room.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.manager_room.R;
import com.example.manager_room.data.entity.Invoice;
import com.example.manager_room.data.entity.InvoiceWithContractAndRoom;
import com.example.manager_room.databinding.FragmentAdminInvoiceBinding;
import com.example.manager_room.ui.user.InvoiceAdapter;
import com.example.manager_room.util.PdfExporter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminInvoiceFragment extends Fragment {
    private FragmentAdminInvoiceBinding binding;
    private AdminViewModel adminViewModel;
    private InvoiceAdapter invoiceAdapter;
    private List<InvoiceWithContractAndRoom> allInvoices = new ArrayList<>();
    private int tabIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminInvoiceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        invoiceAdapter = new InvoiceAdapter(new InvoiceAdapter.OnInvoiceClickListener() {
            @Override
            public void onExportPdf(Invoice invoice) {
                PdfExporter.exportInvoiceToPdf(requireContext(), invoice, "Phòng trọ", "Khách thuê");
            }

            @Override
            public void onMarkPaid(Invoice invoice) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Xác Nhận Thanh Toán")
                        .setMessage("Bạn có chắc chắn muốn xác nhận Hóa đơn Tháng " + invoice.getMonth() + "/" + invoice.getYear()
                                + " (Số tiền: " + String.format(new Locale("vi", "VN"), "%,.0f đ", invoice.getTotalAmount())
                                + ") ĐÃ THANH TOÁN?")
                        .setPositiveButton("Xác nhận", (dialog, which) ->
                                adminViewModel.markInvoiceAsPaid(invoice.getId(), new AdminViewModel.ActionCallback() {
                                    @Override
                                    public void onSuccess() {
                                        toastOnUi("Cập nhật thanh toán thành công!");
                                    }

                                    @Override
                                    public void onError(String error) {
                                        toastOnUi(error);
                                    }
                                }))
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onDeleteInvoice(Invoice invoice) {
                adminViewModel.deleteInvoice(invoice, new AdminViewModel.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        toastOnUi("Đã xóa hóa đơn!");
                    }

                    @Override
                    public void onError(String error) {
                        toastOnUi(error);
                    }
                });
            }
        }, true);

        binding.rvAdminInvoices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminInvoices.setAdapter(invoiceAdapter);

        adminViewModel.getAllInvoicesWithRoom().observe(getViewLifecycleOwner(), list -> {
            allInvoices = list != null ? list : new ArrayList<>();
            applyFilter();
        });

        binding.tabInvoices.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabIndex = tab.getPosition();
                applyFilter();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        binding.fabCreateInvoice.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.navigation_admin_invoice_create));
    }

    private void applyFilter() {
        if (binding == null) return;
        List<InvoiceWithContractAndRoom> result = new ArrayList<>();
        for (InvoiceWithContractAndRoom item : allInvoices) {
            if (item.invoice == null) continue;
            boolean ok = tabIndex == 0
                    || (tabIndex == 1 && !item.invoice.isPaid())
                    || (tabIndex == 2 && item.invoice.isPaid());
            if (ok) result.add(item);
        }
        invoiceAdapter.submitList(result);
        binding.tvInvoiceEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void toastOnUi(String message) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}