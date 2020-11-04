package com.max.hobnob;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.max.hobnob.Adapter.MessageAdapter;
import com.max.hobnob.Model.Chat;
import com.max.hobnob.Model.Chatlist;
import com.max.hobnob.Model.User;
import com.max.hobnob.Model.group;
import com.max.hobnob.Model.memberlist;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;


public class MessageActivity extends AppCompatActivity {
    final int IMAGE_REQUEST_CODE = 999;
    CircleImageView profile_image;
    TextView username, status_txt;
     Chatlist chatkey_data;
    FirebaseUser fuser;
    DatabaseReference sender_ref;
        AVLoadingIndicatorView avi;
    ImageButton btn_send, btn_media, btn_gallery, back_btn, btn_rec_audio,btn_snd_audio;
    EmojiconEditText text_send;
        Dialog dialog;
    MessageAdapter messageAdapter;
    List<Chat> mchat = new ArrayList<>();
    RecyclerView recyclerView;

    Intent intent;
    Uri filepath;
    ValueEventListener seenListener;
    MediaRecorder recorder;
    String ID,for_what;
    String filedir = null;
    String s="";
    LinearLayout media_layout;
    User senderData;
final int PERMISSION_CODE=123;
    boolean notify = false;
Chronometer chronometer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        intent = getIntent();
        ID = intent.getStringExtra("ID");
        for_what=intent.getStringExtra("for");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        sender_ref = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());


        View rootview=findViewById(R.id.topl);
        text_send = findViewById(R.id.text_send);
        ImageButton emojiButton=findViewById(R.id.show_emoji);
        EmojIconActions emojIcon=new EmojIconActions(this,rootview,text_send,emojiButton);
        emojIcon.setIconsIds(R.drawable.ic_keyboard_black_24dp,R.drawable.open_emo);
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                btn_send.setVisibility(View.VISIBLE);
                btn_rec_audio.setVisibility(View.GONE);
                btn_snd_audio.setVisibility(View.GONE);
            }

            @Override
            public void onKeyboardClose() {
                btn_send.setVisibility(View.GONE);
                btn_rec_audio.setVisibility(View.VISIBLE);
            }
        });
        media_layout = findViewById(R.id.media_layout);
        btn_media = findViewById(R.id.show_media);
        btn_rec_audio = findViewById(R.id.btn_rec_audio);
        btn_snd_audio = findViewById(R.id.btn_send_audio);
        back_btn = findViewById(R.id.back_btn);
        btn_gallery = findViewById(R.id.gallery_btn);
        chronometer=findViewById(R.id.chronomtr);
        chronometer.setBase(SystemClock.elapsedRealtime());
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        avi=findViewById(R.id.avi_in_chat);
        File folder = this.getExternalFilesDir("");
        File file = new File(folder, "voice_notes");
        if (!file.exists())
            file.mkdir();
        filedir = this.getExternalFilesDir("").getPath() + "/voice_notes/";
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        status_txt = findViewById(R.id.status);

        FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                senderData=dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if(for_what.equals("single"))
        {
            DatabaseReference chatkey_ref = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid()).child(ID);
            chatkey_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        chatkey_data = dataSnapshot.getValue(Chatlist.class);
                        readMesagges();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(MessageActivity.this,user_info_Activity.class);
                    intent.putExtra("ID",ID);
                    startActivity(intent);
                }
            });

            sender_ref = FirebaseDatabase.getInstance().getReference("Users").child(ID);
            sender_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    status_txt.setText(user.getStatus());
                    Glide.with(getApplicationContext()).load(user.getImageURL()).diskCacheStrategy(DiskCacheStrategy.NONE).into(profile_image);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        if(for_what.equals("group"))
        {
            final group[] g = new group[1];
            sender_ref = FirebaseDatabase.getInstance().getReference("Groups").child(ID);
            sender_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    g[0] = dataSnapshot.getValue(group.class);
                    username.setText(g[0].getGroupName());
                    status_txt.setVisibility(View.GONE);
                    Glide.with(getApplicationContext()).load(g[0].getGroupPic()).diskCacheStrategy(DiskCacheStrategy.NONE).into(profile_image);

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            readMesagges();
            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(MessageActivity.this,AddGroupActivity.class);
                    intent.putExtra("for","info");
                    intent.putExtra("groupID",g[0].getGroupID());
                    intent.putExtra("groupName",g[0].getGroupName());
                    intent.putExtra("groupdesc",g[0].getGroupDesc());
                    intent.putExtra("groupicon",g[0].getGroupPic());
                    intent.putExtra("adminID",g[0].getAdminId());
                    startActivity(intent);
                }
            });
        }
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    MessageActivity.this.sendMessage(fuser.getUid(), ID, msg, "text");
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });
        btn_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (media_layout.getVisibility() == View.GONE)
                    media_layout.setVisibility(View.VISIBLE);
                else
                    media_layout.setVisibility(View.GONE);
            }
        });
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_REQUEST_CODE);
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageActivity.this.finish();
            }
        });


        btn_rec_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkRecordPermission())
                {
                    btn_rec_audio.setVisibility(View.GONE);
                    btn_snd_audio.setVisibility(View.VISIBLE);
                    chronometer.setVisibility(View.VISIBLE);
                    text_send.setEnabled(false);
                    s = "audio_record" + System.currentTimeMillis() + ".aac";
                    filedir += s;
                    startRecording();
                }
                else {
                    dialog = new Dialog(MessageActivity.this);
                    dialog.setContentView(R.layout.alert_dialog);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.color.transparent);
                    dialog.setCanceledOnTouchOutside(false);
                    final TextView title, message;
                    final Button yes, no;
                    title = dialog.findViewById(R.id.alert_title);
                    message = dialog.findViewById(R.id.alert_message);
                    yes = dialog.findViewById(R.id.yes_btn);
                    no = dialog.findViewById(R.id.no_btn);
                    no.setVisibility(View.GONE);
                    dialog.setCancelable(false);
                    title.setText("AUDIO RECORD Permission Required");
                    message.setText("u can't send audio message if u are not allowed to record");
                    dialog.show();
                    yes.setText("OK");
                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);

                        }
                    });
                }
            }
        });
        btn_snd_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avi.setVisibility(View.VISIBLE);
                avi.smoothToShow();
                chronometer.stop();
                stopRecording();
                audio_upload(s);
            }
        });

    }//end of onCreate


    private boolean checkRecordPermission()
    {
        String permission = Manifest.permission.RECORD_AUDIO;
        int res = MessageActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        recorder.setOutputFile(filedir);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("AUDIO_REC_RESULT", "prepare() failed");
        }
        chronometer.setBase(SystemClock.elapsedRealtime());
        recorder.start();
        chronometer.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void audio_upload(String s) {

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("audio_msgs").child(s);
        Uri uri = Uri.fromFile(new File(filedir));
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri1) {
                        MessageActivity.this.sendMessage(fuser.getUid(), ID, uri1.toString(), "voiceMessage");
                        btn_rec_audio.setVisibility(View.VISIBLE);
                        btn_snd_audio.setVisibility(View.GONE);
                        chronometer.setVisibility(View.GONE);
                        text_send.setEnabled(true);
                        avi.smoothToHide();
                        avi.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void sendMessage(String sender, final String receiver, final String message, final String type) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
        String localTime = format.format(cal.getTime()).toUpperCase();
        final HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("time", localTime);
        hashMap.put("type", type);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        final String[] chatkey = new String[1];
        if(for_what.equals("single"))
        {
            hashMap.put("senderName",senderData.getUsername());
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chats");
            if (chatkey_data != null) {
            String msgid=reference.child(chatkey_data.getChatkey()).push().getKey();
            hashMap.put("messageID",msgid);
            reference.child(chatkey_data.getChatkey()).child(msgid).setValue(hashMap);
            avi.smoothToHide();
            avi.setVisibility(View.GONE);
            readMesagges();
        } else {
            final DatabaseReference chatkey_ref = FirebaseDatabase.getInstance().getReference("Chatlist").child(sender).child(receiver);
            chatkey_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        chatkey[0] = reference.push().getKey();
                        assert chatkey[0] != null;
                        String msgid = reference.child(chatkey[0]).push().getKey();
                        hashMap.put("messageID", msgid);
                        reference.child(chatkey[0]).child(msgid).setValue(hashMap);
                        HashMap<String, Object> hashMap1 = new HashMap<>();
                        hashMap1.put("id", ID);
                        hashMap1.put("chatkey", chatkey[0]);
                        chatkey_ref.setValue(hashMap1);
                        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                                .child(ID)
                                .child(fuser.getUid());
                        HashMap<String, Object> hashMap2 = new HashMap<>();
                        hashMap2.put("id", fuser.getUid());
                        hashMap2.put("chatkey", chatkey[0]);
                        chatRefReceiver.setValue(hashMap2);
                        avi.smoothToHide();
                        avi.setVisibility(View.GONE);
                        readMesagges();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
            if (notify) {
                sender_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user=dataSnapshot.getValue(User.class);
                        DatabaseReference notify_ref = FirebaseDatabase.getInstance().getReference("Notifications");
                        HashMap<String, Object> notifymap = new HashMap<>();
                        assert user != null;
                        notifymap.put("from", user.getUsername());
                        notifymap.put("message", message);
                        notify_ref.child(receiver).push().setValue(notifymap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            notify = false;

        }
        else if(for_what.equals("group"))
        {
            sender_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    hashMap.put("senderName",user.getUsername());
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups").child(ID).child("chats");
                    String msgid=reference.push().getKey();
                    hashMap.put("messageID",msgid);
                    reference.child(msgid).setValue(hashMap);
                    avi.smoothToHide();
                    avi.setVisibility(View.GONE);
                    DatabaseReference group_user_ref=FirebaseDatabase.getInstance().getReference("Groups");
                    group_user_ref.child(ID).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot:dataSnapshot.getChildren())
                            {
                                memberlist member=snapshot.getValue(memberlist.class);
                                assert member != null;
                                if(!member.getMemberID().equals(fuser.getUid())) {


                                    if (notify) {
                                        DatabaseReference notify_ref = FirebaseDatabase.getInstance().getReference("Notifications");
                                        HashMap<String, Object> notifymap = new HashMap<>();
                                        notifymap.put("from", username.getText());
                                        if(type.equals("text"))
                                            notifymap.put("message",user.getUsername()+" : "+ message);
                                        else
                                            notifymap.put("message","media file received");
                                        notify_ref.child(member.getMemberID()).push().setValue(notifymap);
                                    }
                                }
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });




        }



    }


    private void readMesagges() {

        if(for_what.equals("single")) {
            if(chatkey_data!=null) {
                DatabaseReference chats = FirebaseDatabase.getInstance().getReference("Chats").child(chatkey_data.getChatkey());
                chats.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mchat.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Chat chat = snapshot.getValue(Chat.class);
                            mchat.add(chat);

                        }
                        messageAdapter = new MessageAdapter(MessageActivity.this, mchat,"single");
                        recyclerView.setAdapter(messageAdapter);
                        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MessageActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

                sender_ref = FirebaseDatabase.getInstance().getReference("Chats").child(chatkey_data.getChatkey());
                seenListener = sender_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Chat chat = snapshot.getValue(Chat.class);
                            assert chat != null;
                            if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(ID)) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("isseen", true);
                                snapshot.getRef().updateChildren(hashMap);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        else if(for_what.equals("group"))
        {
            DatabaseReference chats = FirebaseDatabase.getInstance().getReference("Groups").child(ID).child("chats");
            chats.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mchat.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        mchat.add(chat);

                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat,"group");
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {

        sender_ref = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        sender_ref.updateChildren(hashMap);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MessageActivity.this);


            }
        }
        if(requestCode == PERMISSION_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            dialog.dismiss();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        avi.smoothToShow();
        avi.setVisibility(View.VISIBLE);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filepath = result.getUri();
                media_layout.setVisibility(View.GONE);
                final StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReference().child("images").child(Objects.requireNonNull(filepath.getLastPathSegment()));
                storageReference.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imgdata = uri.toString();

                                sendMessage(fuser.getUid(), ID, imgdata, "image");
                            }
                        });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(for_what.equals("single")) {
            status("online");
            currentUser(ID);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(for_what.equals("single")) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("h:mm a");
            String localTime = format.format(cal.getTime());
            status("last seen:" + localTime);
            currentUser("none");
        }
    }


}
