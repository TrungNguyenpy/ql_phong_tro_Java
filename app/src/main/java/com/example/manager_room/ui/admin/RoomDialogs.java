package com.example.manager_room.ui.admin;

import android.content.Context;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.databinding.ItemFormFieldBinding;
import com.example.manager_room.util.DialogUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class RoomDialogs {

    public static boolean isDuplicate(List<RentalRoom> rooms, String number, long excludeId) {
        for (RentalRoom r : rooms) {
            if (r.getId() != excludeId && r.getRoomNumber() != null
                    && r.getRoomNumber().trim().equalsIgnoreCase(number.trim())) {
                return true;
            }
        }
        return false;
    }

    public static void showEdit(Fragment fragment, RentalRoom room, List<RentalRoom> allRooms, AdminViewModel vm) {
        Context ctx = fragment.requireContext();
        String[] labels = {"Số phòng (vd: P101)", "Giá phòng (VNĐ/tháng)", "Diện tích (m²)"};
        int[] types = {
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
                InputType.TYPE_CLASS_NUMBER,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL};
        double area = room.getArea();
        String[] values = {
                room.getRoomNumber(),
                String.valueOf((long) room.getPrice()),
                area == (long) area ? String.valueOf((long) area) : String.valueOf(area)};

        int pad = (int) (20 * ctx.getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, 0, pad, pad);
        ScrollView scroll = new ScrollView(ctx);
        scroll.addView(container);

        List<ItemFormFieldBinding> fields = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            ItemFormFieldBinding f = ItemFormFieldBinding.inflate(fragment.getLayoutInflater(), container, false);
            f.tvFieldLabel.setText(labels[i]);
            f.etFieldValue.setInputType(types[i]);
            f.etFieldValue.setText(values[i]);
            container.addView(f.getRoot());
            fields.add(f);
        }

        DialogUtils.showForm(new MaterialAlertDialogBuilder(ctx)
                .setTitle("Sửa thông tin phòng")
                .setView(scroll)
                .setPositiveButton("Lưu", (d, w) -> save(fragment, room, allRooms, vm, fields))
                .setNegativeButton("Hủy", null));
    }

    private static void save(Fragment fragment, RentalRoom room, List<RentalRoom> allRooms,
                             AdminViewModel vm, List<ItemFormFieldBinding> fields) {
        Context ctx = fragment.requireContext();
        String number = fields.get(0).etFieldValue.getText().toString().trim();
        String priceStr = fields.get(1).etFieldValue.getText().toString().trim();
        String areaStr = fields.get(2).etFieldValue.getText().toString().trim();

        if (number.isEmpty() || priceStr.isEmpty() || areaStr.isEmpty()) {
            Toast.makeText(ctx, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        double price, area;
        try {
            price = Double.parseDouble(priceStr);
            area = Double.parseDouble(areaStr);
        } catch (NumberFormatException e) {
            Toast.makeText(ctx, "Giá hoặc diện tích không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (price <= 0 || area <= 0) {
            Toast.makeText(ctx, "Giá và diện tích phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isDuplicate(allRooms, number, room.getId())) {
            Toast.makeText(ctx, "Số phòng đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        vm.updateRoom(room.getId(), number, price, area, new AdminViewModel.ActionCallback() {
            @Override
            public void onSuccess() {
                toastOnUi(fragment, ctx, "Đã cập nhật thông tin phòng!");
            }

            @Override
            public void onError(String error) {
                toastOnUi(fragment, ctx, error);
            }
        });
    }

    private static void toastOnUi(Fragment fragment, Context ctx, String message) {
        if (fragment.getActivity() == null) return;
        fragment.getActivity().runOnUiThread(() ->
                Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show());
    }
}