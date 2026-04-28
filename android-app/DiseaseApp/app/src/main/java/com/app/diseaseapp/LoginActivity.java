package com.app.diseaseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView btnGoRegister;
    FirebaseAuth mAuth;

    TextInputLayout emailLayout, passwordLayout;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Auto-login if already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, getString(R.string.camera_permission_granted), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.camera_permission_required_message), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnLogin.setOnClickListener(v -> {
            if (!isCameraPermissionGranted()) {
                showPermissionDialog();
                return;
            }

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            boolean valid = true;
            emailLayout.setError(null);
            passwordLayout.setError(null);

            if (TextUtils.isEmpty(email)) {
                emailLayout.setError(getString(R.string.email_required));
                valid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.setError(getString(R.string.valid_email_required));
                valid = false;
            }

            if (TextUtils.isEmpty(password)) {
                passwordLayout.setError(getString(R.string.password_required));
                valid = false;
            }

            if (!valid) return;

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            String message = task.getException() != null ? task.getException().getMessage() : "";
                            Toast.makeText(this, getString(R.string.login_failed, message), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.camera_permission_required_title)
                .setMessage(R.string.camera_permission_rationale)
                .setPositiveButton(R.string.allow, (dialog, which) -> requestPermissionLauncher.launch(CAMERA_PERMISSION))
                .setNegativeButton(R.string.not_now, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
