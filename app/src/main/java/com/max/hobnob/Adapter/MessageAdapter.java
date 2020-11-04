package com.max.hobnob.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.github.chrisbanes.photoview.PhotoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.max.hobnob.Model.Chat;
import com.max.hobnob.Model.User;
import com.max.hobnob.R;

import java.io.File;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static com.max.hobnob.FileDownloader.downloadFile;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static  final int MSG_TYPE_LEFT = 0;
    private static  final int MSG_TYPE_RIGHT = 1;
    private static  final int IMAGE_TYPE_LEFT = 2;

    private static  final int IMAGE_TYPE_RIGHT = 3;
    private static  final int VOICE_TYPE_LEFT = 4;
    private static  final int VOICE_TYPE_RIGHT = 5;
    private Dialog dialog;
    private String chatType;
    private Context mContext;
    private List<Chat> mChat;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat,String chatType){
        this.mChat = mChat;
        this.mContext = mContext;
        dialog = new Dialog(mContext);
        this.chatType = chatType;
        dialog.setContentView(R.layout.bigimageviwerdialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.color.transparent);
        dialog.setCanceledOnTouchOutside(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else if(viewType==MSG_TYPE_LEFT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }
        else if(viewType==IMAGE_TYPE_LEFT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_image_left, parent, false);
            return new ViewHolder(view);
        }
        else if(viewType==IMAGE_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_image_right, parent, false);
            return new ViewHolder(view);
        }
        else if(viewType==VOICE_TYPE_LEFT)
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_voice_left, parent, false);
            return new ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_voice_right, parent, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Chat chat = mChat.get(position);
        if(chatType.equals("group")&& (getItemViewType(position)==MSG_TYPE_LEFT ||getItemViewType(position)==VOICE_TYPE_LEFT || getItemViewType(position)==IMAGE_TYPE_LEFT))
        {
            holder.senderName.setVisibility(View.VISIBLE);
            holder.senderName.setText(chat.getSenderName());
            holder.senderPic.setVisibility(View.VISIBLE);
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(chat.getSender());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    Glide.with(mContext).load(user.getImageURL()).into(holder.senderPic);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else
        {
            holder.senderName.setVisibility(View.GONE);
            holder.senderPic.setVisibility(View.GONE);

        }

        holder.txt_time.setText(chat.getTime());
        if (chat.getType().equals("image"))
        {
            Glide.with(mContext).load(chat.getMessage()).into(holder.image);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoView photoView = dialog.findViewById(R.id.photo_view);
                Glide.with(mContext).load(chat.getMessage()).into(photoView);
                dialog.show();
            }
        });
    }
        else if(chat.getType().equals("voiceMessage")){
            File audio = new File(String.format("%s/voice_notes/%s.aac", mContext.getExternalFilesDir(""), chat.getMessageID()));

            if (!audio.exists()) {
                File folder = mContext.getExternalFilesDir("");
                File file = new File(folder, "voice_notes");
                if (!file.exists())
                    file.mkdir();

                File audiofile=new File(file, String.format("%s.aac", chat.getMessageID()));
                Boolean a=downloadFile(chat.getMessage(), audiofile);
                if(a)
                {
                    holder.voicePlayerView.setAudio(audiofile.getPath());
                }
            }
            else
            {
                holder.voicePlayerView.setAudio(audio.getPath());
            }



        }
        else
            holder.show_message.setText(chat.getMessage());


if(chat.getSender().equals(FirebaseAuth.getInstance().getUid())) {
    if (chat.isIsseen()) {
        holder.delivered.setVisibility(View.GONE);
        holder.seen.setVisibility(View.VISIBLE);
    } else {
        holder.seen.setVisibility(View.GONE);
        holder.delivered.setVisibility(View.VISIBLE);
    }

}
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

     static class ViewHolder extends RecyclerView.ViewHolder{

        TextView show_message,senderName;

        TextView txt_time;
        ImageView image,seen,delivered;
        CircleImageView senderPic;
        VoicePlayerView voicePlayerView;
         ViewHolder(View itemView) {
            super(itemView);
            txt_time=itemView.findViewById(R.id.time);
            show_message = itemView.findViewById(R.id.show_message);
          seen=itemView.findViewById(R.id.seen);
          delivered=itemView.findViewById(R.id.delivered);
            image=itemView.findViewById(R.id.msg_image);
            voicePlayerView=itemView.findViewById(R.id.voicePlayerView);
            senderName=itemView.findViewById(R.id.senderName);
            senderPic=itemView.findViewById(R.id.senderPic);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())){
            if(mChat.get(position).getType().equals("image"))
                return IMAGE_TYPE_RIGHT;
            else if(mChat.get(position).getType().equals("voiceMessage"))
                return VOICE_TYPE_RIGHT;
            else
                return MSG_TYPE_RIGHT;
        } else {
            if(mChat.get(position).getType().equals("image"))
                return  IMAGE_TYPE_LEFT;
            else if(mChat.get(position).getType().equals("voiceMessage"))
                return VOICE_TYPE_LEFT;
            else
                return MSG_TYPE_LEFT;
        }
    }

}