package com.example.manager_room.ui.contract;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.manager_room.R;
import com.example.manager_room.data.database.AppDatabase;
import com.example.manager_room.data.entity.Contract;
import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.data.entity.TenantWithUser;
import com.example.manager_room.databinding.ActivityContractBinding;
import com.example.manager_room.databinding.ItemFormFieldBinding;
import com.example.manager_room.util.ContractTextBuilder;
import com.example.manager_room.util.DialogUtils;
import com.example.manager_room.util.LandlordInfo;
import com.example.manager_room.util.PdfExporter;
import com.example.manager_room.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContractActivity extends AppCompatActivity {
    public static final String EXTRA_ROOM_ID = "roomId";

    private ActivityContractBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ContractTextBuilder.Data data;
    private LandlordInfo landlord;
    private long roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContractBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomId = getIntent().getLongExtra(EXTRA_ROOM_ID, -1);
        landlord = new LandlordInfo(this);
        boolean isAdmin = "ADMIN".equals(new SessionManager(this).getRole());

        binding.toolbarContract.setNavigationOnClickListener(v -> finish());
        binding.toolbarContract.inflateMenu(R.menu.contract_menu);
        binding.toolbarContract.getMenu().findItem(R.id.action_edit_contract).setVisible(isAdmin);
        binding.toolbarContract.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_contract) {
                showEditContractDialog();
                return true;
            }
            return false;
        });

        binding.btnEditLandlord.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        binding.btnEditLandlord.setOnClickListener(v -> showLandlordDialog());
        binding.btnExportContract.setOnClickListener(v -> exportPdf());

        loadContract();
    }

    private void loadContract() {
        AppDatabase db = AppDatabase.getInstance(this);
        executor.execute(() -> {
            Contract contract = db.contractDao().getActiveContractByRoomIdSync(roomId);
            RentalRoom room = db.roomDao().getRoomByIdSync(roomId);
            TenantWithUser tw = contract != null
                    ? db.tenantDao().getTenantWithUserByIdSync(contract.getTenantId()) : null;

            runOnUiThread(() -> {
                if (contract == null || room == null || tw == null) {
                    binding.tvContractBody.setText("Không tìm thấy hợp đồng đang hiệu lực của phòng này.");
                    return;
                }
                data = new ContractTextBuilder.Data();
                data.contract = contract;
                data.room = room;
                data.tenantUser = tw.user;
                data.tenant = tw.tenant;
                data.landlord = landlord;
                render();
            });
        });
    }

    private void render() {
        binding.tvContractBody.setText(ContractTextBuilder.build(data));
    }

    private void exportPdf() {
        if (data == null) {
            Toast.makeText(this, "Chưa có dữ liệu hợp đồng!", Toast.LENGTH_SHORT).show();
            return;
        }
        PdfExporter.exportContractToPdf(this, ContractTextBuilder.build(data),
                "HopDong_Phong_" + data.room.getRoomNumber() + ".pdf");
    }

    private void showLandlordDialog() {
        String[] keys = {LandlordInfo.NAME, LandlordInfo.CCCD, LandlordInfo.PHONE, LandlordInfo.ADDRESS,
                LandlordInfo.PROPERTY, LandlordInfo.PAY_DAY, LandlordInfo.ELEC, LandlordInfo.WATER};
        String[] labels = {"Họ tên chủ trọ (Bên A)", "Số CCCD của chủ trọ", "Số điện thoại",
                "Địa chỉ thường trú / liên hệ", "Địa chỉ nhà trọ", "Ngày thanh toán hằng tháng (1-31)",
                "Đơn giá điện (đồng/kWh)", "Đơn giá nước (đồng/m³)"};
        int[] types = {InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE,
                InputType.TYPE_CLASS_TEXT, InputType.TYPE_CLASS_TEXT,
                InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_NUMBER};

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, 0, pad, pad);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(container);

        List<ItemFormFieldBinding> fields = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            ItemFormFieldBinding f = ItemFormFieldBinding.inflate(getLayoutInflater(), container, false);
            f.tvFieldLabel.setText(labels[i]);
            f.etFieldValue.setInputType(types[i]);
            f.etFieldValue.setText(landlord.get(keys[i]));
            container.addView(f.getRoot());
            fields.add(f);
        }

        DialogUtils.showForm(new MaterialAlertDialogBuilder(this)
                .setTitle("Thông tin chủ trọ & khu trọ")
                .setView(scroll)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    for (int i = 0; i < keys.length; i++) {
                        landlord.put(keys[i], fields.get(i).etFieldValue.getText().toString().trim());
                    }
                    if (data != null) render();
                })
                .setNegativeButton("Hủy", null));
    }

    private void showEditContractDialog() {
        if (data == null) {
            Toast.makeText(this, "Chưa có dữ liệu hợp đồng!", Toast.LENGTH_SHORT).show();
            return;
        }
        Contract c = data.contract;
        String[] labels = {"Giá thuê (VNĐ/tháng)", "Tiền cọc (VNĐ)",
                "Ngày kết thúc (yyyy-MM-dd)", "Thỏa thuận khác (Điều 9)"};
        int[] types = {InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_NUMBER,
                InputType.TYPE_CLASS_TEXT,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE};
        String[] values = {String.valueOf((long) c.getRentalPrice()),
                String.valueOf((long) c.getDeposit()), c.getEndDate(),
                c.getTerms() == null ? "" : c.getTerms()};

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, 0, pad, pad);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(container);

        List<ItemFormFieldBinding> fields = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            ItemFormFieldBinding f = ItemFormFieldBinding.inflate(getLayoutInflater(), container, false);
            f.tvFieldLabel.setText(labels[i]);
            f.etFieldValue.setInputType(types[i]);
            f.etFieldValue.setText(values[i]);
            container.addView(f.getRoot());
            fields.add(f);
        }

        DialogUtils.showForm(new MaterialAlertDialogBuilder(this)
                .setTitle("Sửa hợp đồng")
                .setView(scroll)
                .setPositiveButton("Lưu", (d, w) -> saveContract(fields))
                .setNegativeButton("Hủy", null));
    }

    private void saveContract(List<ItemFormFieldBinding> fields) {
        String priceStr = fields.get(0).etFieldValue.getText().toString().trim();
        String depositStr = fields.get(1).etFieldValue.getText().toString().trim();
        String endDate = fields.get(2).etFieldValue.getText().toString().trim();
        String terms = fields.get(3).etFieldValue.getText().toString().trim();

        double price, deposit;
        try {
            price = Double.parseDouble(priceStr);
            deposit = Double.parseDouble(depositStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá thuê hoặc tiền cọc không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (price <= 0 || deposit < 0) {
            Toast.makeText(this, "Giá thuê phải lớn hơn 0 và tiền cọc không âm!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            Date end = sdf.parse(endDate);
            Date start = sdf.parse(data.contract.getStartDate());
            if (end == null || start == null || !end.after(start)) {
                Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ngày kết thúc không hợp lệ (định dạng yyyy-MM-dd)!", Toast.LENGTH_SHORT).show();
            return;
        }

        Contract c = data.contract;
        c.setRentalPrice(price);
        c.setDeposit(deposit);
        c.setEndDate(endDate);
        c.setTerms(terms);

        AppDatabase db = AppDatabase.getInstance(this);
        executor.execute(() -> {
            try {
                db.contractDao().update(c);
                runOnUiThread(() -> {
                    render();
                    Toast.makeText(this, "Đã cập nhật hợp đồng!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi cập nhật hợp đồng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}