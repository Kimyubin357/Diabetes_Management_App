package com.cookandroid.login.Fragment;

import android.os.Bundle;
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

import com.cookandroid.login.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragFriend extends Fragment {

    private EditText menuEditText;
    private Spinner mealSpinner;
    private Button saveButton;
    private TextView menuDisplayTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_friend, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        menuEditText = view.findViewById(R.id.menuEditText);
        mealSpinner = view.findViewById(R.id.mealSpinner);
        saveButton = view.findViewById(R.id.saveButton);
        menuDisplayTextView = view.findViewById(R.id.menuDisplayTextView);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMenu();
            }
        });

        // 사용자가 저장한 메뉴 출력
        displayMenu();

        return view;
    }

    private void saveMenu() {
        String meal = mealSpinner.getSelectedItem().toString();
        String menu = menuEditText.getText().toString().trim();

        if (meal.isEmpty() || menu.isEmpty()) {
            Toast.makeText(getActivity(), "메뉴와 식사 시간을 모두 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // 현재 날짜 가져오기
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 해당 사용자 ID 아래에 아침/점심/저녁 메뉴 데이터 저장
        DatabaseReference userRef = mDatabase.child("meals").child(userId).child(currentDate).child(meal);
        userRef.setValue(menu)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "메뉴가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            // 저장 후 다시 출력
                            displayMenu();
                        } else {
                            Toast.makeText(getActivity(), "메뉴를 저장하는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void displayMenu() {
        String userId = currentUser.getUid();
        DatabaseReference userRef = mDatabase.child("meals").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder menuDisplay = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    for (DataSnapshot mealSnapshot : dateSnapshot.getChildren()) {
                        String meal = mealSnapshot.getKey();
                        String menu = (String) mealSnapshot.getValue(); // ERROR1
                        if (menu != null) {
                            menuDisplay.append("Date: ").append(date).append("\n");
                            menuDisplay.append("Meal: ").append(meal).append("\n");
                            menuDisplay.append("Menu: ").append(menu).append("\n\n");
                        }
                    }
                }
                menuDisplayTextView.setText(menuDisplay.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "메뉴를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
