package com.max.hobnob;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.max.hobnob.Model.User;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class user_info_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Intent intent = getIntent();
        String ID = intent.getStringExtra("ID");
        final CircleImageView profile_pic=findViewById(R.id.user_icon);
        final TextView username=findViewById(R.id.username);
        final TextView about=findViewById(R.id.about_et);
        final TextView phone=findViewById(R.id.phone_tv);
        final User[] user = new User[1];
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(ID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 user[0] =dataSnapshot.getValue(User.class);
                assert user[0] != null;
                username.setText(user[0].getUsername());
                about.setText(user[0].getAbout());
                phone.setText(user[0].getPhone());
                Glide.with(user_info_Activity.this).load(user[0].getImageURL()).into(profile_pic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(user_info_Activity.this);
                dialog.setContentView(R.layout.bigimageviwerdialog);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.color.transparent);
                dialog.setCanceledOnTouchOutside(true);
                PhotoView photoView = dialog.findViewById(R.id.photo_view);
                Glide.with(user_info_Activity.this).load(user[0].getImageURL()).into(photoView);
                dialog.show();
            }
        });
    }
}
