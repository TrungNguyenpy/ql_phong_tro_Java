package com.example.manager_room.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.manager_room.data.entity.User;
import com.example.manager_room.data.repository.UserRepository;

public class AuthViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> authSuccessState = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageState = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<User> getAuthSuccessState() {
        return authSuccessState;
    }

    public LiveData<String> getErrorMessageState() {
        return errorMessageState;
    }

    public void login(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            errorMessageState.setValue("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        userRepository.login(username, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authSuccessState.postValue(user);
            }

            @Override
            public void onError(String message) {
                errorMessageState.postValue(message);
            }
        });
    }

    public void register(String username, String password, String fullName, String phone) {
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
            errorMessageState.setValue("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        userRepository.register(username, password, fullName, phone, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authSuccessState.postValue(user);
            }

            @Override
            public void onError(String message) {
                errorMessageState.postValue(message);
            }
        });
    }
}