package com.cookandroid.login.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
// import androidx.appcompat.app.AlertDialog; +h_1 // +g_2

import com.cookandroid.login.R;
import com.cookandroid.login.UserMemo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.Button; // +a_1
// import android.widget.EditText; +e_1 // +a_2
import android.widget.EditText;
import android.widget.NumberPicker; // +a_3
import android.widget.Toast; // +a_4
//import android.widget.DatePickerDialog; +d_3 // +b_1
// import android.widget.RadioGroup; +e_2 // +b_2
// import android.widget.RadioButton; +e_3 // +b_3

import android.widget.TextView; // +c_1

import java.util.Calendar;

public class FragStar extends Fragment {
    private static final String TAG = "MemoSave";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private TextView textViewDateLabel1, textViewDateLabel2; // +d_1
    private Button et_Date, et_Time, btn_Save, buttonCancel; // +d_2 //
    private DatePickerDialog datePickerDialog; // +b_1
    //    private RadioGroup radioGroupMealTime; +e_4 // +b_2
//    private TimePickerDialog timePickerDialog;
    private NumberPicker et_BloodSugar;
    private Calendar calendar;
    private EditText et_Memo;

    private final String[] timeOptions = {
            " 공복 ", " 아침 식전 ", " 아침 식후 ", " 점심 식전 ", " 점심 식후 ", " 저녁 식전 ", " 저녁 식후 ", " 자기 전 "
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 레이아웃 파일을 인플레이트합니다.
        View view = inflater.inflate(R.layout.frag_star, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // UI 컴포넌트를 초기화합니다.
        textViewDateLabel1 = view.findViewById(R.id.textViewDateLabel1);
        textViewDateLabel2 = view.findViewById(R.id.textViewDateLabel2);
        et_Date = view.findViewById(R.id.et_Date);
        et_Time = view.findViewById(R.id.et_Time);
        btn_Save = view.findViewById(R.id.btn_Save);
        et_BloodSugar = view.findViewById(R.id.et_BloodSugar);
        et_Memo = view.findViewById(R.id.et_Memo);

        // 달력 인스턴스를 초기화하고, NumberPicker를 설정합니다.
        calendar = Calendar.getInstance();
        et_BloodSugar.setMinValue(1);
        et_BloodSugar.setMaxValue(999); // (del)예시: 혈당 최대값 설정
        et_BloodSugar.setValue(100); // (del)예시: 초기값 설정

        // 초기 날짜를 레이블에 설정합니다.
        updateDateLabel();
        updateTimeLabel();

        // 날짜 선택 다이얼로그를 설정합니다.
        et_Date.setOnClickListener(v -> {
            datePickerDialog = new DatePickerDialog(getActivity(), (view1, year, monthOfYear, dayOfMonth) -> {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel(); // 날짜 레이블 업데이트 +e_0
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show(); // 다이얼로그를 화면에 표시 +e_0
        });

        // 시간 선택 버튼 이벤트를 처리합니다.
        et_Time.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("시간 선택");
            builder.setItems(timeOptions, (dialog, which) -> {
                textViewDateLabel2.setText("지금은"+timeOptions[which]);
            });
            builder.show();
        });

        // 저장 버튼 클릭 이벤트를 처리합니다.
        btn_Save.setOnClickListener(v -> {
            String str_Date = et_Date.getText().toString();
            String str_Time = et_Time.getText().toString();
            String str_BloodSugar = Integer.toString(et_BloodSugar.getValue());
            String str_Memo = et_Memo.getText().toString();
            Log.i("TAG","ASDASFASfasfasfasfasf");
            if (str_Date.isEmpty() || str_Time.isEmpty() || str_BloodSugar.isEmpty() || str_Memo.isEmpty()) {
                Toast.makeText(getActivity(), "메모 저장 실패: 모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("TAG","into savememo");
                saveMemo(str_Date, str_Time, str_BloodSugar, str_Memo);
            }
        });
        return view;
    }
    private void saveMemo(String date, String time, String bloodSugar, String memo) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            UserMemo userMemo = new UserMemo(date, time, bloodSugar, memo);
            DatabaseReference userMemosRef = mDatabaseRef.child("UserMemos").child(firebaseUser.getUid()).push();
            Log.i("TAG", "Database Path: " + userMemosRef.toString());
            userMemosRef.setValue(userMemo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "메모 저장 성공");
                                Toast.makeText(getActivity(), "메모 저장 성공", Toast.LENGTH_SHORT).show();
                                // Save 되면 어디로 갈지?? 정하기
                            } else {
                                Log.e(TAG, "메모 저장 실패", task.getException());
                                Toast.makeText(getActivity(), "메모 저장 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // 날짜 레이블을 업데이트하는 메서드입니다.
    private void updateDateLabel() {
        String formattedDate = String.format(
                "%d년 %02d월 %02d일",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1, //Java에서 1월은 0으로 시작!
                calendar.get(Calendar.DAY_OF_MONTH));
        textViewDateLabel1.setText("오늘은 " + formattedDate);
    }

    private void updateTimeLabel() {
        String formattedTime = String.format(
                "%02d시 %02d분",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
        textViewDateLabel2.setText("지금은 " + formattedTime);
    }
}