package com.beamotivator.beam.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beamotivator.beam.R;
import com.beamotivator.beam.models.ModelGroupNames;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdapterGroupNames extends RecyclerView.Adapter<AdapterGroupNames.HolderGroupNames>{

    private Context context;
    private ArrayList<ModelGroupNames> groupNames;

    public AdapterGroupNames(Context context, ArrayList<ModelGroupNames> groupNames) {
        this.context = context;
        this.groupNames = groupNames;
    }

    @NonNull
    @Override
    public HolderGroupNames onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupnames,parent,false);

        return new HolderGroupNames(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupNames holder, int position) {

        ModelGroupNames model = groupNames.get(position);

        final String groupTitle = ""+model.getGroupTitle();



        holder.gNames.setText(groupTitle);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gintent = new Intent("custom-message");
                gintent.putExtra("groupName",groupTitle);
                LocalBroadcastManager.getInstance(context).sendBroadcast(gintent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return groupNames.size();
    }

    static class HolderGroupNames extends RecyclerView.ViewHolder{

        TextView gNames;

        public HolderGroupNames(@NonNull View itemView) {
            super(itemView);
            gNames = itemView.findViewById(R.id.postTypeTv);
        }
    }

}
