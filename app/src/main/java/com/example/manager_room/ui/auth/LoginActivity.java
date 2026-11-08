package com.example.manager_room.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.manager_room.data.database.AppDatabase;
import com.example.manager_room.data.entity.User;
import com.example.manager_room.databinding.ActivityLoginBinding;
import com.example.manager_room.databinding.ItemFormFieldBinding;
import com.example.manager_room.util.DialogUtils;
import com.example.manager_room.util.PasswordHasher;
import com.example.manager_room.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Chỉ tự động đăng nhập khi người dùng đã chọn "Ghi nhớ"
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isRemember()) {
                navigateToMain(sessionManager.getRole());
                return;
            }
            sessionManager.clearSession();
        }

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        observeViewModel();

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            authViewModel.login(username, password);
        });

        binding.tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void observeViewModel() {
        authViewModel.getAuthSuccessState().observe(this, user -> {
            sessionManager.saveSession(user.getId(), user.getRole());
            sessionManager.setRemember(binding.cbRemember.isChecked());
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            navigateToMain(user.getRole());
        });

        authViewModel.getErrorMessageState().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void showForgotPasswordDialog() {
        String[] labels = {"Tên đăng nhập", "Số điện thoại đã đăng ký",
                "Mật khẩu mới (tối thiểu 6 ký tự)", "Nhập lại mật khẩu mới"};
        int passType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        int[] types = {InputType.TYPE_CLASS_TEXT, InputType.TYPE_CLASS_PHONE, passType, passType};
        int pad = (int) (20 * getResources().getDisplayMetrics().density);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, 0, pad, pad);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(container);

        List<ItemFormFieldBinding> fields = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            ItemFormFieldBinding f = ItemFormFieldBinding.inflate(getLayoutInflater(), container, false);
            f.tvFieldLabel.setText(labels[i]);
            f.etFieldValue.setInputType(types[i]);
            container.addView(f.getRoot());
            fields.add(f);
        }

        DialogUtils.showForm(new MaterialAlertDialogBuilder(this)
                .setTitle("Quên mật khẩu")
                .setMessage("Nhập tên đăng nhập và số điện thoại đã dùng khi đăng ký để đặt lại mật khẩu.")
                .setView(scroll)
                .setPositiveButton("Đặt lại", (d, w) -> resetPassword(
                        fields.get(0).etFieldValue.getText().toString().trim(),
                        fields.get(1).etFieldValue.getText().toString().trim(),
                        fields.get(2).etFieldValue.getText().toString().trim(),
                        fields.get(3).etFieldValue.getText().toString().trim()))
                .setNegativeButton("Hủy", null));
    }

    private void resetPassword(String username, String phone, String newPass, String confirm) {
        if (username.isEmpty() || phone.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu tối thiểu 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);
        executor.execute(() -> {
            User user = db.userDao().getUserByUsernameSync(username);
            if (user == null || user.getPhone() == null || !user.getPhone().trim().equals(phone)) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Tên đăng nhập hoặc số điện thoại không khớp!", Toast.LENGTH_SHORT).show());
                return;
            }

            String salt = PasswordHasher.generateSalt();
            user.setSalt(salt);
            user.setPasswordHash(PasswordHasher.hashPassword(newPass, salt));
            db.userDao().update(user);
            runOnUiThread(() ->
                    Toast.makeText(this, "Đã đặt lại mật khẩu. Hãy đăng nhập lại!", Toast.LENGTH_LONG).show());
        });
    }

    private void navigateToMain(String role) {
        Intent intent;
        if ("ADMIN".equals(role)) {
            intent = new Intent(this, com.example.manager_room.ui.admin.AdminMainActivity.class);
        } else {
            intent = new Intent(this, com.example.manager_room.ui.user.UserMainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}