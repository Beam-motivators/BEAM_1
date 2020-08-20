package com.beamotivator.beam.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beamotivator.beam.EachGroup;
import com.beamotivator.beam.R;
import com.beamotivator.beam.models.ModelMyGroups;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.logging.LoggingPermission;

public class AdapterMyGroups extends RecyclerView.Adapter<AdapterMyGroups.GroupHolder>{

    private Context context;
    private List<ModelMyGroups> groupsList;

    public AdapterMyGroups(Context context, List<ModelMyGroups> groupsList) {
        this.context = context;
        this.groupsList = groupsList;
    }

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_groups,parent,false);

        return new GroupHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
        //get data
        ModelMyGroups model = groupsList.get(position);

        final String gTitle = model.getGroupTitle();
        String gDescription = model.getGroupDescription();
        String gImage = model.getGroupIcon();
        final String groupId = model.getGroupId();


        holder.groupTitle.setText(gTitle);
        holder.groupDescription.setText(gDescription);

        try {
            Glide.with(context)
                    .load(gImage)
                    .placeholder(R.drawable.ic_image_black)
                    .into(holder.groupImage);
        }
        catch (Exception e){
            holder.groupImage.setImageResource(R.drawable.ic_image_white_chat);
        }

        holder.openGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openIntent = new Intent(context, EachGroup.class);
                openIntent.putExtra("groupTitle",gTitle);
                openIntent.putExtra("groupId",groupId);
                context.startActivity(openIntent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    static class GroupHolder extends RecyclerView.ViewHolder{

        ImageView groupImage;
        TextView groupTitle, groupDescription;
        Button openGroup;

        public GroupHolder(@NonNull View itemView) {
            super(itemView);

            groupImage = itemView.findViewById(R.id.groupImage);
            groupTitle = itemView.findViewById(R.id.groupNameTv);
            groupDescription = itemView.findViewById(R.id.groupDescriptionTv);
            openGroup = itemView.findViewById(R.id.openGroupBtn);
        }
    }
}
