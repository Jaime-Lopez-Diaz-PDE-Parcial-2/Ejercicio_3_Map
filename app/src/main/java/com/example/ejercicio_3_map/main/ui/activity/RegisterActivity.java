package com.example.ejercicio_3_map.main.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ejercicio_3_map.R;
import com.example.ejercicio_3_map.main.PantallaPrincipalActivity;
import com.example.ejercicio_3_map.main.model.Credenciales;
import com.example.ejercicio_3_map.main.model.Usuario;
import com.example.ejercicio_3_map.main.repos.firebase.FirebaseHandler;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Spinner spRole;
    private Button btnRegister;
    private FirebaseHandler firebaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseHandler = new FirebaseHandler();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Registrar al usuario en Firebase Authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            // Éxito al registrar en Firebase Authentication
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            // Crear instancias de Credenciales y Usuario
                            Credenciales credenciales = new Credenciales(userId, password);
                            Usuario usuario = new Usuario(userId, name, email, role, null, userId);

                            // Guardar usuario en Firebase Firestore
                            firebaseHandler.guardarUsuario(usuario, taskUsuario -> {
                                if (taskUsuario.isSuccessful()) {
                                    // Guardar credenciales en Firestore
                                    firebaseHandler.guardarCredenciales(credenciales, taskCredenciales -> {
                                        if (taskCredenciales.isSuccessful()) {
                                            onRegisterSuccess(email);
                                        } else {
                                            Toast.makeText(this, "Error al guardar credenciales", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(this, "Error al guardar usuario", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Error al registrar en Firebase Authentication
                            Toast.makeText(this, "Error al registrar en FirebaseAuth: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            // Manejo de errores inesperados
            Toast.makeText(this, "Error durante el registro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Método para manejar el éxito del registro.
     *
     * @param email Correo electrónico del usuario registrado.
     */
    private void onRegisterSuccess(String email) {
        // Guardar sesión automáticamente después del registro
        try {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("userEmail", email)
                    .apply();

            // Mostrar un mensaje de éxito
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();

            // Redirigir a la pantalla principal
            Intent intent = new Intent(RegisterActivity.this, PantallaPrincipalActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}