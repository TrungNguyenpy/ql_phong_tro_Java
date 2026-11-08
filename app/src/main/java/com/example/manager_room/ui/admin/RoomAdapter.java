package com.example.manager_room.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import com.example.manager_room.R;

import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.databinding.ItemRoomBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class RoomAdapter extends ListAdapter<RentalRoom, RoomAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(RentalRoom room);
    }

    public interface OnRoomLongClickListener {
        void onRoomLongClick(RentalRoom room);
    }

    private final OnRoomClickListener listener;
    private OnRoomLongClickListener longClickListener;

    public void setOnRoomLongClickListener(OnRoomLongClickListener l) {
        this.longClickListener = l;
    }

    public RoomAdapter(OnRoomClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<RentalRoom> DIFF_CALLBACK = new DiffUtil.ItemCallback<RentalRoom>() {
        @Override
        public boolean areItemsTheSame(@NonNull RentalRoom oldItem, @NonNull RentalRoom newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RentalRoom oldItem, @NonNull RentalRoom newItem) {
            return oldItem.getRoomNumber().equals(newItem.getRoomNumber()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    oldItem.getStatus().equals(newItem.getStatus());
        }
    };

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoomBinding binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RoomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        holder.bind(getItem(position), listener, longClickListener);
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoomBinding binding;
        private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        public RoomViewHolder(ItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RentalRoom room, OnRoomClickListener listener, OnRoomLongClickListener longListener) {
            android.content.Context ctx = itemView.getContext();
            boolean available = "AVAILABLE".equals(room.getStatus());
            int color = ContextCompat.getColor(ctx, available ? R.color.status_red : R.color.status_green);
            int bg = ContextCompat.getColor(ctx, available ? R.color.status_red_bg : R.color.status_green_bg);

            binding.tvRoomNumber.setText("Phòng " + room.getRoomNumber());
            binding.tvPrice.setText(String.format(new Locale("vi", "VN"), "%,.0fđ", room.getPrice()));

            binding.imgRoom.setImageTintList(ColorStateList.valueOf(color));
            binding.tvStatus.setText(available ? "TRỐNG" : "ĐANG THUÊ");
            binding.tvStatus.setTextColor(color);
            binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(bg));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(room);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longListener != null) {
                    longListener.onRoomLongClick(room);
                    return true;
                }
                return false;
            });
        }
    }
}