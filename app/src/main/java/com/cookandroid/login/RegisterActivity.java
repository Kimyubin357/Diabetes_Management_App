package com.cookandroid.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private EditText mEt_Email, mEt_pwd, mEt_name, mEt_birthDate, mEt_weight, mEt_height;
    private Button mEt_join, mEt_male, mEt_female;
    private String gender = "";




    private void showDatePickerDialog() {
        // 현재 날짜를 기본값으로 설정
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Panel,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 사용자가 선택한 날짜를 EditText에 설정
                        // 월 값은 0부터 시작하므로 +1 해줘야 함
                        String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        mEt_birthDate.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        datePickerDialog.show();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mEt_name = findViewById(R.id.et_name);//이름 연결
        mEt_birthDate = findViewById(R.id.date);//생년월일 연결

        mEt_Email = findViewById(R.id.et_email);//이메일 연결
        mEt_pwd = findViewById(R.id.et_password);//비밀번호 연결

        mEt_height = findViewById(R.id.height);//키 연결
        mEt_weight = findViewById(R.id.weight);//몸무게 연결


        mEt_join = findViewById(R.id.btn_join);//회원가입 버튼 연결
        mEt_male=findViewById(R.id.male);//남자 버튼 연결
        mEt_female=findViewById(R.id.female);//여자 버튼 연결



        mEt_birthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        mEt_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gender= "남자";
            }
        });
        mEt_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gender="여자";
            }
        });
    //회원가입처리시작
        mEt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_name = mEt_name.getText().toString();//이름 String 가지고 오기
                String str_birthDate = mEt_birthDate.getText().toString();//날짜 String 가지고 오기

                String str_email = mEt_Email.getText().toString();
                String str_pwd = mEt_pwd.getText().toString();

                String str_height = mEt_height.getText().toString();//키 String 가지고 오기
                String str_weight = mEt_weight.getText().toString();//몸무게 String 가지고 오기

                if (str_email.length()==0 || str_pwd.length()==0 || gender.length()==0 || str_name.length()==0 || str_birthDate.length()==0 || str_height.length()==0 || str_weight.length()==0){
                    Toast.makeText(RegisterActivity.this,"회원가입 실패",Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.createUserWithEmailAndPassword(str_email,str_pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                UserAccount account = new UserAccount();
                                account.setIdToken(firebaseUser.getUid());
                                account.setName(str_name);
                                account.setBirthDate(str_birthDate);
                                account.setHeight(str_height);
                                account.setWeight(str_weight);
                                account.setGender(gender);

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