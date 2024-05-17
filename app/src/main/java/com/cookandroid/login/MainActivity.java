package com.cookandroid.login;

import static android.app.ProgressDialog.show;
import static android.content.ContentValues.TAG;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
//import org.pytorch.TensorImageUtils;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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

            // floatingActionButton 초기화
            floatingActionButton = findViewById(R.id.main_floating_add_btn);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 권한 창 생성
                    TedPermission.create()
                            .setPermissionListener(permissionListner)
                            .setDeniedMessage("거부하셨습니다.")
                            .setRationaleMessage("카메라 권한이 필요합니다.")
                            .setPermissions(android.Manifest.permission.CAMERA)
                            .check();
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

                // 이미지 찍은 후 결과 처리 함수
                ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                //원하는 기능 작성
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    Log.i(TAG,"check");
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
                                    // 이미지 캡쳐 결과 뜬다
                                    // 이미지 갤러리에 저장할 필요가 있다.

                                    // Load and prepare the model
                                    Bitmap bitmap2 = loadBitmap("image.jpg");
                                    if (bitmap2 == null) {return;}
                                    bitmap2 = resizeImage(bitmap2,480,480);
                                    // 비트맵의 크기를 로그로 출력
                                    Module module = loadModel("Efficient-Mobile3.ptl");
                                    if (module == null) {return;}
                                    // Display image on UI
                                    ImageView imageView = findViewById(R.id.main_floating_add_btn);
                                    imageView.setImageBitmap(bitmap2);
                                    // image -> tensor
                                    Tensor inputTensor = prepareInputTensor(bitmap2);
                                    // model predict
                                    predict(module, inputTensor);

                                    // display image
                                    ((ImageView) findViewById(R.id.imgview)).setImageBitmap(rotate(bitmap2, exifDegree));
                                }
                                else{
                                    Log.i(TAG,"not check");
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

                private void predict(Module module, Tensor inputTensor) {
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
                }
                /// image predict to db



            });

        }

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

