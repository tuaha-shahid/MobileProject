
        package com.example.medicinetime;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton AddMedicine;
    RecyclerView recyclerView;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    ArrayList<MedicineModel> list;
    MedicineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔵 Ask for Exact Alarm Permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS
                }, 101);
            }
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.mainRoot);
        AddMedicine = findViewById(R.id.add_medicines);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔵 Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }


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

        AddMedicine.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, _add_medicine.class));
        });

        loadMedicines();
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

                        scheduleAlarm(model);
                    }

                    adapter.notifyDataSetChanged();
                });
    }


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

        Intent intent = new Intent(MainActivity.this, MedicineAlarmReceiver.class);
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

