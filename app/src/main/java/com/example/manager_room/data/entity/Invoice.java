package com.example.manager_room.data.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "invoices",
        foreignKeys = {
                @ForeignKey(entity = Contract.class, parentColumns = "id", childColumns = "contractId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = MeterReading.class, parentColumns = "id", childColumns = "meterReadingId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("contractId"), @Index("meterReadingId")}
)
public class Invoice {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long contractId;
    private long meterReadingId;
    private int month;
    private int year;
    private double roomAmount;
    private double electricityAmount;
    private double waterAmount;
    private double serviceAmount;
    private double totalAmount;
    private boolean isPaid; // true: Đã thanh toán, false: Chưa thanh toán
    private String createdDate;

    public Invoice(long contractId, long meterReadingId, int month, int year, double roomAmount, double electricityAmount, double waterAmount, double serviceAmount, double totalAmount, boolean isPaid, String createdDate) {
        this.contractId = contractId;
        this.meterReadingId = meterReadingId;
        this.month = month;
        this.year = year;
        this.roomAmount = roomAmount;
        this.electricityAmount = electricityAmount;
        this.waterAmount = waterAmount;
        this.serviceAmount = serviceAmount;
        this.totalAmount = totalAmount;
        this.isPaid = isPaid;
        this.createdDate = createdDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getContractId() { return contractId; }
    public void setContractId(long contractId) { this.contractId = contractId; }

    public long getMeterReadingId() { return meterReadingId; }
    public void setMeterReadingId(long meterReadingId) { this.meterReadingId = meterReadingId; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getRoomAmount() { return roomAmount; }
    public void setRoomAmount(double roomAmount) { this.roomAmount = roomAmount; }

    public double getElectricityAmount() { return electricityAmount; }
    public void setElectricityAmount(double electricityAmount) { this.electricityAmount = electricityAmount; }

    public double getWaterAmount() { return waterAmount; }
    public void setWaterAmount(double waterAmount) { this.waterAmount = waterAmount; }

    public double getServiceAmount() { return serviceAmount; }
    public void setServiceAmount(double serviceAmount) { this.serviceAmount = serviceAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
