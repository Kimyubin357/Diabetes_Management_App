package com.cookandroid.login;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.cookandroid.login.Fragment.FragFriend;
import com.cookandroid.login.Fragment.FragHome;
import com.cookandroid.login.Fragment.FragHotel;
import com.cookandroid.login.Fragment.FragStar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //바텀 네비게이션
    BottomNavigationView bottomNavigationView;

    private FloatingActionButton floatingActionButton;

    private String Tag = "메인";

    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 123; // 원하는 숫자로 설정 가능

    private String imageFilePath;
    private Uri photoUri;

    // 프래그먼트 변수
    Fragment fragment_home;
    Fragment fragment_star;
    Fragment fragment_group;
    Fragment fragment_hotel;


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


    private List<String> getImageFilePaths() {
        List<String> imagePaths = new ArrayList<>();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        Log.i("TAG", file.getAbsolutePath());
                        imagePaths.add(file.getAbsolutePath());
                    }
                }
            }
        } else {
            Log.e("TAG", "Directory not found: " + storageDir);
        }
        return imagePaths;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firebase 인증 인스턴스를 가져온다.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // 현재 로그인한 사용자를 확인한다.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {

            //프래그먼트 생성
            fragment_home = new FragHome();
            fragment_star = new FragStar();
            fragment_group = new FragFriend();
            fragment_hotel = new FragHotel();

            //초기 플래그먼트 설정
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_home).commitAllowingStateLoss();

            //바텀 네비게이션
            bottomNavigationView = findViewById(R.id.bottomNavigationView);

            setCustomLayout(bottomNavigationView.getMenu().findItem(R.id.home), R.layout.bottom_nav_item, "홈");
            setCustomLayout(bottomNavigationView.getMenu().findItem(R.id.star), R.layout.bottom_nav_item, "다이어리");
            setCustomLayout(bottomNavigationView.getMenu().findItem(R.id.group), R.layout.bottom_nav_item, "음식");
            setCustomLayout(bottomNavigationView.getMenu().findItem(R.id.hotel), R.layout.bottom_nav_item, "프로필");

            //리스터 등록
            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Log.i(TAG, "바텀 네비게이션 클릭");

                    int itemId = item.getItemId();
                    if (itemId == R.id.home) {
                        Log.i(TAG, "home 들어옴");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_home).commitAllowingStateLoss();
                        return true;
                    } else if (itemId == R.id.star) {
                        Log.i(TAG, "star 들어옴");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_star).commitAllowingStateLoss();
                        return true;
                    } else if (itemId == R.id.group) {
                        Log.i(TAG, "group 들어옴");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_group).commitAllowingStateLoss();
                        return true;
                    } else if (itemId == R.id.hotel) {
                        Log.i(TAG, "hotel 들어옴");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_hotel).commitAllowingStateLoss();
                        return true;
                    }
                    return true;
                }
            });
///////////////  최근 상세 정보들  ///////////////
            getImageFilePaths(); // local image 파일들 다 불러오기
            ImageView imageView = findViewById(R.id.imgview); // Select imageView
            String imagePath = "/storage/emulated/0/Android/data/com.cookandroid.login/files/Pictures/JPEG_20240606_201354_8743862467459105438.jpg";
            Bitmap bitmap = loadImageFromFile(imagePath); // imageview안에 load image
            /// onclick 할 때
