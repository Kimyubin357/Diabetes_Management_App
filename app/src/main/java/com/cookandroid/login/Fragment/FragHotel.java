package com.cookandroid.login.Fragment;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.cookandroid.login.MainActivity;
import com.cookandroid.login.ModifyActivity;
import com.cookandroid.login.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FragHotel extends Fragment {
    private View view;
    private FirebaseAuth mAuth;
    private Button logout, member_out;
    private DatabaseReference mDatabaseRef;
    private EditText TextName;
    private EditText TextBirthday;
    private EditText TextHeight;
    private EditText TextWeight;
    private Button save;

    private RadioButton Male, Female;

    private String TAG = "프래그먼트";

    private void showDatePickerDialog() {
        // 현재 날짜를 기본값으로 설정
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Panel,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 사용자가 선택한 날짜를 EditText에 설정
                        // 월 값은 0부터 시작하므로 +1 해줘야 함
                        String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        TextBirthday.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        datePickerDialog.show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_hotel, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(firebaseUser.getUid()).child("UserInformation");//사용자 정보의 접근하기 위해 DatabaseReferance 초기화


        TextName = view.findViewById(R.id.editTextName);
        TextBirthday = view.findViewById(R.id.editTextBirthday);
        TextHeight = view.findViewById(R.id.editTextHeight);
        TextWeight = view.findViewById(R.id.editTextWeight);//xml 버튼 연결

        Male = view.findViewById(R.id.radioButtonMale);
        Female = view.findViewById(R.id.radioButtonFemale);

        save = (Button) view.findViewById(R.id.save);//버튼 연결

        logout = view.findViewById(R.id.logout);//로그아웃 버튼 연결
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "로그아웃 완료.", Toast.LENGTH_SHORT).show();
                mAuth.signOut();

                Intent intent = new Intent(getContext(), MainActivity.class);//로그아웃 이후 메인으로 이동해서 메인이 로그인 여부 검증하고 자동으로 로그인 액티비티 켜줄꺼임 아마?
                startActivity(intent);
            }
        });
        member_out = view.findViewById(R.id.member_out);//회원탈퇴 버튼 연결
        member_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(getContext());//현재 액티비티 명
                dlg.setTitle("정말 탈퇴하시겠습니까?");
                dlg.setMessage("탈퇴를 하시면 그동안 애써 기록한 모든 내용이 삭제되어 복구되지 않아요. 정말 탈퇴하시겠어요?");
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String userid = mAuth.getCurrentUser().getUid();//user의 Id를 string에 저장
                        Log.i("Tag", "First" + userid);
                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {//auth에 현재 사용자를 삭제
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mDatabaseRef.removeValue().addOnCompleteListener(tasks -> {
                                    if (tasks.isSuccessful()) {
                                        Log.i("tag", "Success");
                                        //user Id
                                        Toast.makeText(getContext(), "정상적으로 탈퇴되었습니다.", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Log.i("tag", "Fail");
                                    }
                                });
                            }
                        });
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.show();
            }
        });

        TextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String str_name = snapshot.child("name").getValue(String.class);
                String str_date = snapshot.child("birthDate").getValue(String.class);
                String str_weight = snapshot.child("weight").getValue(String.class);
                String str_height = snapshot.child("height").getValue(String.class);
                String str_gender = snapshot.child("gender").getValue(String.class);

                TextName.setText(str_name);
                TextBirthday.setText(str_date);
                TextWeight.setText(str_weight);
                TextHeight.setText(str_height);

                if (str_gender.equals("남자")) {
                    Male.setChecked(true);
                } else {
                    Female.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "회원정보가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String update_name = TextName.getText().toString();
                String update_date = TextBirthday.getText().toString();
                String update_weight = TextWeight.getText().toString();
                String update_height = TextHeight.getText().toString();
                String update_gender;
                if (Male.isChecked()) {
                    update_gender = "남자";
                } else {
                    update_gender = "여자";
                }

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("name", update_name);
                userUpdates.put("birthDate", update_date);
                userUpdates.put("weight", update_weight);
                userUpdates.put("height", update_height);
                userUpdates.put("gender", update_gender);
                mDatabaseRef.updateChildren(userUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getActivity(), "업데이트 성공", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "업데이트 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return view; // onCreateView 메서드에서 뷰 반환
    }

}
