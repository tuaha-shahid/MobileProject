
package com.example.medicinetime;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    ArrayList<MedicineModel> list;

    public MedicineAdapter(ArrayList<MedicineModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MedicineModel model = list.get(holder.getAdapterPosition());

        holder.medName.setText(model.name);
        holder.medDosage.setText("Dosage: " + model.dosage);
        holder.medTime.setText(String.format("%02d:%02d", model.hour, model.minute));
        holder.medDates.setText("Start: " + model.startDay + "/" + model.startMonth + "/" + model.startYear +
                " | End: " + model.endDay + "/" + model.endMonth + "/" + model.endYear);

        // EDIT
        holder.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), _add_medicine.class);
            i.putExtra("edit", true);
            i.putExtra("id", model.id);
            i.putExtra("name", model.name);
            i.putExtra("dosage", model.dosage);
            i.putExtra("hour", model.hour);
            i.putExtra("minute", model.minute);
            i.putExtra("startDay", model.startDay);
            i.putExtra("startMonth", model.startMonth);
            i.putExtra("startYear", model.startYear);
            i.putExtra("endDay", model.endDay);
            i.putExtra("endMonth", model.endMonth);
            i.putExtra("endYear", model.endYear);
            v.getContext().startActivity(i);
        });

        // DELETE – Only Firestore, NO adapter remove
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            MedicineModel item = list.get(pos);

            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection("Medicines")
                    .document(item.id)
                    .delete();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView medName, medDosage, medTime, medDates;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            medName = itemView.findViewById(R.id.medName);
            medDosage = itemView.findViewById(R.id.medDosage);
            medTime = itemView.findViewById(R.id.medTime);
            medDates = itemView.findViewById(R.id.medDates);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}