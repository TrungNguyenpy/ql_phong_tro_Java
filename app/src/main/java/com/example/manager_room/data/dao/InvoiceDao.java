package com.example.manager_room.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.manager_room.data.entity.Invoice;
import com.example.manager_room.data.entity.InvoiceWithContractAndRoom;

import java.util.List;

@Dao
public interface InvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Invoice invoice);

    @Update
    void update(Invoice invoice);

    @Delete
    void delete(Invoice invoice); // Phương thức xóa hóa đơn

    @Query("SELECT * FROM invoices ORDER BY id DESC")
    LiveData<List<Invoice>> getAllInvoices();

    @Query("SELECT * FROM invoices WHERE contractId = :contractId ORDER BY id DESC")
    LiveData<List<Invoice>> getInvoicesByContract(long contractId);

    @Query("SELECT * FROM invoices WHERE contractId = :contractId ORDER BY id DESC LIMIT 1")
    LiveData<Invoice> getLatestInvoiceByContractId(long contractId);

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE isPaid = 1 AND month = :month AND year = :year")
    LiveData<Double> getTotalRevenueByMonth(int month, int year);

    @Query("SELECT * FROM invoices WHERE isPaid = 0")
    LiveData<List<Invoice>> getUnpaidInvoices();

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    Invoice getInvoiceByIdSync(long id);

    // Kiểm tra và lấy hóa đơn theo hợp đồng, tháng và năm (Phục vụ logic UPSERT)
    @Query("SELECT * FROM invoices WHERE contractId = :contractId AND month = :month AND year = :year LIMIT 1")
    Invoice getInvoiceByContractAndMonthSync(long contractId, int month, int year);

    // Lấy tất cả hóa đơn kèm thông tin Tên/Số phòng đầy đủ
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    LiveData<List<InvoiceWithContractAndRoom>> getAllInvoicesWithRoom();

    @Transaction
    @Query("SELECT * FROM invoices WHERE contractId = :contractId ORDER BY id DESC")
    LiveData<List<InvoiceWithContractAndRoom>> getInvoicesByContractWithRoom(long contractId);
}