package com.example.medicinetime;

public class MedicineModel {

    public String id;
    public String name;
    public String dosage;

    public int hour, minute;

    public int startDay, startMonth, startYear;
    public int endDay, endMonth, endYear;

    public MedicineModel() {
        // Required for Firebase
    }

    public MedicineModel(String id, String name, String dosage,
                         int hour, int minute,
                         int startDay, int startMonth, int startYear,
                         int endDay, int endMonth, int endYear) {

        this.id = id;
        this.name = name;
        this.dosage = dosage;

        this.hour = hour;
        this.minute = minute;

        this.startDay = startDay;
        this.startMonth = startMonth;
        this.startYear = startYear;

        this.endDay = endDay;
        this.endMonth = endMonth;
        this.endYear = endYear;
    }
}
