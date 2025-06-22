package com.s23010901.dealsup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp, btnGoogleSignIn;
    private TextView tvLoginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Google Sign-In configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSignUp.setOnClickListener(v -> validateAndRegister());

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, Login.class));
            finish();
        });
    }


    //field validations
    private void validateAndRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);

                        db.collection("users").document(uid)
                                .set(userData, SetOptions.merge())
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Register.this, chooseCards.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            etEmail.setError("Email already registered");
                        } else {
                            Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //google signin
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", account.getDisplayName());
                        userData.put("email", account.getEmail());

                        db.collection("users").document(uid)
                                .set(userData, SetOptions.merge())
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Google Sign-In successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Register.this, chooseCards.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save Google user", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
