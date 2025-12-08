
package com.example.medicinetime;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class _add_medicine extends AppCompatActivity {

    TextInputEditText inputName, inputDosage;
    MaterialButton btnTime, btnStartDate, btnEndDate, btnSave;

    FirebaseFirestore db;
    FirebaseAuth auth;

    // Start Date
    int year = -1, month = -1, day = -1;

    // End Date
    int endYear = -1, endMonth = -1, endDay = -1;

    // Time
    int hour = -1, minute = -1;

    // Edit Mode variables
    boolean isEdit = false;
    String docId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        inputName = findViewById(R.id.inputMedicineName);
        inputDosage = findViewById(R.id.inputDosage);

        btnTime = findViewById(R.id.btnSelectTime);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSave = findViewById(R.id.btnSaveMedicine);

        MaterialToolbar toolbar = findViewById(R.id.topBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // FIX SYSTEM INSETS
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addMedicine), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, 0);
            return insets;
        });

        // ---------------------------------------------
        //      CHECK IF USER IS EDITING MEDICINE
        // ---------------------------------------------
        isEdit = getIntent().getBooleanExtra("edit", false);
        docId = getIntent().getStringExtra("id");

        if (isEdit) {
            loadExistingData();
        }

        // ---------------------------------------------
        //              PICK TIME
        // ---------------------------------------------
        btnTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTitleText("Select Time")
                    .setHour(hour == -1 ? 9 : hour)
                    .setMinute(minute == -1 ? 0 : minute)
                    .build();

            picker.show(getSupportFragmentManager(), "TIME_PICKER");

            picker.addOnPositiveButtonClickListener(view -> {
                hour = picker.getHour();
                minute = picker.getMinute();
                btnTime.setText(String.format("%02d:%02d", hour, minute));
            });
        });

        // ---------------------------------------------
        //          PICK START DATE
        // ---------------------------------------------
        btnStartDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Start Date")
                            .build();

            datePicker.show(getSupportFragmentManager(), "START_DATE");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(selection);

                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH) + 1;
                day = c.get(Calendar.DAY_OF_MONTH);

                btnStartDate.setText(day + "/" + month + "/" + year);
            });
        });

        // ---------------------------------------------
        //            PICK END DATE
        // ---------------------------------------------
        btnEndDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> endPicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select End Date")
                            .build();

            endPicker.show(getSupportFragmentManager(), "END_DATE");

            endPicker.addOnPositiveButtonClickListener(selection -> {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(selection);

                endYear = c.get(Calendar.YEAR);
                endMonth = c.get(Calendar.MONTH) + 1;
                endDay = c.get(Calendar.DAY_OF_MONTH);

                btnEndDate.setText(endDay + "/" + endMonth + "/" + endYear);
            });
        });

        // SAVE / UPDATE BUTTON
        btnSave.setOnClickListener(v -> saveMedicine());
    }

    // ----------------------------------------------------
    //       PREFILL DATA WHEN EDITING
    // ----------------------------------------------------
    private void loadExistingData() {

        inputName.setText(getIntent().getStringExtra("name"));
        inputDosage.setText(getIntent().getStringExtra("dosage"));

        // time
        hour = getIntent().getIntExtra("hour", -1);
        minute = getIntent().getIntExtra("minute", -1);
        if (hour != -1)
            btnTime.setText(String.format("%02d:%02d", hour, minute));

        // start date
        day = getIntent().getIntExtra("startDay", -1);
        month = getIntent().getIntExtra("startMonth", -1);
        year = getIntent().getIntExtra("startYear", -1);
        if (day != -1)
            btnStartDate.setText(day + "/" + month + "/" + year);

        // end date
        endDay = getIntent().getIntExtra("endDay", -1);
        endMonth = getIntent().getIntExtra("endMonth", -1);
        endYear = getIntent().getIntExtra("endYear", -1);
        if (endDay != -1)
            btnEndDate.setText(endDay + "/" + endMonth + "/" + endYear);

        btnSave.setText("Update Medicine");
    }

    // ----------------------------------------------------
    //     SAVE OR UPDATE MEDICINE IN FIRESTORE
    // ----------------------------------------------------
    private void saveMedicine() {

        // VALIDATION
        if (inputName.getText().toString().trim().isEmpty()) {
            inputName.setError("Required");
            return;
        }

        if (inputDosage.getText().toString().trim().isEmpty()) {
            inputDosage.setError("Required");
            return;
        }

        if (hour == -1 || minute == -1) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (day == -1) {
            Toast.makeText(this, "Please select start date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDay == -1) {
            Toast.makeText(this, "Please select end date", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("name", inputName.getText().toString().trim());
        data.put("dosage", inputDosage.getText().toString().trim());
        data.put("startDay", day);
        data.put("startMonth", month);
        data.put("startYear", year);
        data.put("endDay", endDay);
        data.put("endMonth", endMonth);
        data.put("endYear", endYear);
        data.put("hour", hour);
        data.put("minute", minute);

        // -------------------------------------
        //            UPDATE MODE
        // -------------------------------------
        if (isEdit) {
            db.collection("Users")
                    .document(uid)
                    .collection("Medicines")
                    .document(docId)
                    .update(data)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
        // -------------------------------------
        //              ADD MODE
        // -------------------------------------
        else {
            db.collection("Users")
                    .document(uid)
                    .collection("Medicines")
                    .add(data)
                    .addOnSuccessListener(docRef -> {
                        docRef.update("id", docRef.getId());
                        Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}
