package com.example.myapplication;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_PHONE = 1;

    private EditText emergencyContactEditText;
    private Button setEmergencyContactButton;
    private Button emergencyCallButton;

    private String emergencyNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addMedicineReminderButton = findViewById(R.id.btnAddMedicineReminder);
        Button addWaterReminderButton = findViewById(R.id.btnAddWaterReminder);

        addMedicineReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog("Medicine");
            }
        });

        addWaterReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog("Water");
            }
        });


        emergencyContactEditText = findViewById(R.id.etEmergencyContact);
        setEmergencyContactButton = findViewById(R.id.btnSetEmergencyContact);
        emergencyCallButton = findViewById(R.id.btnEmergencyCall);

        // Schedule medicine reminder
        scheduleReminder("Take medicine", 9, 0); // Set the time for the medicine reminder (e.g., 9:00 AM)

// Schedule water intake reminder
        scheduleReminder("Drink water", 12, 0); // Set the time for the water intake reminder (e.g., 12:00 PM)


        setEmergencyContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactNumber = emergencyContactEditText.getText().toString().trim();
                if (contactNumber.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter an emergency contact number", Toast.LENGTH_SHORT).show();
                } else {
                    emergencyNumber = contactNumber;
                    Toast.makeText(MainActivity.this, "Emergency contact number set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emergencyCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emergencyNumber.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please set an emergency contact number", Toast.LENGTH_SHORT).show();
                } else {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
                    } else {
                        callEmergencyNumber();
                    }
                }
            }
        });
    }

    private void showTimePickerDialog(final String reminderType) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
//            @Override
//            public void onTimeSet(TimePicker timePicker, int i, int i1) {
//
//            }

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                scheduleReminder("Take " + reminderType, hourOfDay, minute);
                Toast.makeText(MainActivity.this, "Reminder added for " + reminderType, Toast.LENGTH_SHORT).show();
            }
        }, 0, 0, false);
        timePickerDialog.show();
    }


    private void callEmergencyNumber() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + emergencyNumber));

        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(MainActivity.this, "Unable to make a call", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleReminder(String message, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        Intent intent = new Intent(MainActivity.this, ReminderBroadcastReceiver.class);
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callEmergencyNumber();
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

