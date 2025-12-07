package com.example.medicinetime;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class _add_medicine extends AppCompatActivity {

    TextInputEditText inputName, inputDosage;
    MaterialButton btnTime, btnStart, btnEnd, btnSave;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String selectedTime = "", startDate = "", endDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Views
        inputName = findViewById(R.id.inputMedicineName);
        inputDosage = findViewById(R.id.inputDosage);
        btnTime = findViewById(R.id.btnSelectTime);
        btnStart = findViewById(R.id.btnStartDate);
        btnEnd = findViewById(R.id.btnEndDate);
        btnSave = findViewById(R.id.btnSaveMedicine);

        MaterialToolbar toolbar = findViewById(R.id.topBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Status bar padding fix
        View root = findViewById(R.id.addMedicine);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // TIME Picker
        btnTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setHour(9)
                    .setMinute(0)
                    .setTitleText("Select Reminder Time")
                    .build();

            picker.show(getSupportFragmentManager(), "TIME_PICKER");

            picker.addOnPositiveButtonClickListener(view -> {
                selectedTime = picker.getHour() + ":" + picker.getMinute();
                btnTime.setText(selectedTime);
            });
        });

        // START DATE PICKER
        btnStart.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Start Date")
                            .build();

            datePicker.show(getSupportFragmentManager(), "START_DATE");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                startDate = datePicker.getHeaderText();
                btnStart.setText(startDate);
            });
        });

        // END DATE PICKER
        btnEnd.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select End Date")
                            .build();

            datePicker.show(getSupportFragmentManager(), "END_DATE");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                endDate = datePicker.getHeaderText();
                btnEnd.setText(endDate);
            });
        });

        // SAVE MEDICINE
        btnSave.setOnClickListener(v -> saveMedicine());
    }

    private void saveMedicine() {

        String name = inputName.getText().toString().trim();
        String dosage = inputDosage.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            inputName.setError("Required");
            return;
        }
        if (dosage.isEmpty()) {
            inputDosage.setError("Required");
            return;
        }
        if (selectedTime.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current logged-in user UID
        String userId = auth.getCurrentUser().getUid();

        // Create data map
        Map<String, Object> medicine = new HashMap<>();
        medicine.put("name", name);
        medicine.put("dosage", dosage);
        medicine.put("time", selectedTime);
        medicine.put("startDate", startDate);
        medicine.put("endDate", endDate);

        // Save in Firestore inside user's folder
        db.collection("Medicines")
                .document(userId)
                .collection("UserMedicines")
                .add(medicine)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Medicine Saved Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
