package com.cookandroid.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MemoSave extends AppCompatActivity {

    private static final String TAG = "MemoSave";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private Button et_Date;
    private Button et_Time;
    private NumberPicker et_BloodSugar;
    private EditText et_Memo;
    private Button btn_Save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_star);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        et_Date = findViewById(R.id.et_Date);
        et_Time = findViewById(R.id.et_Time);
        et_BloodSugar = findViewById(R.id.et_BloodSugar);
        et_Memo = findViewById(R.id.et_Memo);
        btn_Save = findViewById(R.id.btn_Save);

        // NumberPicker 설정
        et_BloodSugar.setMinValue(1);
        et_BloodSugar.setMaxValue(999);

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_Date = et_Date.getText().toString();
                String str_Time = et_Time.getText().toString();
                String str_BloodSugar = Integer.toString(et_BloodSugar.getValue());
                String str_Memo = et_Memo.getText().toString();

                if (str_Date.isEmpty() || str_Time.isEmpty() || str_BloodSugar.isEmpty() || str_Memo.isEmpty()) {
                    Toast.makeText(MemoSave.this, "메모 저장 실패: 모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    saveMemo(str_Date, str_Time, str_BloodSugar, str_Memo);
                }
            }
        });
    }

    private void saveMemo(String date, String time, String bloodSugar, String memo) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            UserMemo userMemo = new UserMemo(date, time, bloodSugar, memo);
            DatabaseReference userMemosRef = mDatabaseRef.child("UserMemos").child(firebaseUser.getUid()).push();
            Log.d(TAG, "Database Path: " + userMemosRef.toString());
            userMemosRef.setValue(userMemo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "메모 저장 성공");
                                Toast.makeText(MemoSave.this, "메모 저장 성공", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Log.e(TAG, "메모 저장 실패", task.getException());
                                Toast.makeText(MemoSave.this, "메모 저장 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(MemoSave.this, "로그인 상태를 확인해 주세요", Toast.LENGTH_SHORT).show();
        }
    }
}
