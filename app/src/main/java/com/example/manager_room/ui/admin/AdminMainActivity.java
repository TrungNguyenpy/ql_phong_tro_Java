package com.example.manager_room.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import androidx.navigation.ui.AppBarConfiguration;

import com.example.manager_room.R;
import com.example.manager_room.databinding.ActivityAdminMainBinding;
import com.example.manager_room.ui.auth.LoginActivity;
import com.example.manager_room.util.SessionManager;

public class AdminMainActivity extends AppCompatActivity {
    private ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarAdmin);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_admin);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            AppBarConfiguration appBarConfig = new AppBarConfiguration.Builder(
                    R.id.navigation_admin_dashboard, R.id.navigation_admin_rooms,
                    R.id.navigation_admin_meter, R.id.navigation_admin_invoices).build();
            NavigationUI.setupWithNavController(binding.toolbarAdmin, navController, appBarConfig);;
            NavigationUI.setupWithNavController(binding.bottomNavigationAdmin, navController);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Xóa phiên và về lại màn hình Đăng nhập
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.clearSession();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}