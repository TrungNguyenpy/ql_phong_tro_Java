package com.example.manager_room.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.manager_room.data.entity.RentalRoom;
import java.util.List;

@Dao
public interface RoomDao {
    @Insert
    long insert(RentalRoom room);

    @Update
    void update(RentalRoom room);

    @Delete
    void delete(RentalRoom room);

    @Query("SELECT * FROM rooms")
    LiveData<List<RentalRoom>> getAllRooms();

    @Query("SELECT * FROM rooms WHERE id = :id LIMIT 1")
    LiveData<RentalRoom> getRoomById(long id);

    @Query("SELECT COUNT(*) FROM rooms WHERE status = :status")
    LiveData<Integer> getRoomCountByStatus(String status);

    @Query("SELECT * FROM rooms WHERE id = :id LIMIT 1")
    RentalRoom getRoomByIdSync(long id);
}
