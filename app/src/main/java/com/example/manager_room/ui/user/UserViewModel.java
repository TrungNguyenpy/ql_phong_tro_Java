package com.example.manager_room.ui.user;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.manager_room.data.dao.ContractDao;
import com.example.manager_room.data.dao.InvoiceDao;
import com.example.manager_room.data.dao.MeterReadingDao;
import com.example.manager_room.data.dao.RoomDao;
import com.example.manager_room.data.dao.TenantDao;
import com.example.manager_room.data.dao.UserDao;
import com.example.manager_room.data.database.AppDatabase;
import com.example.manager_room.data.entity.Contract;
import com.example.manager_room.data.entity.Invoice;
import com.example.manager_room.data.entity.InvoiceWithContractAndRoom;
import com.example.manager_room.data.entity.MeterReading;
import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.data.entity.Tenant;
import com.example.manager_room.data.entity.User;
import com.example.manager_room.util.PasswordHasher;
import com.example.manager_room.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends AndroidViewModel {
    private final TenantDao tenantDao;
    private final ContractDao contractDao;
    private final InvoiceDao invoiceDao;
    private final RoomDao roomDao;
    private final MeterReadingDao meterReadingDao;
    private final UserDao userDao;
    private final ExecutorService executor;

    private final long currentUserId;
    private final LiveData<User> currentUser;
    private final LiveData<Tenant> currentTenant;
    private final LiveData<Contract> activeContract;

    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        tenantDao = db.tenantDao();
        contractDao = db.contractDao();
        invoiceDao = db.invoiceDao();
        roomDao = db.roomDao();
        meterReadingDao = db.meterReadingDao();
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();

        SessionManager sessionManager = new SessionManager(application);
        currentUserId = sessionManager.getUserId();

        currentUser = userDao.getUserById(currentUserId);
        currentTenant = tenantDao.getTenantByUserId(currentUserId);

        activeContract = Transformations.switchMap(currentTenant, tenant -> {
            if (tenant != null) {
                return contractDao.getActiveContractByTenantId(tenant.getId());
            }
            return null;
        });
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Tenant> getCurrentTenant() {
        return currentTenant;
    }

    public LiveData<Contract> getActiveContract() {
        return activeContract;
    }

    public LiveData<RentalRoom> getRoomById(long roomId) {
        return roomDao.getRoomById(roomId);
    }

    public LiveData<Invoice> getLatestInvoice(long contractId) {
        return invoiceDao.getLatestInvoiceByContractId(contractId);
    }

    // Cập nhật dùng DTO InvoiceWithContractAndRoom
    public LiveData<List<InvoiceWithContractAndRoom>> getInvoicesByContractWithRoom(long contractId) {
        return invoiceDao.getInvoicesByContractWithRoom(contractId);
    }

    public LiveData<List<Invoice>> getInvoicesByContract(long contractId) {
        return invoiceDao.getInvoicesByContract(contractId);
    }

    public LiveData<List<MeterReading>> getMeterReadingsByRoom(long roomId) {
        return meterReadingDao.getReadingsByRoomId(roomId);
    }

    public void updateProfile(String fullName, String phone, String oldPass, String newPass, UpdateCallback callback) {
        executor.execute(() -> {
            User user = currentUser.getValue();
            if (user == null) {
                callback.onError("Không tìm thấy người dùng!");
                return;
            }

            user.setFullName(fullName);
            user.setPhone(phone);

            // Kiểm tra nếu có đổi mật khẩu
            if (!oldPass.isEmpty() && !newPass.isEmpty()) {
                String hashedOldPass = PasswordHasher.hashPassword(oldPass, user.getSalt());
                if (hashedOldPass == null || !hashedOldPass.equals(user.getPasswordHash())) {
                    callback.onError("Mật khẩu cũ không chính xác!");
                    return;
                }
                String newSalt = PasswordHasher.generateSalt();
                user.setSalt(newSalt);
                user.setPasswordHash(PasswordHasher.hashPassword(newPass, newSalt));
            }

            userDao.update(user);
            callback.onSuccess();
        });
    }
}