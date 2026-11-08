package com.example.manager_room.data.dao;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.manager_room.data.entity.Contract;
import java.util.List;

@Dao
public interface ContractDao {
    @Insert
    long insert(Contract contract);

    @Update
    void update(Contract contract);

    @Query("SELECT * FROM contracts WHERE status = 'ACTIVE'")
    LiveData<List<Contract>> getActiveContracts();

    @Query("SELECT * FROM contracts WHERE tenantId = :tenantId AND status = 'ACTIVE' LIMIT 1")
    LiveData<Contract> getActiveContractByTenantId(long tenantId);

    @Query("SELECT * FROM contracts WHERE roomId = :roomId AND status = 'ACTIVE' LIMIT 1")
    Contract getActiveContractByRoomIdSync(long roomId);


}

