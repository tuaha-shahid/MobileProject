package com.example.medicinetime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.mainRoot);
        AddMedicine = findViewById(R.id.add_medicines);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerMedicine);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new MedicineAdapter(list);
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        AddMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, _add_medicine.class);
            startActivity(intent);
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
                    if (error != null) {
                        Toast.makeText(MainActivity.this, "Error loading medicines", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    list.clear();
                    for (DocumentSnapshot snapshot : value.getDocuments()) {
                        MedicineModel model = snapshot.toObject(MedicineModel.class);
                        model.id = snapshot.getId();
                        list.add(model);

                        setAlarmForMedicine(model);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void setAlarmForMedicine(MedicineModel model) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, model.hour);
        calendar.set(Calendar.MINUTE, model.minute);
        calendar.set(Calendar.SECOND, 0);

        // if time passed, schedule for the next day
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

        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}
