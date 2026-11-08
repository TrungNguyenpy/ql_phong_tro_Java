package com.example.manager_room.data.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "contracts",
        foreignKeys = {
                @ForeignKey(entity = RentalRoom.class, parentColumns = "id", childColumns = "roomId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Tenant.class, parentColumns = "id", childColumns = "tenantId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("roomId"), @Index("tenantId")}
)
public class Contract {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long roomId;
    private long tenantId;
    private String startDate; // Địn dạng yyyy-MM-dd
    private String endDate;   // Định dạng yyyy-MM-dd
    private double rentalPrice;
    private double deposit;
    private String terms;
    private String status;    // "ACTIVE", "EXPIRED", "TERMINATED"

    public Contract(long roomId, long tenantId, String startDate, String endDate, double rentalPrice, double deposit, String terms, String status) {
        this.roomId = roomId;
        this.tenantId = tenantId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentalPrice = rentalPrice;
        this.deposit = deposit;
        this.terms = terms;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }

    public long getTenantId() { return tenantId; }
    public void setTenantId(long tenantId) { this.tenantId = tenantId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public double getRentalPrice() { return rentalPrice; }
    public void setRentalPrice(double rentalPrice) { this.rentalPrice = rentalPrice; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
