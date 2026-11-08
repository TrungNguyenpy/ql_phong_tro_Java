package com.example.manager_room.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.databinding.FragmentAdminInvoiceCreateBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminInvoiceCreateFragment extends Fragment {
    private FragmentAdminInvoiceCreateBinding binding;
    private AdminViewModel adminViewModel;
    private List<RentalRoom> roomList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminInvoiceCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Chỉ liệt kê phòng đang thuê (phòng trống chưa có hợp đồng nên không lập được hóa đơn)
        adminViewModel.getAllRooms().observe(getViewLifecycleOwner(), rooms -> {
            if (rooms == null) return;
            roomList = new ArrayList<>();
            List<String> roomNames = new ArrayList<>();
            for (RentalRoom r : rooms) {
                if ("RENTED".equals(r.getStatus())) {
                    roomList.add(r);
                    roomNames.add("Phòng " + r.getRoomNumber());
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roomNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerRooms.setAdapter(adapter);
        });

        binding.btnGenerateInvoice.setOnClickListener(v -> {
            int selectedPos = binding.spinnerRooms.getSelectedItemPosition();
            if (selectedPos < 0 || roomList.isEmpty()) {
                Toast.makeText(requireContext(), "Chưa có phòng đang thuê để lập hóa đơn!", Toast.LENGTH_SHORT).show();
                return;
            }

            String elecRateStr = binding.etElectricRate.getText().toString().trim();
            String waterRateStr = binding.etWaterRate.getText().toString().trim();
            String serviceFeeStr = binding.etServiceFee.getText().toString().trim();
            if (elecRateStr.isEmpty() || waterRateStr.isEmpty() || serviceFeeStr.isEmpty()) {
                Toast.makeText(requireContext(), "Nhập đầy đủ đơn giá!", Toast.LENGTH_SHORT).show();
                return;
            }

            RentalRoom selectedRoom = roomList.get(selectedPos);
            adminViewModel.createOrUpdateInvoice(selectedRoom.getId(),
                    Double.parseDouble(elecRateStr),
                    Double.parseDouble(waterRateStr),
                    Double.parseDouble(serviceFeeStr),
                    new AdminViewModel.SaveCallback() {
                        @Override
                        public void onSuccess(String message) {
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                NavHostFragment.findNavController(AdminInvoiceCreateFragment.this).popBackStack();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
                        }
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}