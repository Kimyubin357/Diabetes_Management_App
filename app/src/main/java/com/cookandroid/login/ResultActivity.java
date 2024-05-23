package com.cookandroid.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.login.Fragment.ImagePredict;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ResultActivity extends AppCompatActivity {

    private EditText result_text;
    private Spinner mealSpinner;
    private Button saveButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ImageView imageView = findViewById(R.id.result_image);
        result_text = findViewById(R.id.result_text);
        mealSpinner = findViewById(R.id.mealSpinner);
        saveButton = findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        // Get the data from the Intent
        String foodName = getIntent().getStringExtra("foodName");
        byte[] byteArray = getIntent().getByteArrayExtra("image");

        // Convert byte array back to Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        // Display the image and text
        imageView.setImageBitmap(bitmap);
        result_text.setText(foodName);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMenu();
            }
        });
    }

    private void saveMenu() {
        String meal = mealSpinner.getSelectedItem().toString();
        String menu = result_text.getText().toString();

        if (meal.isEmpty() || menu.isEmpty()) {
            Toast.makeText(ResultActivity.this, "메뉴와 식사 시간을 모두 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        ImagePredict imageObj = new ImagePredict(meal, menu);

        // 해당 사용자 ID 아래에 아침/점심/저녁 메뉴 데이터 저장
        DatabaseReference userRef = mDatabase.child("meals").child(userId).push();
        userRef.setValue(imageObj)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResultActivity.this, "메뉴가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            // 저장 후 MainActivity로 이동
                            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ResultActivity.this, "메뉴를 저장하는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
