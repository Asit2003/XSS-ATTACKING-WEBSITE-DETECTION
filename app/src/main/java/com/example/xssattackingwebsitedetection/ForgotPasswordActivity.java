package com.example.xssattackingwebsitedetection;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Firebase authentication object
    private FirebaseAuth mAuth;

    // UI elements
    private EditText mEmailEditText;
    private Button mResetPasswordButton;
    private TextView mResetPasswordSentTextView;
    private TextView mErrorMessageTextView;
    private Button mBackToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // Get UI elements
        mEmailEditText = findViewById(R.id.edit_text_email);
        mResetPasswordButton = findViewById(R.id.button_reset_password);
        mResetPasswordSentTextView = findViewById(R.id.text_view_reset_password_sent);
        mErrorMessageTextView = findViewById(R.id.text_view_error_message);
        mBackToLoginButton = findViewById(R.id.button_back_to_login);

        // Set click listener for reset password button
        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user's email
                String email = mEmailEditText.getText().toString().trim();

                // Validate email
                if (TextUtils.isEmpty(email)) {
                    // Display error message if email is empty
                    mErrorMessageTextView.setText(getString(R.string.empty_email_error));
                    mErrorMessageTextView.setVisibility(View.VISIBLE);
                    mResetPasswordSentTextView.setVisibility(View.GONE);
                    return;
                }

                // Send password reset email to user's email address
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Display reset password sent message
                                    mResetPasswordSentTextView.setVisibility(View.VISIBLE);
                                    mErrorMessageTextView.setVisibility(View.GONE);
                                } else {
                                    // Display error message if unable to send reset password email
                                    mErrorMessageTextView.setText(getString(R.string.reset_password_error));
                                    mErrorMessageTextView.setVisibility(View.VISIBLE);
                                    mResetPasswordSentTextView.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });

        // Set click listener for back to login button
        mBackToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to login activity
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
