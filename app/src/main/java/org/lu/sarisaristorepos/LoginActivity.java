package org.lu.sarisaristorepos;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText editUserTextName, getEditUserTextPassword;
    Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        editUserTextName = findViewById(R.id.editUsertxt);
        getEditUserTextPassword = findViewById(R.id.editPasswordTxt);
        btnLogin = findViewById(R.id.loginBtn);
        btnRegister = findViewById(R.id.registerBtn);

        btnLogin.setOnClickListener(v -> Login());

        btnRegister.setOnClickListener(v -> Register());

    }

    //Login Method
    public void Login() {
        String email = editUserTextName.getText().toString().trim();
        String pass = getEditUserTextPassword.getText().toString().trim();

        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (!pass.isEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(authResult -> {
                            Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Login Failed" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } else if (email.isEmpty()) {
            editUserTextName.setError("Email cannot be empty");
        } else if (pass.isEmpty()) {
            getEditUserTextPassword.setError("Password cannot be empty");
        } else {
            editUserTextName.setError("Invalid Email");
        }


    }

    //This method redirects to the Register Activity
    public  void Register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

}