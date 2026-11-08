package com.example.manager_room.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.manager_room.databinding.FragmentUserProfileBinding;
import com.example.manager_room.ui.auth.LoginActivity;
import com.example.manager_room.util.SessionManager;

public class UserProfileFragment extends Fragment {
    private FragmentUserProfileBinding binding;
    private UserViewModel userViewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.etFullName.setText(user.getFullName());
                binding.etPhone.setText(user.getPhone());
            }
        });

        binding.btnUpdateProfile.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String oldPass = binding.etOldPassword.getText().toString().trim();
            String newPass = binding.etNewPassword.getText().toString().trim();

            userViewModel.updateProfile(fullName, phone, oldPass, newPass, new UserViewModel.UpdateCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    binding.etOldPassword.setText("");
                    binding.etNewPassword.setText("");
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}