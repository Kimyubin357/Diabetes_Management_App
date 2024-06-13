package com.cookandroid.login.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cookandroid.login.AlarmActivity;
import com.cookandroid.login.DetailActivity;
import com.cookandroid.login.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
    private TextView detail1, detail2;
    private TextView name1, name2;
    private TextView time1, time2;
    private TextView kcal1, kcal2;
    private ImageView imageView1, imageView2;
    private String TAG = "프래그먼트";
    LineChart lineChart;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private Calendar currentCalendarDate; // 홈에서 선택한 날짜
    private List<String> getImageFilePaths() {
        List<String> imagePaths = new ArrayList<>();
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
//                        Log.i("TAG", file.getAbsolutePath());
                        imagePaths.add(file.getAbsolutePath());
                    }
                }
            }
        } else {
            Log.e("TAG", "Directory not found: " + storageDir);
        }

        // 마지막 두 개의 경로만 반환 -> 최신 파일 두개
        int size = imagePaths.size();
        List<String> lastTwoPaths = new ArrayList<>();

        if (size >= 2) {
            lastTwoPaths = imagePaths.subList(size - 2, size);
        } else {
            lastTwoPaths = new ArrayList<>(imagePaths);
        }

        for (String path : lastTwoPaths) {
            Log.i("TAG","START");
            Log.i("TAG", path);
        }

        return lastTwoPaths;

    }
    private static String extractWordAtPosition(String fileName, int position) {
        // 파일 이름을 언더스코어로 분리
        String[] parts = fileName.split("_");

        // 주어진 위치에 단어가 존재하면 반환, 그렇지 않으면 빈 문자열 반환
        if (position >= 0 && position < parts.length) {
            return parts[position];
        } else {
            return "";
        }
    }

    private Bitmap loadImageFromFile(String imagePath) {
        Bitmap bitmap = null;
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                bitmap = BitmapFactory.decodeFile(imagePath);
            } else {
                Log.e("TAG", "Image file not found: " + imagePath);
            }
        } catch (Exception e) {
            Log.e("TAG", "Error loading image: " + e.getMessage());
        }
        return bitmap;
    }
    private String getImageName(String imagePath){
        return imagePath.substring(imagePath.lastIndexOf('/') + 1);
    }
    public static String convertTimeFormat(String time) {
        // Define the input and output date formats
        SimpleDateFormat inputFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("a hh:mm", Locale.getDefault());

        try {
            // Parse the input time string to a Date object
            Date date = inputFormat.parse(time);

            // Format the Date object to the desired output string
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
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

    public static double convertKcal(Context context, String imageName) throws JSONException {
        // food 상세 정보 mapping
        String jsonData = loadJSONFromAsset(context, "label_mapping_nutrition.json");
        JSONObject jsonObject = new JSONObject(jsonData);
        Double value;
        try{
            JSONObject foodData = jsonObject.getJSONObject(imageName);
            value = foodData.getDouble("cal");
            Log.i("TAG",imageName+value);
        }catch (Exception ex){
            ex.printStackTrace();
            value =0.0;
        }
        return value;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_home, container, false);

        // 상태 바 색상을 빨간색으로 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.red_red_red)); // 빨간색으로 설정
        }

        // 버튼 찾기
        buttonAlarm = view.findViewById(R.id.button_alarm);
        buttonPreviousDate = view.findViewById(R.id.button_previous_date);
        buttonNextDate = view.findViewById(R.id.button_next_date);
        tvCurrentDate = view.findViewById(R.id.tv_current_date);

        imageView1 = view.findViewById(R.id.americano_img); // Select imageView
        imageView2 = view.findViewById(R.id.chocolate_milk_img);

        detail1 = view.findViewById(R.id.americano_detail);
        detail2 = view.findViewById(R.id.chocolate_milk_detail);

        name1 = view.findViewById(R.id.americano);
        name2 = view.findViewById(R.id.chocolate_milk);

        time1 = view.findViewById(R.id.americano_eating_time);
        time2 = view.findViewById(R.id.chocolate_milk_eating_time);

        kcal1 = view.findViewById(R.id.americano_kcal);
        kcal2 = view.findViewById(R.id.chocolate_milk_kcal);


//        List<String> imageList = getImageFilePaths(); // local image 파일들 다 불러오기
//
////            String imagePath = "/storage/emulated/0/Android/data/com.cookandroid.login/files/Pictures/JPEG_20240606_201354_8743862467459105438.jpg";
////            Bitmap bitmap = loadImageFromFile(imagePath); // imageview안에 load image
//
//        // 파일 이름 부분만 추출
//        String imageName1 = getImageName(imageList.get(0));
//        String imageName2 = getImageName(imageList.get(1));
//
//        // 언더스코어로 분리하여 특정 위치의 단어 추출
//        String imageTime1 = extractWordAtPosition(imageName1, 2);
//        imageTime1 = convertTimeFormat(imageTime1);
//        String extractedWord1 = extractWordAtPosition(imageName1, 3);
//
//        String imageTime2 = extractWordAtPosition(imageName2, 2);
//        imageTime2 = convertTimeFormat(imageTime2);
//        String extractedWord2 = extractWordAtPosition(imageName2, 3);
//
//        // 결과 표시
//        Bitmap bitmap = loadImageFromFile(imageList.get(0)); // imageView안에 load image
//        imageView1.setImageBitmap(bitmap); // image1 set
//        Bitmap bitmap2 = loadImageFromFile(imageList.get(1)); // imageView안에 load image
//        imageView2.setImageBitmap(bitmap2); // image2 set
//
//        name1.setText(extractedWord1);
//        name2.setText(extractedWord2);
//        time1.setText(imageTime1);
//        time2.setText(imageTime2);
//        try {
//            Double Kcal1 = convertKcal(getContext(),extractedWord1);
//            Double Kcal2 = convertKcal(getContext(),extractedWord2);
//
//            kcal1.setText(Kcal1 +"kcal");
//            kcal2.setText(Kcal2 +"kcal");
//
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//        // 공통 ClickListener 생성
//        View.OnClickListener detailClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), DetailActivity.class);
//
//                int id = v.getId();
//                if (id == R.id.americano_detail) {
//                    intent.putExtra("foodName", extractedWord1); // detail1에 대한 값 설정
//                    String imagePath1 = imageList.get(0); // 이미지 경로 가져오기
//                    intent.putExtra("imageUri", imagePath1); // detail1에 대한 imageUri 설정
//                } else if (id == R.id.chocolate_milk_detail) {
//                    intent.putExtra("foodName", extractedWord2); // detail2에 대한 값 설정
//                    String imagePath2 = imageList.get(1); // 이미지 경로 가져오기
//                    intent.putExtra("imageUri", imagePath2); // detail2에 대한 imageUri 설정
//                }
//
//                startActivity(intent);
//            }
//        };
//
//
//        // ClickListener를 detail1과 detail2에 설정
//        detail1.setOnClickListener(detailClickListener);
//        detail2.setOnClickListener(detailClickListener);
        List<String> imageList = getImageFilePaths(); // local image 파일들 다 불러오기

        if (imageList.isEmpty()) {
            Log.e("TAG", "No images found");
            // 적절한 대처: 예를 들어, UI에 에러 메시지를 표시하거나 기본 이미지를 설정
            name1.setText("No Image");
            name2.setText("No Image");
            time1.setText("-");
            time2.setText("-");
            kcal1.setText("-");
            kcal2.setText("-");
        } else {
            // imageList에 최소 2개의 이미지가 있는지 확인
            if (imageList.size() < 2) {
                Log.e("TAG", "Not enough images found");
                // 적절한 대처: 예를 들어, 첫 번째 이미지만 표시하고 두 번째는 기본 이미지로 설정
                name1.setText("No Image");
                name2.setText("No Image");
                time1.setText("-");
                time2.setText("-");
                kcal1.setText("-");
                kcal2.setText("-");
            } else {
                // 파일 이름 부분만 추출
                String imageName1 = getImageName(imageList.get(0));
                String imageName2 = getImageName(imageList.get(1));

                // 언더스코어로 분리하여 특정 위치의 단어 추출
                String imageTime1 = extractWordAtPosition(imageName1, 2);
                imageTime1 = convertTimeFormat(imageTime1);
                String extractedWord1 = extractWordAtPosition(imageName1, 3);

                String imageTime2 = extractWordAtPosition(imageName2, 2);
                imageTime2 = convertTimeFormat(imageTime2);
                String extractedWord2 = extractWordAtPosition(imageName2, 3);

                // 결과 표시
                Bitmap bitmap = loadImageFromFile(imageList.get(0)); // imageView안에 load image
                imageView1.setImageBitmap(bitmap); // image1 set
                Bitmap bitmap2 = loadImageFromFile(imageList.get(1)); // imageView안에 load image
                imageView2.setImageBitmap(bitmap2); // image2 set

                name1.setText(extractedWord1);
                name2.setText(extractedWord2);
                time1.setText(imageTime1);
                time2.setText(imageTime2);
                try {
                    Double Kcal1 = convertKcal(getContext(), extractedWord1);
                    Double Kcal2 = convertKcal(getContext(), extractedWord2);

                    kcal1.setText(Kcal1 + "kcal");
                    kcal2.setText(Kcal2 + "kcal");

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // 공통 ClickListener 생성
                View.OnClickListener detailClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), DetailActivity.class);

                        int id = v.getId();
                        if (id == R.id.americano_detail) {
                            intent.putExtra("foodName", extractedWord1); // detail1에 대한 값 설정
                            String imagePath1 = imageList.get(0); // 이미지 경로 가져오기
                            intent.putExtra("imageUri", imagePath1); // detail1에 대한 imageUri 설정
                        } else if (id == R.id.chocolate_milk_detail) {
                            intent.putExtra("foodName", extractedWord2); // detail2에 대한 값 설정
                            String imagePath2 = imageList.get(1); // 이미지 경로 가져오기
                            intent.putExtra("imageUri", imagePath2); // detail2에 대한 imageUri 설정
                        }

                        startActivity(intent);
                    }
                };

                // ClickListener를 detail1과 detail2에 설정
                detail1.setOnClickListener(detailClickListener);
                detail2.setOnClickListener(detailClickListener);
            }
        }


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

