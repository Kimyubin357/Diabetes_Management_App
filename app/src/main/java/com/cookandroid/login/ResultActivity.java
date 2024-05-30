package com.cookandroid.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.cookandroid.login.Fragment.ImagePredict;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
        String imageUriString = getIntent().getStringExtra("imageUri");
        Uri imageUri = Uri.parse(imageUriString);

        // Convert URI to Bitmap
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

            // Display the image and text
            imageView.setImageBitmap(bitmap);
            result_text.setText(foodName);

            // food 상세 정보 mapping
            String jsonData = loadJSONFromAsset(this, "label_mapping_nutrition.json");
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject foodData = jsonObject.getJSONObject(foodName);

            // 텍스트 뷰 연결
            Map<String, TextView> textViews = new HashMap<>();
            textViews.put("g", findViewById(R.id.text_g)); // gram (기준 단위)
            textViews.put("e", findViewById(R.id.text_e)); // energy (에너지)
            textViews.put("cal", findViewById(R.id.text_cal)); // 탄수화물
            textViews.put("sug", findViewById(R.id.text_sug)); // 당류
            textViews.put("fat", findViewById(R.id.text_fat)); // 지질
            textViews.put("pro", findViewById(R.id.text_pro)); // 단백질
            textViews.put("na", findViewById(R.id.text_na)); // 나트륨
            textViews.put("chol", findViewById(R.id.text_chol)); // 콜레스테롤

            setTextViewData(foodData, textViews);
        } catch (Exception e) {
            Log.e("ResultActivity", "Error loading image", e);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMenu();
            }
        });
    }

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return json;
    }

    // 텍스트뷰에 데이터를 설정하는 함수
    private void setTextViewData(JSONObject foodData, Map<String, TextView> textViews) {
        try {
            textViews.get("g").setText("G: " + foodData.getDouble("g"));
            textViews.get("e").setText("E: " + foodData.getDouble("e"));
            textViews.get("cal").setText("Cal: " + foodData.getDouble("cal"));
            textViews.get("sug").setText("Sug: " + foodData.getDouble("sug"));
            textViews.get("fat").setText("Fat: " + foodData.getDouble("fat"));
            textViews.get("pro").setText("Pro: " + foodData.getDouble("pro"));
            textViews.get("na").setText("Na: " + foodData.getDouble("na"));
            textViews.get("chol").setText("Chol: " + foodData.getDouble("chol"));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        userRef.setValue(imageObj).addOnCompleteListener(new OnCompleteListener<Void>() {
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
