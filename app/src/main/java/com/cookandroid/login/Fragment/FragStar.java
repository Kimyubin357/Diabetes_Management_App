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

import androidx.fragment.app.FragmentManager; //
import androidx.fragment.app.FragmentTransaction; //

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
    private TextView textViewDateLabel1, textViewDateLabel2; // +d_1
    private Button buttonSelectDate, buttonChoose, buttonSave, buttonCancel; // +d_2 //
    private DatePickerDialog datePickerDialog; // +b_1
    //    private RadioGroup radioGroupMealTime; +e_4 // +b_2
//    private TimePickerDialog timePickerDialog;
    private NumberPicker numberPicker;
    private Calendar calendar;
    private EditText editTextNote;

    private final String[] timeOptions = {
            " 공복 ", " 아침 식전 ", " 아침 식후 ", " 점심 식전 ", " 점심 식후 ", " 저녁 식전 ", " 저녁 식후 ", " 자기 전 "
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 레이아웃 파일을 인플레이트합니다.
        View view = inflater.inflate(R.layout.frag_star, container, false);

        // UI 컴포넌트를 초기화합니다.
        textViewDateLabel1 = view.findViewById(R.id.textViewDateLabel1);
        textViewDateLabel2 = view.findViewById(R.id.textViewDateLabel2);
        buttonSelectDate = view.findViewById(R.id.buttonSelectDate);
        buttonChoose = view.findViewById(R.id.buttonChoose);
        buttonSave = view.findViewById(R.id.buttonSave);
//        radioGroupMealTime = view.findViewById(R.id.radioGroupMealTime); +e_5
//      buttonCancel = view.findViewById(R.id.buttonCancel);
        numberPicker = view.findViewById(R.id.numberPicker);
        editTextNote = view.findViewById(R.id.editTextNote);

        // 달력 인스턴스를 초기화하고, NumberPicker를 설정합니다.
        calendar = Calendar.getInstance();
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(999); // (del)예시: 혈당 최대값 설정
        numberPicker.setValue(100); // (del)예시: 초기값 설정

        // 초기 날짜를 레이블에 설정합니다.
        updateDateLabel();
        updateTimeLabel();

        // 날짜 선택 다이얼로그를 설정합니다.
        buttonSelectDate.setOnClickListener(v -> {
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
        buttonChoose.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            RadioButton selectedMealTimeButton = view.findViewById(radioGroupMealTime.getCheckedRadioButtonId()); +e_6 // +b_3정도
//            String mealTime = selectedMealTimeButton.getText().toString(); +e_7
//            timePickerDialog = new TimePickerDialog(getActivity(), (view1, hourOfDay, minute) -> {
//              calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                calendar.set(Calendar.MINUTE, minute);
//                updateTimeLabel();
            //*******시간 선택 로직 추가
            //*******예: 시간 선택 다이얼로그 표시
//                    calendar.get(Calendar.HOUR_OF_DAY),
//                    calendar.get(Calendar.MINUTE),
//                    true);

//            timePickerDialog.show();
            builder.setTitle("시간 선택");
            builder.setItems(timeOptions, (dialog, which) -> {
                textViewDateLabel2.setText("지금은"+timeOptions[which]);
            });
            builder.show();
        });

        // 저장 버튼 클릭 이벤트를 처리합니다.
        buttonSave.setOnClickListener(v -> {
            String date = textViewDateLabel1.getText().toString();
            String time = textViewDateLabel2.getText().toString();
            int number = numberPicker.getValue();
            String note = editTextNote.getText().toString(); // 메모장 내용 가져오기

            String message = String.format("날짜: %s, 시간: %s, 혈당 측정값: %d, 메모: %s", date, time, number, note);
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        });

/*      buttonCancel.setOnClickListener(v -> {
            // FragHome으로 이동
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new FragHome()); // R.id.fragment_container는 실제 프래그먼트 컨테이너 ID로 대체해야 함
            fragmentTransaction.addToBackStack(null); // 필요하다면 백스택에 추가해야 함
            fragmentTransaction.commit();
        });
*/

        return view;
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