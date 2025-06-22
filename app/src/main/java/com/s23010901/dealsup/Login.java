package com.s23010901.dealsup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.*;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn, btnGoogleSignIn;
    private TextView tvRegister;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegister = findViewById(R.id.tvRegister);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from strings.xml
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Firebase Email/Password Sign-In
        btnSignIn.setOnClickListener(view -> validateAndLogin());

        // Google Sign-In
        btnGoogleSignIn.setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Navigate to Register screen
        tvRegister.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, Register.class));
            finish();
        });
    }

    //field validations
    private void validateAndLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, Dashboard.class));
                        finish();
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            etEmail.setError("No account found with this email");
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            etPassword.setError("Incorrect password");
                        } else {
                            Toast.makeText(Login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Google Sign-In result handler
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, Dashboard.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Firebase auth failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
