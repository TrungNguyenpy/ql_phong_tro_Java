package com.example.manager_room.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class InvoiceWithContractAndRoom {
    @Embedded
    public Invoice invoice;

    @Relation(
            entity = Contract.class,
            parentColumn = "contractId",
            entityColumn = "id"
    )
    public ContractWithRoom contractWithRoom;

    public static class ContractWithRoom {
        @Embedded
        public Contract contract;

        @Relation(
                parentColumn = "roomId",
                entityColumn = "id"
        )
        public RentalRoom room;
    }
}