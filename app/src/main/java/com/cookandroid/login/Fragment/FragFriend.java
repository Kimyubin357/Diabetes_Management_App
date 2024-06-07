package com.cookandroid.login.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.login.DateAdapter;
import com.cookandroid.login.MenuAdapter;
import com.cookandroid.login.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class FragFriend extends Fragment implements DateAdapter.OnDateClickListener {

    private RecyclerView dateRecyclerView;
    private DateAdapter dateAdapter;
    private List<String> dateList;
    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private List<ImagePredict> menuList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private TextView emptyMenuTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_friend, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        dateRecyclerView = view.findViewById(R.id.dateRecyclerView);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        emptyMenuTextView=view.findViewById(R.id.emptyMenuTextView);

        dateList = generateDateList();

        // 현재 날짜의 인덱스를 구한다.
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());
        int currentIndex = dateList.indexOf(currentDate);

        // 현재 날짜의 인덱스가 리스트의 중앙에 오도록 조정
        dateRecyclerView.scrollToPosition(currentIndex-2);

        dateAdapter = new DateAdapter(dateList, this);
        dateRecyclerView.setAdapter(dateAdapter);

        menuRecyclerView = view.findViewById(R.id.menuRecyclerView);
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        menuList = new ArrayList<>();
        menuAdapter = new MenuAdapter(Collections.unmodifiableList(menuList));
        menuRecyclerView.setAdapter(menuAdapter);


        displayMenu(currentDate);

        return view;
    }
    private List<String> generateDateList() {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

        // 이전 달의 날짜 추가
        calendar.add(Calendar.MONTH, -1);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 이전 달의 마지막 날짜
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 이전 달의 첫 날로 설정
        for (int i = 1; i <= maxDay; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 현재 달의 날짜 추가
        calendar = Calendar.getInstance(); // 달을 현재 달로 다시 설정
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 현재 달의 마지막 날짜
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 현재 달의 첫 날로 설정
        for (int i = 1; i <= maxDay; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 다음 달의 날짜 추가
        calendar.add(Calendar.MONTH, 1);
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 다음 달의 마지막 날짜
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 다음 달의 첫 날로 설정
        for (int i = 1; i <= maxDay; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }


    @Override
    public void onDateClick(int position) {
        String selectedDate = dateList.get(position);
        Toast.makeText(getActivity(), "Selected date: " + selectedDate, Toast.LENGTH_SHORT).show();
        displayMenu(selectedDate);
    }
    private void displayMenu(String selectedDate) {
        String userId = currentUser.getUid();
        DatabaseReference mealsRef = mDatabase.child("meals").child(userId);
        DatabaseReference memosRef = mDatabase.child("UserMemos").child(userId);

        // 리스트 초기화
        menuList.clear();

        // 비동기 요청을 위한 CountDownLatch
        CountDownLatch latch = new CountDownLatch(2);

        // meals 데이터 가져오기
        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ImagePredict imagePredict = snapshot.getValue(ImagePredict.class);
                    if (imagePredict != null && imagePredict.getDateTime().contains(selectedDate)) {
                        menuList.add(imagePredict);
                    }
                }
                latch.countDown();  // 비동기 요청 완료
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                latch.countDown();  // 비동기 요청 실패
            }
        });

        // UserMemos 데이터 가져오기
        memosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // 직접 데이터베이스에서 값을 가져와서 대입
                    String date = snapshot.child("date").getValue(String.class);
                    String bloodSugar = snapshot.child("bloodSugar").getValue(String.class);
                    String memo = snapshot.child("memo").getValue(String.class);
                    String realTime = snapshot.child("realTime").getValue(String.class);

                    if (bloodSugar != null && memo != null && realTime != null) {
                        UserMemos userMemos = new UserMemos(date, bloodSugar, memo, realTime);
                        if (userMemos.getRealTime().contains(selectedDate)) {
                            // UserMemos의 데이터 중에서 시간을 비교하여 선택된 날짜의 데이터를 가져옴
                            menuList.add(userMemos); // 메뉴 리스트에 추가
                        }
                    }
                }
                latch.countDown();  // 비동기 요청 완료
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                latch.countDown();  // 비동기 요청 실패
            }
        });

        // 데이터를 모두 가져온 후 처리
        new Thread(() -> {
            try {
                latch.await();  // 모든 비동기 요청이 완료될 때까지 대기

                // 시간 순서대로 정렬
                Collections.sort(menuList, new Comparator<ImagePredict>() {
                    @Override
                    public int compare(ImagePredict o1, ImagePredict o2) {
                        return o1.getDateTime().compareTo(o2.getDateTime());
                    }
                });

                // UI 스레드에서 UI 업데이트
                Activity activity = getActivity(); // Fragment에서 호출하는 경우
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        menuAdapter.notifyDataSetChanged();

                        // 데이터가 비어있을 때 메뉴가 없음을 표시
                        if (menuList.isEmpty()) {
                            emptyMenuTextView.setVisibility(View.VISIBLE);
                        } else {
                            emptyMenuTextView.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}