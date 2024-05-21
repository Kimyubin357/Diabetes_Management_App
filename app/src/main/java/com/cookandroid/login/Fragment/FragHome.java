package com.cookandroid.login.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cookandroid.login.AlarmActivity;
import com.cookandroid.login.MainActivity;
import com.cookandroid.login.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class FragHome extends Fragment {
    private View view;
    private Button buttonAlarm;

    private String TAG = "프래그먼트";
    LineChart lineChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_home, container, false);
        // 버튼 찾기
        buttonAlarm = view.findViewById(R.id.button_alarm);

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
        LineDataSet lineDataSet1 = new LineDataSet(data1(), "Data Set1"); // dataset 만들기
        ArrayList<ILineDataSet> dataSets = new ArrayList<>(); // 데이터셋 추가
        dataSets.add(lineDataSet1);
        LineData data = new LineData(dataSets); // 라인 데이터에 리스트 추가
        lineChart.setData(data); // 차트에 라인 데이터 추가
        lineChart.invalidate(); // 차트 초기화
        return view;
    }
    // 데이터 생성
    private ArrayList<Entry> data1(){
        ArrayList<Entry> dataList = new ArrayList<>();
        dataList.add(new Entry(0,10));
        dataList.add(new Entry(0,10));
        dataList.add(new Entry(1,20));
        dataList.add(new Entry(2,30));
        dataList.add(new Entry(3,40));
        return dataList;
    }
}
