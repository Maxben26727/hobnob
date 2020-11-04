package com.max.hobnob.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.max.hobnob.MessageActivity;
import com.max.hobnob.Model.Chat;
import com.max.hobnob.Model.Chatlist;
import com.max.hobnob.Model.User;
import com.max.hobnob.Model.group;
import com.max.hobnob.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private Context mContext;
    private List<group> mgroups;
    private boolean ischat;
    int i;
    String theLastMessage;

    public GroupAdapter(Context mContext, List<group> groups){
        this.mgroups = groups;
        this.mContext = mContext;
        this.i=0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final group group = mgroups.get(position);

        holder.groupname.setText(group.getGroupName());
        Glide.with(mContext).load(group.getGroupPic()).into(holder.profile_image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("ID", group.getGroupID());
                intent.putExtra("for", "group");
                mContext.startActivity(intent);
            }
        });
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups").child(group.getGroupID());
        reference.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Chat chat=snapshot.getValue(Chat.class);
                    assert chat != null;
                    if(chat.getReceiver().equals(group.getGroupID())&& !chat.getSender().equals(FirebaseAuth.getInstance().getUid())){
                        if(chat.getMessage().startsWith("https://firebasestorage.googleapis.com/v0/b/hobnob-b987d.appspot.com"))
                        {
                            theLastMessage=chat.getSenderName()+" : media file received";
                        }
                        else
                            theLastMessage = chat.getSenderName()+" : "+chat.getMessage();
                    }
                }
                holder.last_message.setText(theLastMessage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public  int getItemCount() {
        return mgroups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView groupname,msg_count,last_message;
        CircleImageView profile_image;

        ViewHolder(View itemView) {
            super(itemView);
            groupname = itemView.findViewById(R.id.groupname_tv);
            profile_image = itemView.findViewById(R.id.group_image_iv);
            msg_count = itemView.findViewById(R.id.msg_count);
            last_message = itemView.findViewById(R.id.last_msg);
        }
    }


}
