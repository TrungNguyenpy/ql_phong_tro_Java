package com.example.manager_room.ui.auth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.manager_room.data.dao.TenantDao;
import com.example.manager_room.data.dao.UserDao;
import com.example.manager_room.data.database.AppDatabase;
import com.example.manager_room.data.entity.Tenant;
import com.example.manager_room.data.entity.User;
import com.example.manager_room.databinding.ActivityRegisterBinding;
import com.example.manager_room.util.PasswordHasher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private UserDao userDao;
    private TenantDao tenantDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppDatabase db = AppDatabase.getInstance(this);
        userDao = db.userDao();
        tenantDao = db.tenantDao();

        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void registerUser() {
        String fullName = binding.etFullName.getText().toString().trim();
        String username = binding.etUsername.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        String confirm = binding.etConfirmPassword.getText().toString().trim();
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu tối thiểu 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            User existingUser = userDao.getUserByUsernameSync(username);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show());
                return;
            }

            String salt = PasswordHasher.generateSalt();
            String passwordHash = PasswordHasher.hashPassword(password, salt);

            User newUser = new User(username, passwordHash, salt, fullName, phone, "USER");
            long newUserId = userDao.insert(newUser);

            // Tự động khởi tạo bản ghi Tenant để gán cho phòng trọ
            Tenant newTenant = new Tenant(newUserId, "Chưa cập nhật CCCD", "Chưa cập nhật địa chỉ");
            tenantDao.insert(newTenant);

            runOnUiThread(() -> {
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}