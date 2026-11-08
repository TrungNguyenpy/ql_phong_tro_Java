package com.example.manager_room.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "meter_readings",
        foreignKeys = @ForeignKey(entity = RentalRoom.class, parentColumns = "id", childColumns = "roomId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("roomId")}
)
public class MeterReading {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long roomId;
    private int month;
    private int year;
    private int oldElectricity;
    private int newElectricity;
    private int oldWater;
    private int newWater;

    public MeterReading(long roomId, int month, int year, int oldElectricity, int newElectricity, int oldWater, int newWater) {
        this.roomId = roomId;
        this.month = month;
        this.year = year;
        this.oldElectricity = oldElectricity;
        this.newElectricity = newElectricity;
        this.oldWater = oldWater;
        this.newWater = newWater;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getOldElectricity() { return oldElectricity; }
    public void setOldElectricity(int oldElectricity) { this.oldElectricity = oldElectricity; }

    public int getNewElectricity() { return newElectricity; }
    public void setNewElectricity(int newElectricity) { this.newElectricity = newElectricity; }

    public int getOldWater() { return oldWater; }
    public void setOldWater(int oldWater) { this.oldWater = oldWater; }

    public int getNewWater() { return newWater; }
    public void setNewWater(int newWater) { this.newWater = newWater; }
}