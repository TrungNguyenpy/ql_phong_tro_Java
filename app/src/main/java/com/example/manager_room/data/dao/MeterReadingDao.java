package com.example.manager_room.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.manager_room.data.entity.MeterReading;
import java.util.List;

@Dao
public interface MeterReadingDao {
    @Insert
    long insert(MeterReading meterReading);

    @Update
    void update(MeterReading meterReading);

    @Query("SELECT * FROM meter_readings WHERE roomId = :roomId ORDER BY year DESC, month DESC")
    LiveData<List<MeterReading>> getReadingsByRoomId(long roomId);

    @Query("SELECT * FROM meter_readings WHERE roomId = :roomId ORDER BY year DESC, month DESC LIMIT 1")
    MeterReading getLatestReadingByRoomIdSync(long roomId);

    // Kiểm tra bản ghi đã tồn tại trong tháng/năm này chưa
    @Query("SELECT * FROM meter_readings WHERE roomId = :roomId AND month = :month AND year = :year LIMIT 1")
    MeterReading getReadingByRoomAndMonthSync(long roomId, int month, int year);
}