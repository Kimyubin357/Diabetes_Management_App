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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText mEt_Email, mEt_pwd;
    private Button mEt_join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mEt_Email = findViewById(R.id.et_email);
        mEt_pwd = findViewById(R.id.et_password);
        mEt_join = findViewById(R.id.btn_join);
        //회원가입처리시작
        mEt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_email = mEt_Email.getText().toString();
                String str_pwd = mEt_pwd.getText().toString();

                if (str_email.length()==0 || str_pwd.length()==0){
                    Toast.makeText(RegisterActivity.this,"회원가입 실패",Toast.LENGTH_SHORT).show();
                }
                else{
                    mFirebaseAuth.createUserWithEmailAndPassword(str_email,str_pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                                UserAccount account = new UserAccount();
                                account.setIdToken(firebaseUser.getUid());
                                account.setEmailId(firebaseUser.getEmail());
                                account.setPassword(str_pwd);

                                mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                                Toast.makeText(RegisterActivity.this,"회원가입 성공",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(RegisterActivity.this,"회원가입 실패",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}