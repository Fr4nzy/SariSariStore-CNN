package org.lu.sarisaristorepos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText editRegEmail, editRegPassword, editRegConfirmPassword;
    Button regBtn, backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        editRegEmail = findViewById(R.id.editRegEmail);
        editRegPassword = findViewById(R.id.editRegPassword);
        editRegConfirmPassword = findViewById(R.id.editRegCnfrmPword);
        regBtn = findViewById(R.id.regBtn);
        backBtn = findViewById(R.id.backBtn);

        regBtn.setOnClickListener(v -> {
            String user = editRegEmail.getText().toString().trim();
            String password = editRegPassword.getText().toString().trim();
            String cpassword = editRegConfirmPassword.getText().toString().trim();

            // Check if the email is empty or not in a valid format
            if (user.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty() || cpassword.isEmpty()) {
                Toast.makeText(this, "Please fill in both password fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(cpassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            } else {
                auth.createUserWithEmailAndPassword(user, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Sign-Up Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Sign-Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
