package com.cookandroid.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ModifyActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private Button logout,member_out;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        mAuth = FirebaseAuth.getInstance();

        logout = findViewById(R.id.logout);//로그아웃 버튼 연결
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ModifyActivity.this,"로그아웃 완료.",Toast.LENGTH_SHORT).show();
                mAuth.signOut();

                Intent intent = new Intent(ModifyActivity.this,MainActivity.class);//로그아웃 이후 메인으로 이동해서 메인이 로그인 여부 검증하고 자동으로 로그인 액티비티 켜줄꺼임 아마?
                startActivity(intent);
                finish();
            }
        });
        member_out = findViewById(R.id.member_out);//회원탈퇴 버튼 연결
        member_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(ModifyActivity.this);//현재 액티비티 명
                dlg.setTitle("정말 탈퇴하시겠습니까?");
                dlg.setMessage("탈퇴를 하시면 그동안 애써 기록한 모든 내용이 삭제되어 복구되지 않아요. 정말 탈퇴하시겠어요?");
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ModifyActivity.this,"정상적으로 탈퇴되었습니다.",Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ModifyActivity.this,MainActivity.class);
                                startActivity(intent);
                            }
                        });

                        finish();
                    }
                });
                dlg.setNegativeButton("취소",null);
                dlg.show();
            }
        });


    }
}