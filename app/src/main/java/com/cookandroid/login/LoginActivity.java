package com.cookandroid.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText mEt_id, mEt_pwd;
    private Button mEt_login, mEt_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mEt_id = findViewById(R.id.et_id);
        mEt_pwd = findViewById(R.id.et_password);
        mEt_signup = findViewById(R.id.btn_signup);
        mEt_login = findViewById(R.id.btn_login);


        mEt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String str_email = mEt_id.getText().toString();
                String str_pwd = mEt_pwd.getText().toString();
                if(str_email.length()==0||str_pwd.length()==0){
                    Toast.makeText(LoginActivity.this,"로그인 실패",Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.signInWithEmailAndPassword(str_email,str_pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                //로그인 성공
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(LoginActivity.this,"로그인 실패",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        mEt_signup.setOnClickListener(new View.OnClickListener() {//회원가입 버튼 눌렀을 때 이동ㅋ
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }



}