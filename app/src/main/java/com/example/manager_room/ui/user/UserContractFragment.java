package com.example.manager_room.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.manager_room.databinding.FragmentUserContractBinding;
import com.example.manager_room.ui.contract.ContractActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserContractFragment extends Fragment {
    private FragmentUserContractBinding binding;
    private UserViewModel userViewModel;
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserContractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getActiveContract().observe(getViewLifecycleOwner(), contract -> {
            if (contract != null) {
                binding.tvStartDate.setText("Ngày bắt đầu: " + contract.getStartDate());
                binding.tvEndDate.setText("Ngày kết thúc: " + contract.getEndDate());
                binding.tvPrice.setText("Giá thuê: " + formatter.format(contract.getRentalPrice()));
                binding.tvDeposit.setText("Tiền cọc: " + formatter.format(contract.getDeposit()));
                binding.tvTerms.setText("Điều khoản:\n" + contract.getTerms());

                // Tính số ngày còn lại của hợp đồng
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date endDate = sdf.parse(contract.getEndDate());
                    Date today = new Date();
                    if (endDate != null) {
                        long diffInMillies = endDate.getTime() - today.getTime();
                        long days = diffInMillies / (1000 * 60 * 60 * 24);
                        binding.tvDaysRemaining.setText("Số ngày còn lại: " + Math.max(0, days) + " ngày");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Chuyển sang màn hình xem toàn bộ hợp đồng
                binding.btnViewFullContract.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), ContractActivity.class);
                    intent.putExtra(ContractActivity.EXTRA_ROOM_ID, contract.getRoomId());
                    startActivity(intent);
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