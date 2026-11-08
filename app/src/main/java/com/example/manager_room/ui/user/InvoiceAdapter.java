package com.example.manager_room.ui.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import com.example.manager_room.R;

import com.example.manager_room.data.entity.Invoice;
import com.example.manager_room.data.entity.InvoiceWithContractAndRoom;
import com.example.manager_room.databinding.DialogInvoiceDetailBinding;
import com.example.manager_room.databinding.ItemInvoiceBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.Locale;

public class InvoiceAdapter extends ListAdapter<InvoiceWithContractAndRoom, InvoiceAdapter.InvoiceViewHolder> {

    public interface OnInvoiceClickListener {
        void onExportPdf(Invoice invoice);
        void onMarkPaid(Invoice invoice);
        void onDeleteInvoice(Invoice invoice); // Callback xóa hóa đơn
    }

    private final OnInvoiceClickListener listener;
    private final boolean isAdmin;

    public InvoiceAdapter(OnInvoiceClickListener listener, boolean isAdmin) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    private static final DiffUtil.ItemCallback<InvoiceWithContractAndRoom> DIFF_CALLBACK = new DiffUtil.ItemCallback<InvoiceWithContractAndRoom>() {
        @Override
        public boolean areItemsTheSame(@NonNull InvoiceWithContractAndRoom oldItem, @NonNull InvoiceWithContractAndRoom newItem) {
            return oldItem.invoice != null && newItem.invoice != null && oldItem.invoice.getId() == newItem.invoice.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull InvoiceWithContractAndRoom oldItem, @NonNull InvoiceWithContractAndRoom newItem) {
            if (oldItem.invoice == null || newItem.invoice == null) return false;
            return oldItem.invoice.isPaid() == newItem.invoice.isPaid() &&
                    oldItem.invoice.getTotalAmount() == newItem.invoice.getTotalAmount();
        }
    };

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInvoiceBinding binding = ItemInvoiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new InvoiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        holder.bind(getItem(position), listener, isAdmin);
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemInvoiceBinding binding;
        private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        public InvoiceViewHolder(ItemInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(InvoiceWithContractAndRoom item, OnInvoiceClickListener listener, boolean isAdmin) {
            if (item == null || item.invoice == null) return;
            Invoice invoice = item.invoice;

            binding.tvTitle.setText("Hóa đơn Tháng " + invoice.getMonth() + "/" + invoice.getYear());
            binding.tvTotal.setText(String.format(new Locale("vi", "VN"), "%,.0fđ", invoice.getTotalAmount()));
            binding.tvCreatedDate.setText("Ngày lập: " + (invoice.getCreatedDate() != null ? invoice.getCreatedDate() : "Chưa cập nhật"));

            // Trích xuất tên phòng
            String roomName = "Phòng #" + invoice.getId();
            if (item.contractWithRoom != null && item.contractWithRoom.room != null) {
                roomName = "Phòng " + item.contractWithRoom.room.getRoomNumber();
            }
            binding.tvRoomName.setText(roomName);

            boolean paid = invoice.isPaid();
            Context ctx = binding.getRoot().getContext();
            int color = ContextCompat.getColor(ctx, paid ? R.color.status_green : R.color.status_red);
            int bg = ContextCompat.getColor(ctx, paid ? R.color.status_green_bg : R.color.status_red_bg);
            binding.tvStatus.setText(paid ? "ĐÃ THANH TOÁN"
                    : (isAdmin ? "CHƯA THANH TOÁN (BẤM ĐỔI)" : "CHƯA THANH TOÁN"));
            binding.tvStatus.setTextColor(color);
            binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(bg));

            // Bật Dialog xem chi tiết hóa đơn
            String finalRoomName = roomName;
            binding.btnViewDetail.setOnClickListener(v -> showInvoiceDetailDialog(binding.getRoot().getContext(), item, finalRoomName));

            binding.btnExportPdf.setOnClickListener(v -> {
                if (listener != null) listener.onExportPdf(invoice);
            });

            if (isAdmin && !invoice.isPaid()) {
                binding.tvStatus.setOnClickListener(v -> {
                    if (listener != null) listener.onMarkPaid(invoice);
                });
            } else {
                binding.tvStatus.setOnClickListener(null);
            }

            // Nhấn giữ để Xóa Hóa Đơn (Dành riêng cho Admin)
            binding.getRoot().setOnLongClickListener(v -> {
                if (isAdmin && listener != null) {
                    new MaterialAlertDialogBuilder(binding.getRoot().getContext())
                            .setTitle("Xóa Hóa Đơn")
                            .setMessage("Bạn có chắc chắn muốn xóa " + binding.tvTitle.getText() + " của " + finalRoomName + " không?")
                            .setPositiveButton("Xóa", (dialog, which) -> listener.onDeleteInvoice(invoice))
                            .setNegativeButton("Hủy", null)
                            .show();
                    return true;
                }
                return false;
            });
        }

        private void showInvoiceDetailDialog(Context context, InvoiceWithContractAndRoom item, String roomName) {
            DialogInvoiceDetailBinding detailBinding = DialogInvoiceDetailBinding.inflate(LayoutInflater.from(context));
            Invoice invoice = item.invoice;

            // Header Info
            detailBinding.tvDetailTitle.setText("CHI TIẾT HÓA ĐƠN THÁNG " + invoice.getMonth() + "/" + invoice.getYear());
            detailBinding.tvDetailRoom.setText("Số phòng: " + roomName);

            String tenantName = "Khách thuê";
            detailBinding.tvDetailTenant.setText("Người ở: " + tenantName);
            detailBinding.tvDetailCreatedDate.setText("Ngày nộp / Ngày lập: " + (invoice.getCreatedDate() != null ? invoice.getCreatedDate() : "N/A"));

            // Bind dữ liệu chính xác từ Entity Invoice
            detailBinding.tvDetailRoomPrice.setText(formatter.format(invoice.getRoomAmount()));

            // Chi tiết Điện
            detailBinding.tvDetailElectricTotal.setText(formatter.format(invoice.getElectricityAmount()));
            detailBinding.tvDetailElectricUsage.setText("Thành tiền điện tiêu thụ trong tháng");

            // Chi tiết Nước
            detailBinding.tvDetailWaterTotal.setText(formatter.format(invoice.getWaterAmount()));
            detailBinding.tvDetailWaterUsage.setText("Thành tiền nước tiêu thụ trong tháng");

            // Phí dịch vụ khác & Tổng tiền
            detailBinding.tvDetailServiceFee.setText(formatter.format(invoice.getServiceAmount()));
            detailBinding.tvDetailTotal.setText(formatter.format(invoice.getTotalAmount()));

            // Hiển thị Material Dialog
            new MaterialAlertDialogBuilder(context)
                    .setView(detailBinding.getRoot())
                    .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}