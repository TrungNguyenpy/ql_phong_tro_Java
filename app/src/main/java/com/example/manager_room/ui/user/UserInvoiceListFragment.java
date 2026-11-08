package com.example.manager_room.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.manager_room.data.entity.Invoice;
import com.example.manager_room.databinding.FragmentUserInvoiceListBinding;
import com.example.manager_room.util.PdfExporter;

public class UserInvoiceListFragment extends Fragment {
    private FragmentUserInvoiceListBinding binding;
    private UserViewModel userViewModel;
    private InvoiceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserInvoiceListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        adapter = new InvoiceAdapter(new InvoiceAdapter.OnInvoiceClickListener() {
            @Override
            public void onExportPdf(Invoice invoice) {
                userViewModel.getCurrentTenant().observe(getViewLifecycleOwner(), tenant -> {
                    String tenantName = "Khách thuê";
                    PdfExporter.exportInvoiceToPdf(requireContext(), invoice, "Phòng thuê", tenantName);
                });
            }

            @Override
            public void onMarkPaid(Invoice invoice) {
                // User không có quyền đổi trạng thái thanh toán
            }

            @Override
            public void onDeleteInvoice(Invoice invoice) {
                // User không có quyền xóa hóa đơn
            }
        }, false);

        binding.rvInvoices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvInvoices.setAdapter(adapter);

        userViewModel.getActiveContract().observe(getViewLifecycleOwner(), contract -> {
            if (contract != null) {
                userViewModel.getInvoicesByContractWithRoom(contract.getId()).observe(getViewLifecycleOwner(), invoices -> {
                    if (invoices != null) {
                        adapter.submitList(invoices);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}