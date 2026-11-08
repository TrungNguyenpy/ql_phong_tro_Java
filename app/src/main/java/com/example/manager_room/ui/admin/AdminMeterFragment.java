package com.example.manager_room.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.databinding.FragmentAdminMeterBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminMeterFragment extends Fragment {
    private FragmentAdminMeterBinding binding;
    private AdminViewModel adminViewModel;
    private List<RentalRoom> roomList = new ArrayList<>();
    private long selectedRoomId = -1; // Biến lưu ID phòng trọ đang được chọn

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminMeterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Nạp danh sách phòng vào Spinner
        adminViewModel.getAllRooms().observe(getViewLifecycleOwner(), rooms -> {
            if (rooms != null && !rooms.isEmpty()) {
                this.roomList = rooms;
                List<String> roomNames = new ArrayList<>();
                for (RentalRoom r : rooms) {
                    roomNames.add("Phòng " + r.getRoomNumber());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roomNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerRooms.setAdapter(adapter);

                // Mặc định chọn phòng đầu tiên trong danh sách
                selectedRoomId = roomList.get(0).getId();
            }
        });

        // Lắng nghe sự kiện người dùng thay đổi lựa chọn phòng trên Spinner
        binding.spinnerRooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (roomList != null && position < roomList.size()) {
                    selectedRoomId = roomList.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRoomId = -1;
            }
        });

        // Sự kiện Lưu/Cập nhật chỉ số
        binding.btnSaveMeter.setOnClickListener(v -> {
            if (selectedRoomId == -1) {
                Toast.makeText(requireContext(), "Vui lòng chọn phòng trọ trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            String elecStr = binding.etNewElectricity.getText().toString().trim();
            String waterStr = binding.etNewWater.getText().toString().trim();

            if (elecStr.isEmpty() || waterStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ chỉ số!", Toast.LENGTH_SHORT).show();
                return;
            }

            int newElec = Integer.parseInt(elecStr);
            int newWater = Integer.parseInt(waterStr);

            // Lấy tháng và năm hiện tại
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentYear = calendar.get(Calendar.YEAR);

            // Gọi hàm UPSERT tự động chèn/cập nhật trong AdminViewModel
            adminViewModel.saveOrUpdateMeterReading(selectedRoomId, currentMonth, currentYear, newElec, newWater, new AdminViewModel.SaveCallback() {
                @Override
                public void onSuccess(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            binding.etNewElectricity.setText("");
                            binding.etNewWater.setText("");
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        });
                    }
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