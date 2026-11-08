package com.example.manager_room.data.repository;

import android.app.Application;

import com.example.manager_room.data.dao.TenantDao;
import com.example.manager_room.data.dao.UserDao;
import com.example.manager_room.data.database.AppDatabase;
import com.example.manager_room.data.entity.Tenant;
import com.example.manager_room.data.entity.User;
import com.example.manager_room.util.PasswordHasher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private final TenantDao tenantDao;
    private final ExecutorService executor;

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        tenantDao = db.tenantDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Đăng nhập
    public void login(String username, String password, AuthCallback callback) {
        executor.execute(() -> {
            User user = userDao.getUserByUsernameSync(username);
            if (user == null) {
                callback.onError("Tài khoản không tồn tại!");
                return;
            }

            String hashedInput = PasswordHasher.hashPassword(password, user.getSalt());
            if (hashedInput != null && hashedInput.equals(user.getPasswordHash())) {
                callback.onSuccess(user);
            } else {
                callback.onError("Mật khẩu không chính xác!");
            }
        });
    }

    // Đăng ký tài khoản mới & tự động khởi tạo Tenant
    public void register(String username, String password, String fullName, String phone, AuthCallback callback) {
        executor.execute(() -> {
            User existingUser = userDao.getUserByUsernameSync(username);
            if (existingUser != null) {
                callback.onError("Tên đăng nhập đã tồn tại!");
                return;
            }

            String salt = PasswordHasher.generateSalt();
            String passwordHash = PasswordHasher.hashPassword(password, salt);

            User newUser = new User(username, passwordHash, salt, fullName, phone, "USER");
            long newUserId = userDao.insert(newUser);
            newUser.setId(newUserId);

            // Tự động tạo hồ sơ Tenant để Admin gán vào phòng trọ
            Tenant newTenant = new Tenant(newUserId, "Chưa cập nhật CCCD", "Chưa cập nhật địa chỉ");
            tenantDao.insert(newTenant);

            callback.onSuccess(newUser);
        });
    }
}