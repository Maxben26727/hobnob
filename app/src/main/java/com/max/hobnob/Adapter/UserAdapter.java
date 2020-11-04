package com.max.hobnob.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.max.hobnob.R;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private List<User> userListFull;
    private boolean ischat;
    int i;
    List<User> filterdList = new ArrayList<>();
    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
        this.i = 0;
        this.userListFull=new ArrayList<>(mUsers);

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, final int position) {
        final User user = mUsers.get(position);


        if (ischat) {
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
            holder.username.setText(user.getUsername());
            if (!String.valueOf(user.getImageURL()).equals("default")) {
                Glide.with(mContext).load(user.getImageURL()).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.profile_image);
            }
            lastMessage(user.getId(), holder.last_msg, holder.seen, holder.delivered);
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
            holder.last_msg.setVisibility(View.GONE);
            holder.username.setText(user.getUsername());
            holder.last_msg.setText(user.getAbout());
            if (!String.valueOf(user.getImageURL()).equals("default")) {
                Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
            }

        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("ID", user.getId());
                intent.putExtra("for", "single");
                mContext.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public Filter getFilter() {
        return productFilter;
    }
    private  Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filterdList.clear();
            if(constraint == null || constraint.length() == 0){
                filterdList.addAll(userListFull);
            }
            else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(User item:userListFull){
                    if(item.getUsername().toLowerCase().contains(filterPattern)){
                        filterdList.add(item);
                    }
                }
            }
            FilterResults results=new FilterResults();
            results.values = filterdList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mUsers.clear();
            mUsers.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };






    //check for last message
    private void lastMessage(final String userid, final TextView last_msg, final ImageView seen, final ImageView delivered) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference chatkey_ref = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid()).child(userid);
        chatkey_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Chatlist chatkey_data = dataSnapshot.getValue(Chatlist.class);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(chatkey_data.getChatkey());

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Chat chat = snapshot.getValue(Chat.class);
                                if (firebaseUser != null && chat != null) {

                                    if (chat.getMessage().startsWith("https://firebasestorage.googleapis.com/v0/b/hobnob-b987d.appspot.com")) {
                                        theLastMessage = "media file";
                                    } else
                                        theLastMessage = chat.getMessage();
                                    if(chat.isIsseen())
                                    {
                                        if(chat.getSender().equals(firebaseUser.getUid()))
                                        {
                                            delivered.setVisibility(View.GONE);
                                            seen.setVisibility(View.VISIBLE);
                                        }
                                        else
                                        {
                                            delivered.setVisibility(View.GONE);
                                            seen.setVisibility(View.GONE);
                                        }
                                    }else
                                    {
                                        if(chat.getSender().equals(firebaseUser.getUid())) {
                                            seen.setVisibility(View.GONE);
                                            delivered.setVisibility(View.VISIBLE);
                                        }
                                        else
                                        {
                                            delivered.setVisibility(View.GONE);
                                            seen.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }

                            switch (theLastMessage) {
                                case "default":
                                    last_msg.setText("No Message");
                                    break;

                                default:
                                    last_msg.setText(theLastMessage);
                                    break;
                            }

                            theLastMessage = "default";


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView username, msg_count;
        ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off, seen, delivered;
        private TextView last_msg;


        UserViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            msg_count = itemView.findViewById(R.id.msg_count);
            seen = itemView.findViewById(R.id.seen);
            delivered = itemView.findViewById(R.id.delivered);
        }
    }
}
