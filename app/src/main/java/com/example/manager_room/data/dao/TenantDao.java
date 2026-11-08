package com.example.manager_room.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.manager_room.data.entity.Tenant;
import com.example.manager_room.data.entity.TenantWithUser;

import java.util.List;

@Dao
public interface TenantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Tenant tenant);

    @Update
    void update(Tenant tenant);

    @Query("SELECT * FROM tenants WHERE userId = :userId LIMIT 1")
    LiveData<Tenant> getTenantByUserId(long userId);

    @Transaction
    @Query("SELECT * FROM tenants")
    LiveData<List<TenantWithUser>> getAllTenantsWithUsers();

    // Bổ sung phương thức truy vấn đồng bộ này
    @Transaction
    @Query("SELECT * FROM tenants WHERE id = :tenantId LIMIT 1")
    TenantWithUser getTenantWithUserByIdSync(long tenantId);
}