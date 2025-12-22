package com.example.medicinetime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    FloatingActionButton addMedicine;
    RecyclerView recyclerView;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    ArrayList<MedicineModel> list;
    MedicineAdapter adapter;

    private boolean exactAlarmChecked = false; // Prevent looping

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        auth = FirebaseAuth.getInstance();

        // Block unauthorized access
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, activity_login.class));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Notification permission (Android 13+)
        requestNotificationPermission();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.menu_contact) {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(android.net.Uri.parse("mailto:medicinetime.app@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Medicine Time App - Support");
                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hello Medicine Time Team,\n\nI need help with...\n\nThanks.");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email using"));
                } catch (Exception e) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            if (item.getItemId() == R.id.menu_logout) {

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MainActivity.this, activity_login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                return true;
            }

            if (item.getItemId() == R.id.nav_add) {
                startActivity(new Intent(MainActivity.this, _add_medicine.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, activity_about_us.class));
                return true;
            }

            return false;
        });

        View root = findViewById(R.id.mainRoot);
        addMedicine = findViewById(R.id.add_medicines);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerMedicine);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new MedicineAdapter(list);
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, 0);
            return insets;
        });

        addMedicine.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, _add_medicine.class))
        );

        loadMedicines();
    }

    /** Request notification permission for Android 13+ **/
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMedicines() {

        String uid = auth.getUid();
        if (uid == null) return;

        firestore.collection("Users")
                .document(uid)
                .collection("Medicines")
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    list.clear();

                    for (DocumentSnapshot snap : value.getDocuments()) {
                        MedicineModel model = snap.toObject(MedicineModel.class);
                        if (model == null) continue;

                        model.id = snap.getId();
                        list.add(model);

                        // Only check exact alarm permission once
                        if (!exactAlarmChecked) {
                            checkAndScheduleAlarm(model);
                            exactAlarmChecked = true;
                        } else {
                            scheduleAlarm(model);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    /** Checks exact alarm permission (Android 12+) before scheduling **/
    private void checkAndScheduleAlarm(MedicineModel model) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
            return;
        }
        scheduleAlarm(model);
    }

    /** Schedule individual alarm **/
    private void scheduleAlarm(MedicineModel model) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, model.startYear);
        calendar.set(Calendar.MONTH, model.startMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, model.startDay);
        calendar.set(Calendar.HOUR_OF_DAY, model.hour);
        calendar.set(Calendar.MINUTE, model.minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, MedicineAlarmReceiver.class);
        intent.putExtra("medicineName", model.name);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                model.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}
