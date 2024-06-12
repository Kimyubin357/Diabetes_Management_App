package com.cookandroid.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class email_modify_Activity extends AppCompatActivity {
    private EditText editTextNewEmail;
    private EditText editTextPassword;
    private Button buttonChangeEmail;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_modify);
        editTextNewEmail = findViewById(R.id.editTextNewEmail);

        editTextPassword = findViewById(R.id.editTextPassword);
        buttonChangeEmail = findViewById(R.id.buttonChangeEmail);

        buttonChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = editTextNewEmail.getText().toString().trim();

                String password = editTextPassword.getText().toString().trim();

                if (TextUtils.isEmpty(newEmail)) {
                    Toast.makeText(email_modify_Activity.this, "변경하실 이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError("비밀번호를 입력해주세요");
                    return;
                }

                reauthenticateAndChangeEmail(newEmail, password);
            }
        });
    }
    private void reauthenticateAndChangeEmail(final String newEmail, String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);


            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updateEmail(newEmail)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(email_modify_Activity.this, "이메일 변경 완료", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(email_modify_Activity.this, "이메일 변경 실패", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(email_modify_Activity.this, "로그인 인증 실패" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}