package com.cookandroid.login.Fragment;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cookandroid.login.AlarmActivity;
import com.cookandroid.login.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

interface DataCallback {
    void onDataReady(ArrayList<Entry> dataList);
}

public class FragHome extends Fragment {
    private View view;
    private FloatingActionButton buttonAlarm;
    private Button buttonPreviousDate, buttonNextDate;
    private TextView tvCurrentDate;

    private String TAG = "프래그먼트";
    LineChart lineChart;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private Calendar currentCalendarDate; // 홈에서 선택한 날짜

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_home, container, false);

        // 버튼 찾기
        buttonAlarm = view.findViewById(R.id.button_alarm);
        buttonPreviousDate = view.findViewById(R.id.button_previous_date);
        buttonNextDate = view.findViewById(R.id.button_next_date);
        tvCurrentDate = view.findViewById(R.id.tv_current_date);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserMemos").child(Objects.requireNonNull(firebaseUser).getUid());

        // 클릭 이벤트 리스너 설정
        buttonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다른 액티비티로 이동하는 Intent 생성
                Intent intent = new Intent(getContext(), AlarmActivity.class);
                startActivity(intent);
            }
        });

        // 날짜 버튼 클릭 리스너 설정
        buttonPreviousDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDate(-1);
            }
        });

        buttonNextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDate(1);
            }
        });

        // 초기화
        lineChart = view.findViewById(R.id.day_time_chart);

        // 오늘 날짜로 설정
        currentCalendarDate = Calendar.getInstance();
        updateDisplayedDate();

        return view;
    }

    private void updateDisplayedDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
        SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd(E)", Locale.KOREA);
        String formattedDate = sdf.format(currentCalendarDate.getTime());
        tvCurrentDate.setText(formattedDate);
        fetchDataAndDisplay(dateFormat.format(currentCalendarDate.getTime()));
    }

    private void changeDate(int days) {
        currentCalendarDate.add(Calendar.DAY_OF_YEAR, days);
        updateDisplayedDate();
    }

    private void fetchDataAndDisplay(String clickDate) {
        data1(clickDate, new DataCallback() {
            @Override
            public void onDataReady(ArrayList<Entry> dataList) {
                LineDataSet lineDataSet1 = new LineDataSet(dataList, "하루 혈당");  // dataset 만들기

                // 데이터셋 스타일링
                lineDataSet1.setColor(ContextCompat.getColor(getContext(), R.color.line_color)); // 라인 색상
                lineDataSet1.setValueTextColor(ContextCompat.getColor(getContext(), R.color.value_text_color)); // 값 텍스트 색상
                lineDataSet1.setValueTextSize(16f); // 텍스트 크기 설정
                lineDataSet1.setLineWidth(2f);  // 라인 두께
                lineDataSet1.setCircleColor(ContextCompat.getColor(getContext(), R.color.circle_color));  // 데이터 포인트 색상
                lineDataSet1.setCircleRadius(5f); // 데이터 포인트 반경
                lineDataSet1.setDrawValues(true); // 값 표시 여부

                // 데이터셋 추가
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(lineDataSet1);
                LineData data = new LineData(dataSets); // 라인 데이터에 리스트 추가
                lineChart.setData(data); // 차트에 라인 데이터 추가

                // 추가 차트 설정

                Description description = new Description();
                description.setText("오늘 하루 혈당 수치");
                description.setTextColor(ContextCompat.getColor(getContext(), R.color.chart_description_color));
                description.setTextSize(12f);
                description.setTextAlign(Paint.Align.CENTER);  // 그래프 제목 정렬 설정
                lineChart.setDescription(description);

                Legend legend = lineChart.getLegend();
                legend.setEnabled(true); // 범례 활성화
                legend.setTextColor(ContextCompat.getColor(getContext(), R.color.legend_text_color));
                legend.setTextSize(14f);

                // X축 설정
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 위치를 아래쪽으로 설정
//                xAxis.setGranularity(1f); // x축 간격 설정 (예시: 1)
//                xAxis.setGranularityEnabled(true); // x축 간격 활성화

                // x축 레이블 설정
                int dataSize = dataList.size();
//                String[] labels = new String[]{"공복", "아침 식전", "아침 식후", "점심 식전", "점심 식후", "저녁 식전", "저녁 식후", "자기 전"};

//                String[] labels2 = new String[dataSize];
//                for (int i = 0; i < dataSize; i++) {
//                    Entry temp = dataList.get(i);
//                    Log.i("TAG", labels[(int) temp.getX()]);
//                    labels2[i] = labels[(int) temp.getX()];
//                }
//                Log.i("TAG", labels2.toString());
//                Log.i("TAG", dataList.toString());
                xAxis.setAxisMinimum(0f);  // x축 최소값 설정
                xAxis.setLabelCount(dataSize); // 레이블 개수 설정
//                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels2));

                lineChart.setDrawGridBackground(false); // 그리드 배경 비활성화
                lineChart.setDrawBorders(false); // 차트 경계선 비활성화

                lineChart.invalidate(); // 차트 초기화
            }
        });
    }

    // 데이터 생성
    private ArrayList<Entry> data1(String clickDate, DataCallback callback) {
        Log.i("TAG","CLICK");
        Log.i("TAG",clickDate);
        ArrayList<Entry> dataList = new ArrayList<>();

        // DB에서 해당 날짜 Data 가져오기
        mDatabaseRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    Map<Integer, String> timeBloodSugarMap = new HashMap<Integer, String>();

                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (!Objects.equals(childSnapshot.child("date").getValue(String.class), clickDate)) {
                            continue;  // date 안맞을 시 다음 체크
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
                        Log.i("TAG", "HOHOHO" + bloodSugar);
                        dataList.add(new Entry(time, Integer.parseInt(bloodSugar)));
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



//                xAxis.setAxisMaximum(labels.length - 1f); // x축 최대값 설정
//                xAxis.setLabelCount(3); // 레이블 개수 설정
//                String[] labels = new String[dataSize];
//                xAxis.setValueFormatter(new IndexAxisValueFormatter()); // 레이블 포맷터 설정


//                xAxis.setValueFormatter(new ValueFormatter() {
//                    @Override
//                    public String getAxisLabel(float value, AxisBase axis) {
//                        int index = (int) value;
//                        if (index >= 0 && index < 10) {
//                            return labels[index];
//                        } else {
//                            return "";
//                        }
//                    }
//                });