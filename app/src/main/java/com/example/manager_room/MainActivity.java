package com.example.manager_room;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.manager_room.ui.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mở màn hình LoginActivity
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}