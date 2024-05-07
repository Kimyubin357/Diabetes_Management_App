package com.cookandroid.login;

import static android.content.ContentValues.TAG;

import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //바텀 네비게이션
    BottomNavigationView bottomNavigationView;



    private FloatingActionButton floatingActionButton;

    private String Tag = "메인";

    private static final int REQUEST_IMAGE_CAPTURE = 672;
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
            startActivity(intent);
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

            // 권한 창 생성
            TedPermission.create()
                    .setPermissionListener(permissionListner)
                    .setDeniedMessage("거부하셨습니다.")
                    .setRationaleMessage("카메라 권한이 필요합니다.")
                    .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
                    .check();

            // floatingActionButton 초기화
            floatingActionButton = findViewById(R.id.main_floating_add_btn);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
//                startActivity(intent);

                    // 카메라 권한 있는지 확인


                    // 권한이 다 있다면 카메라 띄우기
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try { // 파일 쓰기를 할때는 항상 try catch 문을 적어야함 !
                            photoFile = createImageFile();
                        } catch (IOException e) {
                        }
                        if (photoFile != null) {
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
                                    // 이미지 캡쳐 결과 뜨게 하기
                                    // 이미지 뜨는 창이나 그런 곳에 나타나게 해야할 듯
                                    ((ImageView) findViewById(R.id.imgview)).setImageBitmap(rotate(bitmap, exifDegree));
                                }
                            }
                        });

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
            });

        }

    }


}
