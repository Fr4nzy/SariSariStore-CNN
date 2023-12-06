package org.lu.sarisaristorepos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText editUserTextName, getEditUserTextPassword;
    Button btnLogin, btnRegister, btnPhone;
    TextView forgotPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        editUserTextName = findViewById(R.id.editUsertxt);
        getEditUserTextPassword = findViewById(R.id.editPasswordTxt);
        btnLogin = findViewById(R.id.loginBtn);
        btnRegister = findViewById(R.id.registerBtn);
        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);
        btnPhone = findViewById(R.id.phoneBtn);

        btnLogin.setOnClickListener(v -> Login());

        btnRegister.setOnClickListener(v -> Register());

        btnPhone.setOnClickListener(view -> Phone());

        forgotPasswordBtn.setOnClickListener(v -> {ForgotPassword(); });

    }

    //Login Method
    public void Login() {
        String email = editUserTextName.getText().toString().trim();
        String pass = getEditUserTextPassword.getText().toString().trim();

        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (!pass.isEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(authResult -> {
                            Toast.makeText(LoginActivity.this, R.string.login_success_message, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, R.string.login_failed_message + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } else if (email.isEmpty()) {
            editUserTextName.setError("Email cannot be empty");
        } else if (pass.isEmpty()) {
            getEditUserTextPassword.setError("Password cannot be empty");
        } else {
            editUserTextName.setError("Invalid Email");
        }


    }

    //The Register Activity
    public  void Register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    //Forgot Password Activity
    public void ForgotPassword() {
        Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
        startActivity(intent);
    }

    public void Phone(){
        Intent intent = new Intent(LoginActivity.this, LoginPhoneNumber.class);
        startActivity(intent);
    }

}