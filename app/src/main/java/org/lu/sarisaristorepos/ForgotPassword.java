package org.lu.sarisaristorepos;

import android.content.Intent;
import android.os.Bundle;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText recoverEmailEditText;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        recoverEmailEditText = findViewById(R.id.recoverEmail_et);
        TextView sendCodeButton = findViewById(R.id.sendCode_btn);

        sendCodeButton.setOnClickListener(v -> sendRecoveryCode());

    }

    private void sendRecoveryCode() {
        String email = recoverEmailEditText.getText().toString().trim();

        if (!email.isEmpty()) {
            // Send the recovery code to the user's email using Firebase Authentication
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, R.string.recovery_email_sent, Toast.LENGTH_SHORT).show();
                            LoginActivity();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to send recovery email", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(this, R.string.enter_email, Toast.LENGTH_SHORT).show();
        }
    }

    private void LoginActivity(){
        Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
        startActivity(intent);
    }

}
