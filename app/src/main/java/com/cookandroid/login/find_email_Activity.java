package com.cookandroid.login;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class find_email_Activity extends AppCompatActivity {
    TextView idTextView;
    private SharedPreferences sharedPreferences;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_email);

        idTextView = findViewById(R.id.findIdView);
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String lastLoggedInEmail = sharedPreferences.getString("last_logged_in_email", null);
        if (lastLoggedInEmail != null) {
            // 이메일 아이디 부분 가리기
            String maskedEmail = maskEmail(lastLoggedInEmail);
            idTextView.setText(maskedEmail);
        } else {
            idTextView.setText("이전 로그인 기록이 없습니다.");
        }
    }
    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex > 0) {
            String id = email.substring(0, atIndex);
            int maskLength = id.length() / 2;
            String maskedId = id.substring(0, maskLength) + "*".repeat(id.length() - maskLength);
            return maskedId + email.substring(atIndex);
        }
        return email; // @ 기호가 없으면 원본 이메일 반환
    }

}