package com.example.manager_room.ui.admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.example.manager_room.R;
import com.example.manager_room.data.entity.Contract;
import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.data.entity.TenantWithUser;
import com.example.manager_room.databinding.FragmentAdminRoomDetailBinding;
import com.example.manager_room.ui.contract.ContractActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdminRoomDetailFragment extends Fragment {

    private FragmentAdminRoomDetailBinding binding;
    private AdminViewModel adminViewModel;
    private long roomId = -1;
    private Contract currentContract;
    private List<RentalRoom> roomsCache = new ArrayList<>();
    private RentalRoom currentRoom;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomId = getArguments().getLong("roomId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminRoomDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Scope ViewModel theo Activity để dùng chung dữ liệu với các màn hình khác
        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adminViewModel.getAllRooms().observe(getViewLifecycleOwner(), rooms -> {
            if (rooms == null) return;
            roomsCache = rooms;
            for (RentalRoom r : rooms) {
                if (r.getId() == roomId) {
                    currentRoom = r;
                    bindRoom(r);
                    break;
                }
            }
        });

        setupMenu();
        loadTenant();
        setupClickListeners();
    }

    private void setupMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.room_detail_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_edit_room) {
                    showEditRoomDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void setupClickListeners() {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigationAdmin);

        binding.rowContract.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ContractActivity.class);
            intent.putExtra(ContractActivity.EXTRA_ROOM_ID, roomId);
            startActivity(intent);
        });

        binding.rowInvoices.setOnClickListener(v -> {
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_admin_invoices);
            }
        });

        binding.rowMeter.setOnClickListener(v -> {
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_admin_meter);
            }
        });
    }

    private void bindRoom(RentalRoom room) {
        if (binding == null || !isAdded()) return;

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Phòng " + room.getRoomNumber());
        }

        String status = room.getStatus();
        boolean rented = "RENTED".equals(status);
        String label = rented ? "Đang thuê" : ("AVAILABLE".equals(status) ? "Còn trống" : "Bảo trì");

        int color = ContextCompat.getColor(requireContext(), rented ? R.color.status_green : R.color.status_red);
        int bg = ContextCompat.getColor(requireContext(), rented ? R.color.status_green_bg : R.color.status_red_bg);

        binding.tvDetailStatus.setText("Trạng thái: " + label);
        binding.tvDetailStatus.setTextColor(color);
        binding.cardStatus.setCardBackgroundColor(bg);
        binding.imgStatusIcon.setBackgroundTintList(ColorStateList.valueOf(color));
        binding.imgStatusIcon.setImageResource(rented ? R.drawable.ic_check : R.drawable.ic_door_open);
    }

    private void loadTenant() {
        adminViewModel.getTenantDetailsByRoom(roomId, new AdminViewModel.TenantDetailsCallback() {
            @Override
            public void onLoaded(TenantWithUser tenantWithUser, Contract contract) {
                if (!isAdded() || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    currentContract = contract;

                    String name = (tenantWithUser != null && tenantWithUser.user != null) ? tenantWithUser.user.getFullName() : null;
                    String phone = (tenantWithUser != null && tenantWithUser.user != null) ? tenantWithUser.user.getPhone() : null;

                    binding.tvTenantName.setText(name != null ? name : "Chưa cập nhật");
                    binding.tvTenantPhone.setText("SĐT: " + (phone != null ? phone : "Chưa cập nhật"));
                    binding.tvAvatar.setText(name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?");

                    if (contract != null) {
                        binding.tvDetailSince.setText("Kể từ ngày " + formatDate(contract.getStartDate()));
                        binding.tvContractSub.setText(remainingText(contract.getEndDate()));
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded() || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.tvTenantName.setText("Chưa có người thuê");
                    binding.tvTenantPhone.setText("");
                    binding.tvDetailSince.setText(error);
                });
            }
        });
    }

    private String formatDate(String iso) {
        if (iso == null) return "";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso);
            return d != null ? new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(d) : iso;
        } catch (Exception e) {
            return iso;
        }
    }

    private String remainingText(String endIso) {
        if (endIso == null) return "Chi tiết điều khoản và thời hạn";
        try {
            Date end = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(endIso);
            if (end == null) return "Chi tiết điều khoản và thời hạn";

            long days = (end.getTime() - System.currentTimeMillis()) / TimeUnit.DAYS.toMillis(1);
            if (days < 0) return "Đã hết hạn";
            if (days >= 30) return "Còn hạn " + (days / 30) + " tháng";

            return "Còn " + days + " ngày";
        } catch (Exception e) {
            return "Chi tiết điều khoản và thời hạn";
        }
    }

    private void showEditRoomDialog() {
        if (currentRoom == null) return;
        RoomDialogs.showEdit(this, currentRoom, roomsCache, adminViewModel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh leak memory khi Fragment bị destroy view
    }
}