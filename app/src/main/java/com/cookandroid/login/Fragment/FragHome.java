package com.cookandroid.login.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cookandroid.login.AlarmActivity;
import com.cookandroid.login.MainActivity;
import com.cookandroid.login.ModifyActivity;
import com.cookandroid.login.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

interface DataCallback {
    void onDataReady(ArrayList<Entry> dataList);
}
public class FragHome extends Fragment {
    private View view;
    private FloatingActionButton buttonAlarm;

    private String TAG = "프래그먼트";
    LineChart lineChart;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private String clickDate; // 홈에서 선택한 날짜


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_home, container, false);
        // 버튼 찾기
        buttonAlarm = view.findViewById(R.id.button_alarm);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserMemos").child(firebaseUser.getUid());

        // 클릭 이벤트 리스너 설정
        buttonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다른 액티비티로 이동하는 Intent 생성
                Intent intent = new Intent(getContext(), AlarmActivity.class);
                startActivity(intent);
            }
        });

        lineChart = view.findViewById(R.id.day_time_chart); // 초기화

        clickDate = "2024년 06월 02일"; // 선택 날짜 임의로 지정 -> 추후에 홈에 바뀌는 날짜 값을 가져오는 것으로 변경!!!!

        data1(clickDate, new DataCallback() {
            @Override
            public void onDataReady(ArrayList<Entry> dataList) {
                LineDataSet lineDataSet1 = new LineDataSet(dataList, "하루 혈당"); // dataset 만들기
                ArrayList<ILineDataSet> dataSets = new ArrayList<>(); // 데이터셋 추가
                dataSets.add(lineDataSet1);
                LineData data = new LineData(dataSets); // 라인 데이터에 리스트 추가
                lineChart.setData(data); // 차트에 라인 데이터 추가
                lineChart.invalidate(); // 차트 초기화
            }
        });

        return view;
    }
    // 데이터 생성
    private ArrayList<Entry> data1(String clickDate, DataCallback callback){
        ArrayList<Entry> dataList = new ArrayList<>();

        // DB에서 해당 날짜 Data 가져오기
        mDatabaseRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    Map<Integer, String> timeBloodSugarMap = new HashMap<Integer, String>();

                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (!Objects.equals(childSnapshot.child("date").getValue(String.class), clickDate)){
                            continue; // date 안맞을 시 다음 체크
                        }
                        String bloodSugar = childSnapshot.child("bloodSugar").getValue(String.class);
                        String time = childSnapshot.child("time").getValue(String.class);
                        if (bloodSugar != null && time != null) {
                            timeBloodSugarMap.put(mapTimeToIndex(time), bloodSugar);
                        }
                    }

                    // time 값을 정렬 -> 시간 순서에 맞기?
                    List<Integer> sortedTimes = new ArrayList<>(timeBloodSugarMap.keySet());
                    Collections.sort(sortedTimes);

                    // 정렬된 time 값에 맞는 bloodSugar 값을 추출
                    for (int i = 0; i < sortedTimes.size(); i++) {
                        Integer time = sortedTimes.get(i);
                        String bloodSugar = timeBloodSugarMap.get(time);
                        dataList.add(new Entry(i+10,Integer.parseInt(bloodSugar)));

                    }
                    // 데이터가 준비되었음을 콜백을 통해 알림 -> 비동기
                    callback.onDataReady(dataList);
                } else {
                    Log.e("FirebaseData", "Error getting data", task.getException());
                }
            }
        });
        return dataList;
    }

    public static int mapTimeToIndex(String time) {
        switch (time) {
            case " 공복 ":
                return 0;
            case " 아침 식전 ":
                return 1;
            case " 아침 식후 ":
                return 2;
            case " 점심 식전 ":
                return 3;
            case " 점심 식후 ":
                return 4;
            case " 저녁 식전 ":
                return 5;
            case " 저녁 식후 ":
                return 6;
            case " 자기 전 ":
                return 7;
            default:
                throw new IllegalArgumentException("잘못된 시간 값입니다: " + time);
        }
    }

}
