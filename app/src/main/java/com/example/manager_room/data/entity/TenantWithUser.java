package com.example.manager_room.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TenantWithUser {
    @Embedded
    public Tenant tenant;

    @Relation(
            parentColumn = "userId",
            entityColumn = "id"
    )
    public User user;
}