package com.example.manager_room.ui.admin;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.manager_room.data.dao.ContractDao;
import com.example.manager_room.data.dao.InvoiceDao;
import com.example.manager_room.data.dao.MeterReadingDao;
import com.example.manager_room.data.dao.RoomDao;
import com.example.manager_room.data.dao.TenantDao;
import com.example.manager_room.data.database.AppDatabase;
import com.example.manager_room.data.entity.Contract;
import com.example.manager_room.data.entity.Invoice;
import com.example.manager_room.data.entity.InvoiceWithContractAndRoom;
import com.example.manager_room.data.entity.MeterReading;
import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.data.entity.TenantWithUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminViewModel extends AndroidViewModel {
    private final RoomDao roomDao;
    private final MeterReadingDao meterReadingDao;
    private final InvoiceDao invoiceDao;
    private final ContractDao contractDao;
    private final TenantDao tenantDao;
    private final ExecutorService executor;

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface TenantDetailsCallback {
        void onLoaded(TenantWithUser tenantWithUser, Contract contract);
        void onError(String error);
    }

    public AdminViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        roomDao = db.roomDao();
        meterReadingDao = db.meterReadingDao();
        invoiceDao = db.invoiceDao();
        contractDao = db.contractDao();
        tenantDao = db.tenantDao();
        executor = Executors.newFixedThreadPool(2);
    }

    // LiveData Queries
    public LiveData<List<InvoiceWithContractAndRoom>> getAllInvoicesWithRoom() {
        return invoiceDao.getAllInvoicesWithRoom();
    }

    public LiveData<List<RentalRoom>> getAllRooms() {
        return roomDao.getAllRooms();
    }

    public LiveData<Integer> getRoomCountByStatus(String status) {
        return roomDao.getRoomCountByStatus(status);
    }

    public LiveData<List<Invoice>> getUnpaidInvoices() {
        return invoiceDao.getUnpaidInvoices();
    }

    public LiveData<List<Invoice>> getAllInvoices() {
        return invoiceDao.getAllInvoices();
    }

    public LiveData<List<TenantWithUser>> getAllTenantsWithUsers() {
        return tenantDao.getAllTenantsWithUsers();
    }

    // Quản lý Phòng
    public void addRoom(String roomNumber, double price, double area, ActionCallback callback) {
        executor.execute(() -> {
            try {
                RentalRoom room = new RentalRoom(roomNumber, price, area, "AVAILABLE", "");
                roomDao.insert(room);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError("Không thể thêm phòng (có thể trùng số phòng)!");
            }
        });
    }

    public void updateRoom(long roomId, String roomNumber, double price, double area, ActionCallback callback) {
        executor.execute(() -> {
            try {
                RentalRoom room = roomDao.getRoomByIdSync(roomId);
                if (room == null) {
                    if (callback != null) callback.onError("Không tìm thấy phòng!");
                    return;
                }
                room.setRoomNumber(roomNumber);
                room.setPrice(price);
                room.setArea(area);
                roomDao.update(room);
                if (callback != null) callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                if (callback != null) callback.onError("Số phòng đã tồn tại!");
            } catch (Exception e) {
                if (callback != null) callback.onError("Lỗi cập nhật phòng: " + e.getMessage());
            }
        });
    }

    // Quản lý Hợp đồng
    public void createContract(long roomId, long tenantId, String startDate, String endDate, double rentalPrice, double deposit, String terms, ActionCallback callback) {
        executor.execute(() -> {
            Contract contract = new Contract(roomId, tenantId, startDate, endDate, rentalPrice, deposit, terms, "ACTIVE");
            contractDao.insert(contract);

            RentalRoom room = roomDao.getRoomByIdSync(roomId);
            if (room != null) {
                room.setStatus("RENTED");
                roomDao.update(room);
            }
            if (callback != null) callback.onSuccess();
        });
    }

    // 1. Quản lý Điện Nước (UPSERT - Cập nhật nếu đã tồn tại)
    public void saveOrUpdateMeterReading(long roomId, int month, int year, int newElectricity, int newWater, SaveCallback callback) {
        executor.execute(() -> {
            try {
                MeterReading existing = meterReadingDao.getReadingByRoomAndMonthSync(roomId, month, year);

                if (existing != null) {
                    existing.setNewElectricity(newElectricity);
                    existing.setNewWater(newWater);
                    meterReadingDao.update(existing);

                    if (callback != null) {
                        callback.onSuccess("Đã cập nhật chỉ số điện nước cho tháng " + month + "/" + year);
                    }
                } else {
                    MeterReading latest = meterReadingDao.getLatestReadingByRoomIdSync(roomId);
                    int oldElec = (latest != null) ? latest.getNewElectricity() : 0;
                    int oldWater = (latest != null) ? latest.getNewWater() : 0;

                    MeterReading newReading = new MeterReading(roomId, month, year, oldElec, newElectricity, oldWater, newWater);
                    meterReadingDao.insert(newReading);

                    if (callback != null) {
                        callback.onSuccess("Đã lưu thành công chỉ số điện nước mới!");
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Lỗi khi lưu dữ liệu: " + e.getMessage());
                }
            }
        });
    }

    // 2. Quản lý Hóa Đơn (UPSERT - Cập nhật đè hóa đơn nếu đã tạo trong tháng)
    public void createOrUpdateInvoice(long roomId, double electricRate, double waterRate, double serviceFee, SaveCallback callback) {
        executor.execute(() -> {
            try {
                Contract contract = contractDao.getActiveContractByRoomIdSync(roomId);
                if (contract == null) {
                    if (callback != null) callback.onError("Phòng này hiện chưa có hợp đồng thuê!");
                    return;
                }

                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);

                MeterReading latestReading = meterReadingDao.getLatestReadingByRoomIdSync(roomId);
                if (latestReading == null) {
                    if (callback != null) callback.onError("Chưa nhập chỉ số điện nước cho tháng này!");
                    return;
                }

                int electricUsed = latestReading.getNewElectricity() - latestReading.getOldElectricity();
                int waterUsed = latestReading.getNewWater() - latestReading.getOldWater();

                double roomAmount = roomDao.getRoomByIdSync(roomId).getPrice();
                double elecAmount = electricUsed * electricRate;
                double waterAmount = waterUsed * waterRate;
                double totalAmount = roomAmount + elecAmount + waterAmount + serviceFee;

                String createdDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                Invoice existingInvoice = invoiceDao.getInvoiceByContractAndMonthSync(contract.getId(), month, year);

                if (existingInvoice != null) {
                    existingInvoice.setMeterReadingId(latestReading.getId());
                    existingInvoice.setRoomAmount(roomAmount);
                    existingInvoice.setElectricityAmount(elecAmount);
                    existingInvoice.setWaterAmount(waterAmount);
                    existingInvoice.setServiceAmount(serviceFee);
                    existingInvoice.setTotalAmount(totalAmount);
                    existingInvoice.setCreatedDate(createdDate);

                    invoiceDao.update(existingInvoice);
                    if (callback != null) callback.onSuccess("Đã cập nhật lại hóa đơn Tháng " + month + "/" + year);
                } else {
                    Invoice newInvoice = new Invoice(contract.getId(), latestReading.getId(), month, year, roomAmount, elecAmount, waterAmount, serviceFee, totalAmount, false, createdDate);
                    invoiceDao.insert(newInvoice);
                    if (callback != null) callback.onSuccess("Tạo hóa đơn mới thành công!");
                }

            } catch (Exception e) {
                if (callback != null) callback.onError("Lỗi xử lý hóa đơn: " + e.getMessage());
            }
        });
    }

    // 3. Xóa Hóa Đơn (Bổ sung để xóa hóa đơn dư thừa/rác)
    public void deleteInvoice(Invoice invoice, ActionCallback callback) {
        executor.execute(() -> {
            try {
                invoiceDao.delete(invoice);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError("Lỗi xóa hóa đơn: " + e.getMessage());
            }
        });
    }

    public void markInvoiceAsPaid(long invoiceId, ActionCallback callback) {
        executor.execute(() -> {
            Invoice invoice = invoiceDao.getInvoiceByIdSync(invoiceId);
            if (invoice != null) {
                invoice.setPaid(true);
                invoiceDao.update(invoice);
                if (callback != null) callback.onSuccess();
            } else {
                if (callback != null) callback.onError("Không tìm thấy hóa đơn!");
            }
        });
    }

    public void getTenantDetailsByRoom(long roomId, TenantDetailsCallback callback) {
        executor.execute(() -> {
            Contract contract = contractDao.getActiveContractByRoomIdSync(roomId);
            if (contract == null) {
                callback.onError("Phòng này chưa có hợp đồng thuê active!");
                return;
            }

            TenantWithUser tenantWithUser = tenantDao.getTenantWithUserByIdSync(contract.getTenantId());
            if (tenantWithUser != null && tenantWithUser.user != null) {
                callback.onLoaded(tenantWithUser, contract);
            } else {
                callback.onError("Không tìm thấy dữ liệu người thuê tương ứng!");
            }
        });
    }
}