package com.example.manager_room.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tenants",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class Tenant {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long userId; // Liên kết với tài khoản User
    private String identityCard; // CCCD/CMND
    private String address;

    public Tenant(long userId, String identityCard, String address) {
        this.userId = userId;
        this.identityCard = identityCard;
        this.address = address;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getIdentityCard() { return identityCard; }
    public void setIdentityCard(String identityCard) { this.identityCard = identityCard; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
