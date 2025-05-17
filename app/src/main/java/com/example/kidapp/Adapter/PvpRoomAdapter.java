package com.example.kidapp.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.R;
import com.example.kidapp.models.PvpRoom;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.function.Consumer;

public class PvpRoomAdapter extends RecyclerView.Adapter<PvpRoomAdapter.RoomViewHolder> {
    private static final String TAG = "PvpRoomAdapter";
    private List<PvpRoom> rooms;
    private final Consumer<PvpRoom> onRoomClickListener;

    public PvpRoomAdapter(List<PvpRoom> rooms, Consumer<PvpRoom> onRoomClickListener) {
        this.rooms = rooms;
        this.onRoomClickListener = onRoomClickListener;
        Log.d(TAG, "Adapter created with " + (rooms != null ? rooms.size() : 0) + " rooms");
    }

    public void updateRooms(List<PvpRoom> newRooms) {
        Log.d(TAG, "Updating rooms list. New size: " + (newRooms != null ? newRooms.size() : 0));
        if (newRooms != null) {
            for (PvpRoom room : newRooms) {
                Log.d(TAG, "Room in list: " + room.getRoomName() + " (ID: " + room.getRoomId() + 
                      "), Host: " + room.getHostName() + ", Status: " + room.getStatus());
            }
        }
        this.rooms = newRooms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pvp_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        PvpRoom room = rooms.get(position);
        Log.d(TAG, "Binding room at position " + position + ": " + room.getRoomName() + 
              " (ID: " + room.getRoomId() + "), Host: " + room.getHostName());
        holder.bind(room, onRoomClickListener);
    }

    @Override
    public int getItemCount() {
        return rooms != null ? rooms.size() : 0;
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivGameIcon;
        private final TextView tvRoomName;
        private final TextView tvHostName;
        private final TextView tvGameType;
        private final MaterialButton btnJoin;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGameIcon = itemView.findViewById(R.id.ivGameIcon);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvHostName = itemView.findViewById(R.id.tvHostName);
            tvGameType = itemView.findViewById(R.id.tvGameType);
            btnJoin = itemView.findViewById(R.id.btnJoin);
        }

        void bind(PvpRoom room, Consumer<PvpRoom> onRoomClickListener) {
            Log.d(TAG, "Setting room data: " + room.getRoomName() + ", Host: " + room.getHostName());
            
            // Đảm bảo thông tin phòng không null
            String roomName = room.getRoomName() != null ? room.getRoomName() : "Phòng không tên";
            String hostName = room.getHostName() != null ? room.getHostName() : "Không xác định";
            
            tvRoomName.setText(roomName);
            tvHostName.setText("Chủ phòng: " + hostName);

            // Thiết lập icon và text loại game
            int iconResId;
            String gameTypeText;
            switch (room.getGameType()) {
                case "FLIP_CARD":
                    iconResId = R.drawable.baseline_flip_24;
                    gameTypeText = "Trò chơi: Lật thẻ nhớ hình";
                    break;
                case "PUZZLE":
                    iconResId = R.drawable.baseline_extension_24;
                    gameTypeText = "Trò chơi: Xếp hình";
                    break;
                case "GUESS_WORD":
                    iconResId = R.drawable.baseline_spellcheck_24;
                    gameTypeText = "Trò chơi: Đoán chữ";
                    break;
                case "MATH":
                    iconResId = R.drawable.baseline_calculate_24;
                    gameTypeText = "Trò chơi: Làm toán";
                    break;
                default:
                    iconResId = R.drawable.ic_star;
                    gameTypeText = "Trò chơi: Khác";
                    break;
            }

            ivGameIcon.setImageResource(iconResId);
            tvGameType.setText(gameTypeText);

            // Thiết lập sự kiện click nút tham gia
            btnJoin.setOnClickListener(v -> {
                Log.d(TAG, "Join button clicked for room: " + room.getRoomId());
                onRoomClickListener.accept(room);
            });
            
            // Thiết lập sự kiện click vào item
            itemView.setOnClickListener(v -> {
                Log.d(TAG, "Room item clicked: " + room.getRoomId());
                onRoomClickListener.accept(room);
            });
        }
    }
} 