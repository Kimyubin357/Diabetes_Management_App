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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class password_modify_Activity extends AppCompatActivity {

    private EditText modify_password, modify_check;
    private FirebaseAuth mAuth;
    private Button buttonChange;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_modify);

        modify_password = findViewById(R.id.textView3);
        modify_check = findViewById(R.id.textView5);
        buttonChange = findViewById(R.id.btn_change);

        mAuth = FirebaseAuth.getInstance();

        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPass = modify_password.getText().toString().trim();
                String checkPass = modify_check.getText().toString().trim();

                if(TextUtils.isEmpty(newPass)){
                    Toast.makeText(password_modify_Activity.this, "첫번째란에 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(checkPass)){
                    Toast.makeText(password_modify_Activity.this, "두번째란에 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPass.equals(checkPass)) {
                    Toast.makeText(password_modify_Activity.this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                updatePassword(newPass);
            }
        });
    }
    private void updatePassword(String newPass) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.updatePassword(newPass)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(password_modify_Activity.this, "비밀번호 변경 완료", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(password_modify_Activity.this, "비밀번호 변경 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // 사용자가 로그인되지 않은 경우 처리
            Toast.makeText(password_modify_Activity.this, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

}