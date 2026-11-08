package com.example.manager_room.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.manager_room.R;
import com.example.manager_room.data.entity.Contract;
import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.data.entity.TenantWithUser;
import com.example.manager_room.databinding.FragmentAdminRoomListBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRoomListFragment extends Fragment {
    private FragmentAdminRoomListBinding binding;
    private AdminViewModel adminViewModel;
    private RoomAdapter adapter;
    private List<TenantWithUser> tenantWithUserList = new ArrayList<>();
    private List<RentalRoom> allRooms = new ArrayList<>();
    private String query = "";
    private int tabIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminRoomListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Khai báo Adapter phân luồng click chọn phòng
        adapter = new RoomAdapter(room -> {
            if ("AVAILABLE".equals(room.getStatus())) {
                // Nếu phòng trống -> Tạo Hợp Đồng
                showAssignTenantDialog(room);
            } else if ("RENTED".equals(room.getStatus())) {
                Bundle args = new Bundle();
                args.putLong("roomId", room.getId());
                NavHostFragment.findNavController(this)
                        .navigate(R.id.navigation_admin_room_detail, args);
            }
        });

        // Thêm sự kiện nhấn giữ để chỉnh sửa thông tin phòng
        adapter.setOnRoomLongClickListener(room ->
                RoomDialogs.showEdit(this, room, allRooms, adminViewModel));

        binding.rvAdminRooms.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvAdminRooms.setAdapter(adapter);

        adminViewModel.getAllRooms().observe(getViewLifecycleOwner(), rooms -> {
            if (rooms != null) {
                allRooms = rooms;
                applyFilter();
            }
        });

        // Lấy danh sách khách thuê kèm tài khoản
        adminViewModel.getAllTenantsWithUsers().observe(getViewLifecycleOwner(), tenants -> {
            if (tenants != null) {
                this.tenantWithUserList = tenants;
            }
        });

        binding.tabRooms.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabIndex = tab.getPosition();
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase();
                applyFilter();
            }
        });

        binding.fabAddRoom.setOnClickListener(v -> showAddRoomDialog());
    }

    // Hiển thị thông tin khách đang thuê phòng
    private void applyFilter() {
        if (binding == null) return;
        List<RentalRoom> result = new ArrayList<>();
        for (RentalRoom r : allRooms) {
            boolean okTab = tabIndex == 0
                    || (tabIndex == 1 && "RENTED".equals(r.getStatus()))
                    || (tabIndex == 2 && "AVAILABLE".equals(r.getStatus()));
            boolean okQuery = query.isEmpty() || r.getRoomNumber().toLowerCase().contains(query);
            if (okTab && okQuery) result.add(r);
        }
        adapter.submitList(result);
    }

    private void showTenantDetailsDialog(RentalRoom room) {
        adminViewModel.getTenantDetailsByRoom(room.getId(), new AdminViewModel.TenantDetailsCallback() {
            @Override
            public void onLoaded(TenantWithUser tenantWithUser, Contract contract) {
                requireActivity().runOnUiThread(() -> {
                    String fullName = (tenantWithUser != null && tenantWithUser.user != null) ? tenantWithUser.user.getFullName() : "Chưa cập nhật";
                    String phone = (tenantWithUser != null && tenantWithUser.user != null) ? tenantWithUser.user.getPhone() : "Chưa cập nhật";
                    String cic = (tenantWithUser != null && tenantWithUser.tenant != null) ? tenantWithUser.tenant.getIdentityCard() : "Chưa cập nhật";
                    String message = "Khách thuê: " + fullName + "\n"
                            + "SĐT: " + phone + "\n"
                            + "CCCD: " + cic + "\n"
                            + "Ngày thuê: " + contract.getStartDate() + "\n"
                            + "Hạn hợp đồng: " + contract.getEndDate() + "\n"
                            + "Tiền cọc: " + String.format(new Locale("vi", "VN"), "%,.0f đ", contract.getDeposit());

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Thông Tin Thuê - Phòng " + room.getRoomNumber())
                            .setMessage(message)
                            .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Dialog lập hợp đồng cho phòng trống
    private void showAssignTenantDialog(RentalRoom room) {
        if (tenantWithUserList.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có dữ liệu khách thuê!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tạo Hợp Đồng Cho " + room.getRoomNumber());

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final Spinner spinnerTenant = new Spinner(requireContext());
        List<String> tenantDisplayNames = new ArrayList<>();

        for (TenantWithUser item : tenantWithUserList) {
            String name = (item.user != null) ? item.user.getFullName() : "Khách chưa đặt tên";
            String phone = (item.user != null && item.user.getPhone() != null) ? item.user.getPhone() : "";
            tenantDisplayNames.add(name + " (" + phone + ")");
        }

        ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tenantDisplayNames);
        tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTenant.setAdapter(tenantAdapter);
        layout.addView(spinnerTenant);

        final EditText etStartDate = new EditText(requireContext());
        etStartDate.setHint("Ngày bắt đầu (yyyy-MM-dd)");
        etStartDate.setText("2026-11-01");
        layout.addView(etStartDate);

        final EditText etEndDate = new EditText(requireContext());
        etEndDate.setHint("Ngày kết thúc (yyyy-MM-dd)");
        etEndDate.setText("2027-11-01");
        layout.addView(etEndDate);

        final EditText etDeposit = new EditText(requireContext());
        etDeposit.setHint("Tiền cọc (VNĐ)");
        etDeposit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etDeposit.setText(String.valueOf((long) room.getPrice()));
        layout.addView(etDeposit);

        builder.setView(layout);

        builder.setPositiveButton("Tạo hợp đồng", (dialog, which) -> {
            int selectedTenantPos = spinnerTenant.getSelectedItemPosition();
            if (selectedTenantPos >= 0) {
                TenantWithUser selectedTenant = tenantWithUserList.get(selectedTenantPos);
                String startDate = etStartDate.getText().toString().trim();
                String endDate = etEndDate.getText().toString().trim();
                String depositStr = etDeposit.getText().toString().trim();

                double deposit = depositStr.isEmpty() ? room.getPrice() : Double.parseDouble(depositStr);

                adminViewModel.createContract(room.getId(), selectedTenant.tenant.getId(), startDate, endDate, room.getPrice(), deposit, "Thanh toán đầu tháng", new AdminViewModel.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Lập hợp đồng thành công cho " + room.getRoomNumber() + "!", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Dialog thêm phòng trọ mới
    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Phòng Mới");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etRoomNumber = new EditText(requireContext());
        etRoomNumber.setHint("Số phòng (vd: P105)");
        layout.addView(etRoomNumber);

        final EditText etPrice = new EditText(requireContext());
        etPrice.setHint("Giá phòng (VNĐ)");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPrice);

        final EditText etArea = new EditText(requireContext());
        etArea.setHint("Diện tích (m²)");
        etArea.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etArea);

        builder.setView(layout);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String roomNum = etRoomNumber.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String areaStr = etArea.getText().toString().trim();

            if (roomNum.isEmpty() || priceStr.isEmpty() || areaStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra xem phòng đã tồn tại chưa
            if (RoomDialogs.isDuplicate(allRooms, roomNum, -1)) {
                Toast.makeText(requireContext(), "Số phòng đã tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            double area = Double.parseDouble(areaStr);

            adminViewModel.addRoom(roomNum, price, area, new AdminViewModel.ActionCallback() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Thêm phòng thành công!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}