//            Intent intent = new Intent(MainActivity.this, DetailActivity.class); // intent 생성
//            // 정보 저장
//            intent.putExtra("foodName", foodName); // foodName 전달
//            intent.putExtra("imageUri", imageUri.toString()); // imageUri (image path)도 전달
//
//            // ResultActivity 시작
//            startActivity(intent);

            ///////////////  최근 상세 정보들 누르면 상세 정보 activity로 이동하게  ///////////////

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Log.e("TAG", "Failed to load image");
            }
            ////////////////////////CAMERA////////////////////////
            PermissionListener permissionListner = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    Toast.makeText(getApplicationContext(), "권한이 허용됨", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionDenied(List<String> list) {
                    Toast.makeText(getApplicationContext(), "권한이 거부됨", Toast.LENGTH_SHORT).show();
                }
            };
            // 권한 창 생성
            TedPermission.create()
                    .setPermissionListener(permissionListner)
                    .setDeniedMessage("거부하셨습니다.")
                    .setRationaleMessage("카메라 권한이 필요합니다.")
                    .setPermissions(android.Manifest.permission.CAMERA)
                    .check();

            // floatingActionButton 초기화
            floatingActionButton = findViewById(R.id.main_floating_add_btn);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.i(TAG, "camera success!");

                    // 카메라 권한 있는지 확인
                    // 권한이 다 있다면 카메라 띄우기
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra("key", "value"); // 예시로 putExtra를 사용하여 데이터 추가
                    Log.i(TAG, "Intent contents: " + intent.resolveActivity(getPackageManager()));

                    // camera start !!
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try { // 파일 쓰기를 할때는 항상 try catch 문을 적어야함!
                            photoFile = createImageFile();
                            Log.i(TAG, "EXception");
                        } catch (IOException e) {
                        }
                        Log.i(TAG, "EXception");
                        if (photoFile != null) {
                            Log.i(TAG,"photo");
                            photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                            startActivityResult.launch(intent); // 결과 실행
                        }
                    }

                }

                // 이미지 파일 만드는 함수
                private File createImageFile() throws IOException {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "TEST_" + timeStamp + "_";
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File image = File.createTempFile(
                            imageFileName,
                            ".jpg",
                            storageDir
                    );
                    imageFilePath = image.getAbsolutePath();
                    Log.i(Tag,imageFilePath);
                    return image;
                }
                private Uri saveImageToFile(Bitmap bitmap) throws IOException {
                    // 파일 이름 생성
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + "_";
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

                    // Bitmap을 JPEG 파일로 저장
                    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    }

                    // 파일의 URI를 반환
                    return FileProvider.getUriForFile(MainActivity.this, getPackageName(), imageFile);
                }


                // 이미지 찍은 후 결과 처리 함수
                ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            // onActivityResult 내 예측 결과를 처리하는 부분의 끝에 추가

                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    Log.i(TAG, "check");
                                    Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

                                    ExifInterface exif = null;

                                    try {
                                        exif = new ExifInterface(imageFilePath);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    int exifOrientation;
                                    int exifDegree;

                                    if (exif != null) {
                                        exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                        exifDegree = exifOrientationToDegress(exifOrientation);
                                    } else {
                                        exifDegree = 0;
                                    }

                                    // 모델 로드 및 준비
                                    Bitmap bitmap2 = loadBitmap("image.jpg");
                                    if (bitmap2 == null) {
                                        return;
                                    }
                                    bitmap2 = resizeImage(bitmap2, 480, 480);

                                    // 모델 예측
                                    Module module = loadModel("Efficient-Mobile3.ptl");
                                    if (module == null) {
                                        return;
                                    }

                                    Tensor inputTensor = prepareInputTensor(bitmap2);
                                    String foodName = predict(module, inputTensor);
                                    Log.i(TAG, "[predict]: " + foodName);

                                    try {
                                        // Bitmap을 파일로 저장하고 URI를 얻음
                                        Uri imageUri = saveImageToFile(bitmap2);

                                        // ResultActivity 시작
                                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                                        intent.putExtra("foodName", foodName);
                                        intent.putExtra("imageUri", imageUri.toString());
                                        startActivity(intent);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.i(TAG, "not check");
                                    Toast.makeText(getApplicationContext(), "Not Save Picture", Toast.LENGTH_SHORT).show();
                                }
                            }

                        });
                private Bitmap resizeImage(Bitmap bitmap, int imageWidth, int imageHeight){
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, true);
                    return resizedBitmap;
                }

                // 그 외 처리에 필요한 함수들
                private int exifOrientationToDegress(int exifOrientation) {
                    if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        return 90;
                    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        return 180;
                    } else if ((exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)) {
                        return 270;
                    }
                    return 0;
                }

                private Bitmap rotate(Bitmap bitmap, int exifDegree) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(exifDegree);
                    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
                private Bitmap loadBitmap(String fileName) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open(fileName));
                        Log.i(TAG, "Image loaded!");
                        return bitmap;
                    } catch (IOException e) {
                        Log.e("PytorchHelloWorld", "Error reading assets", e);
                        finish();
                        return null;
                    }
                }

                private Module loadModel(String modelPath) {
                    try {
                        String modelFilePath = assetFilePath(MainActivity.this, modelPath);
                        Log.i(TAG, "Model path: " + modelFilePath);
                        Module module = LiteModuleLoader.load(modelFilePath);
                        Log.i(TAG, "Model loaded!");
                        return module;
                    } catch (IOException e) {
                        Log.e("PytorchHelloWorld", "Error loading model", e);
                        finish();
                        return null;
                    }
                }

                private Tensor prepareInputTensor(Bitmap bitmap) {
                    return TensorImageUtils.bitmapToFloat32Tensor(
                            bitmap,
                            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                            TensorImageUtils.TORCHVISION_NORM_STD_RGB,
                            MemoryFormat.CHANNELS_LAST
                    );
                }

                private String predict(Module module, Tensor inputTensor) {
                    // Perform inference
                    Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
                    Log.i(TAG, "Model input success");

                    // Convert tensor to Java array
                    float[] scores = outputTensor.getDataAsFloatArray();
                    Log.i("TAG", Arrays.toString(scores));
                    Log.i("TAG", "Scores length: " + scores.length);

                    // Find the index with the highest score
                    int maxIndex = 0;
                    float maxScore = scores[0];
                    for (int i = 1; i < scores.length; i++) {
                        if (scores[i] > maxScore) {
                            maxScore = scores[i];
                            maxIndex = i;
                        }
                    }
                    Log.i("TAG", "[Predict] Number: " + maxIndex + " Score: " + maxScore);

                    // Get label data and show the prediction result
                    List<String> foodList = getLabelData();
                    Log.i("TAG", "[Predict]: " + foodList.get(maxIndex));
                    Toast.makeText(getApplicationContext(), "[Predict]: " + foodList.get(maxIndex), Toast.LENGTH_SHORT).show();
                    return foodList.get(maxIndex);
                }
                /// image predict to db



            });

        }

    }

    private void setCustomLayout(MenuItem menuItem, int layoutRes, String title) {
        View view = LayoutInflater.from(this).inflate(layoutRes, null);
        ImageView icon = view.findViewById(R.id.icon);
        TextView text = view.findViewById(R.id.title);

        icon.setImageDrawable(menuItem.getIcon());
        text.setText(title);

        menuItem.setActionView(view);
    }


    public List<String> getLabelData(){
        List<String> foodList = new ArrayList<>();
        try {
            InputStream inputStream = getAssets().open("label_data.txt");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                foodList.add(line);
            }
            reader.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        return foodList;
    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            Log.i(TAG, "model load success!!!");
            return file.getAbsolutePath();
        }
        Log.i(TAG, "model load success!!!!");
        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}

