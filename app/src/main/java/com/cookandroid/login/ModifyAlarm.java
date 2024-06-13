package com.cookandroid.login;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ModifyAlarm extends AppCompatActivity {

    private TimePicker timePicker;
    private EditText editText;
    private AlarmManager alarmManager;
    SQLiteDatabase mDb;
    Button regist;
    private int hour, minute;
    public String alarmtime;
    private String text, ampm;
    public PendingIntent pIntent;
    public Intent intent;
    private int alarmId;

    @Override
    public void onBackPressed() {
        // 버튼을 누르면 메인화면으로 이동
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_alarm);

        // 알람 ID 가져오기
        alarmId = getIntent().getIntExtra("alarm_id", -1);

        // 기존 알람 정보 가져오기
        retrieveExistingAlarmInfo();

        timePicker = findViewById(R.id.timepicker);
        editText = findViewById(R.id.editText);
        timePicker.setIs24HourView(false);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        regist = findViewById(R.id.btnset);
        regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 기존 알람 취소
                cancelExistingAlarm();

                if (Build.VERSION.SDK_INT < 23) {
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                } else {
                    hour = timePicker.getHour();
                    minute = timePicker.getMinute();
                }
                text = editText.getText().toString();
                alarmtime = String.valueOf(hour) + minute;

                ampm = (hour >= 12 && hour < 24) ? "오후" : "오전";

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                long intervalTime = 1000 * 60 * 60 * 24; // 24시간
                long currentTime = System.currentTimeMillis();

                if (currentTime > calendar.getTimeInMillis()) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + intervalTime);
                }
                intent = new Intent(getApplicationContext(), AlarmReceiver.class);

                intent.putExtra("id", String.valueOf(alarmId));
                intent.putExtra("drug", text);

                pIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmId, intent, PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalTime, pIntent);

                // 데이터베이스 업데이트
                ContentValues contentValues = new ContentValues();
                contentValues.put(Databases.CreateDB.AMPM, ampm);
                contentValues.put(Databases.CreateDB.HOUR, hour);
                contentValues.put(Databases.CreateDB.MINUTE, minute);
                contentValues.put(Databases.CreateDB.DRUGTEXT, text);
                contentValues.put(Databases.CreateDB.ALARMTIME, alarmtime);

                mDb = AlarmDbHelper.getInstance(getApplicationContext()).getWritableDatabase();
                int rowsUpdated = mDb.update(Databases.CreateDB.TABLE_NAME, contentValues, Databases.CreateDB._ID + "=?", new String[]{String.valueOf(alarmId)});

                if (rowsUpdated > 0) {
                    Toast.makeText(getApplicationContext(), "알림이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "알림 수정에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
                setResult(RESULT_OK);

                // 메인 액티비티로 이동
                Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void retrieveExistingAlarmInfo() {
        mDb = AlarmDbHelper.getInstance(getApplicationContext()).getReadableDatabase();
        Cursor cursor = mDb.query(Databases.CreateDB.TABLE_NAME,
                null,
                Databases.CreateDB._ID + "=?",
                new String[]{String.valueOf(alarmId)},
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            String ampm = cursor.getString(cursor.getColumnIndexOrThrow(Databases.CreateDB.AMPM));
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow(Databases.CreateDB.HOUR));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow(Databases.CreateDB.MINUTE));
            String drug = cursor.getString(cursor.getColumnIndexOrThrow(Databases.CreateDB.DRUGTEXT));

            EditText drugtext = findViewById(R.id.editText);
            drugtext.setText(drug);

            TimePicker timepicker = findViewById(R.id.timepicker);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timepicker.setHour(hour);
                timepicker.setMinute(minute);
            }
            cursor.close();
            Log.i("ModifyAlarm", "알람 정보 로드 성공: alarmId=" + alarmId + ", drug=" + drug);
        } else {
            Log.e("ModifyAlarm", "알람 정보 로드 실패: alarmId=" + alarmId);
        }
    }


    private void cancelExistingAlarm() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmId, intent, PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pIntent);
        }
    }
}