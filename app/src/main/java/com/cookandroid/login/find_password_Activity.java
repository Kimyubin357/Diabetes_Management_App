package com.cookandroid.login;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Calendar;

public class find_password_Activity extends AppCompatActivity {

    private EditText edit_email;
    private Button btn_find;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);

        // 상태 바 색상 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.red_red_red));
        }

        edit_email = findViewById(R.id.edit_email);

        btn_find = findViewById(R.id.btn_find);

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = edit_email.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(find_password_Activity.this, " 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(find_password_Activity.this, "비밀번호 재설정 이메일을 전송했습니다.", Toast.LENGTH_SHORT).show();
                                // 비밀번호 재설정 이메일을 보냈으므로 메인 액티비티로 이동
                                Intent intent = new Intent(find_password_Activity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                // Firebase의 예외 처리
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    Toast.makeText(find_password_Activity.this, "존재하지 않는 이메일입니다.", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(find_password_Activity.this, "비밀번호 재설정 이메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


    }
}