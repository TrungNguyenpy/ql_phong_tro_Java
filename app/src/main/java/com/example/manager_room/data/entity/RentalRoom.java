package com.example.manager_room.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
@Entity(tableName = "rooms", indices = {@Index(value = {"roomNumber"}, unique = true)})
public class RentalRoom {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String roomNumber; // Ví dụ: "P101"
    private double price;      // Giá thuê VNĐ
    private double area;       // Diện tích m2
    private String status;     // "AVAILABLE" (Trống), "RENTED" (Đã thuê), "MAINTENANCE" (Bảo trì)
    private String imagePath;  // Đường dẫn ảnh hoặc URI

    public RentalRoom(String roomNumber, double price, double area, String status, String imagePath) {
        this.roomNumber = roomNumber;
        this.price = price;
        this.area = area;
        this.status = status;
        this.imagePath = imagePath;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}

