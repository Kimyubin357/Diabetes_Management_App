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

public class FragHome extends Fragment {
    private View view;
    private Button buttonAlarm;

    private String TAG = "프래그먼트";

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

        return view;
    }
}
