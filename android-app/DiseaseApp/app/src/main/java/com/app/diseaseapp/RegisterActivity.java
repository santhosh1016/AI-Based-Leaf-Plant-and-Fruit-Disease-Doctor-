package com.app.diseaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword;
    Button btnRegister, btnLogin;
    TextInputLayout nameLayout, emailLayout, passwordLayout;

    FirebaseAuth mAuth;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            boolean valid = true;
            nameLayout.setError(null);
            emailLayout.setError(null);
            passwordLayout.setError(null);

            if (TextUtils.isEmpty(name)) {
                nameLayout.setError(getString(R.string.full_name_required));
                valid = false;
            }
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
            } else if (password.length() < 6) {
                passwordLayout.setError(getString(R.string.password_min_length));
                valid = false;
            }

            if (!valid) return;

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            userRef.child(userId).child("name").setValue(name);
                            userRef.child(userId).child("email").setValue(email);
                            Toast.makeText(this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();

                            // 🏠 Redirect to HomeActivity after successful registration
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            String message = task.getException() != null ? task.getException().getMessage() : "";
                            Toast.makeText(this, getString(R.string.registration_failed, message), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
