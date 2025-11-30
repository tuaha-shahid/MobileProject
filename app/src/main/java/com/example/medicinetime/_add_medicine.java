package com.example.medicinetime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;

public class _add_medicine extends AppCompatActivity {

    Button AddMedicine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_medicine);
//        AddMedicine = findViewById(R.id.top_app_bar);

        MaterialButton btnTime = findViewById(R.id.btnSelectTime);
        MaterialButton btnStart = findViewById(R.id.btnStartDate);
        MaterialButton btnEnd = findViewById(R.id.btnEndDate);
        MaterialButton btnSave = findViewById(R.id.btnSaveMedicine);

        // TIME PICKER
        btnTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setHour(9)
                    .setMinute(0)
                    .setTitleText("Select Reminder Time")
                    .build();

            picker.show(getSupportFragmentManager(), "TIME_PICKER");

            picker.addOnPositiveButtonClickListener(view -> {
                String selectedTime = picker.getHour() + ":" + picker.getMinute();
                btnTime.setText(selectedTime);
            });
        });

        // DATE PICKER (Start Date)
        btnStart.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Start Date")
                            .build();

            datePicker.show(getSupportFragmentManager(), "START_DATE");

            datePicker.addOnPositiveButtonClickListener(selection ->
                    btnStart.setText(datePicker.getHeaderText()));
        });

        // DATE PICKER (End Date)
        btnEnd.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select End Date")
                            .build();

            datePicker.show(getSupportFragmentManager(), "END_DATE");

            datePicker.addOnPositiveButtonClickListener(selection ->
                    btnEnd.setText(datePicker.getHeaderText()));
        });
//        AddMedicine.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(_add_medicine.this , MainActivity.class);
//                startActivity(intent);
//            }
//        });
    }

}