package com.example.manager_room.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.manager_room.databinding.FragmentUserHomeBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class UserHomeFragment extends Fragment {
    private FragmentUserHomeBinding binding;
    private UserViewModel userViewModel;
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getActiveContract().observe(getViewLifecycleOwner(), contract -> {
            if (contract != null) {
                // Lấy thông tin phòng
                userViewModel.getRoomById(contract.getRoomId()).observe(getViewLifecycleOwner(), room -> {
                    if (room != null) {
                        binding.tvRoomName.setText("Phòng: " + room.getRoomNumber());
                        binding.tvRoomPrice.setText("Giá thuê: " + formatter.format(room.getPrice()));
                    }
                });

                // Lấy thông tin hóa đơn tháng mới nhất
                userViewModel.getLatestInvoice(contract.getId()).observe(getViewLifecycleOwner(), invoice -> {
                    if (invoice != null) {
                        binding.tvInvoiceMonth.setText("Tháng: " + invoice.getMonth() + "/" + invoice.getYear());
                        binding.tvInvoiceTotal.setText("Tổng tiền: " + formatter.format(invoice.getTotalAmount()));
                        if (invoice.isPaid()) {
                            binding.tvInvoiceStatus.setText("Trạng thái: Đã thanh toán");
                            binding.tvInvoiceStatus.setTextColor(0xFF388E3C); // Màu xanh lá
                        } else {
                            binding.tvInvoiceStatus.setText("Trạng thái: Chưa thanh toán");
                            binding.tvInvoiceStatus.setTextColor(0xFFD32F2F); // Màu đỏ
                        }
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