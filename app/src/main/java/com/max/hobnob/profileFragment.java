package com.max.hobnob;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.max.hobnob.Model.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class profileFragment extends Fragment {
    TextView username,phone;
    CircleImageView circleImageView;
    EmojiconEditText about_et;
    ImageButton edit_btn,change_pic;
    Button logout;
    Context mctx;
    AVLoadingIndicatorView pbar;
    final int IMAGE_REQUEST_CODE = 999;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
         mctx = v.getContext();
        edit_btn = v.findViewById(R.id.edit_btn);
        about_et = v.findViewById(R.id.about_et);
        username = v.findViewById(R.id.username);
        change_pic=v.findViewById(R.id.change_pic_btn);
        phone = v.findViewById(R.id.phone_tv);
        logout = v.findViewById(R.id.logout);
        pbar=v.findViewById(R.id.avi_logout);
        circleImageView = v.findViewById(R.id.user_pic);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query ref = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(firebaseUser.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    about_et.setText(user.getAbout());
                    phone.setText(user.getPhone());
                    Glide.with(mctx).load(user.getImageURL()).diskCacheStrategy(DiskCacheStrategy.NONE).into(circleImageView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                if (about_et.isEnabled()) {
                    String s = about_et.getText().toString();
                    about_et.setEnabled(false);
                    edit_btn.setBackgroundColor(Color.argb(0, 0, 0, 0));
                    DatabaseReference about = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("about", s);
                    about.updateChildren(hashMap);
                } else {
                    about_et.setEnabled(true);
                    edit_btn.setBackground(profileFragment.this.getResources().getDrawable(R.drawable.greengradientbg, null));
                }
            }
        });
        View rootview=v.findViewById(R.id.profile_linear);
        final ImageButton emojiButton=v.findViewById(R.id.btn_emoji);
        EmojIconActions emojIcon=new EmojIconActions(mctx,rootview,about_et,emojiButton);
        emojIcon.setIconsIds(R.drawable.ic_keyboard_black_24dp,R.drawable.open_emo);
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
               emojiButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void onKeyboardClose() {
               emojiButton.setVisibility(View.GONE);
            }
        });
logout.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v12) {
        logout.setVisibility(View.GONE);
        pbar.setVisibility(View.VISIBLE);
        DatabaseReference tokenreference = FirebaseDatabase.getInstance().getReference("Tokens").child(firebaseUser.getUid());
        tokenreference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                SharedPreferences preferences = mctx.getSharedPreferences("PREFS", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("currentuser", null);
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(mctx, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                ((MainActivity)mctx).finish();
            }
        });

    }
});
change_pic.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v13) {
        profileFragment.this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_REQUEST_CODE);

    }
});
        return v;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(mctx,profileFragment.this);


            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Toast.makeText(mctx, "pic updating...", Toast.LENGTH_SHORT).show();

                Uri filepath = result.getUri();
                final StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReference().child("user_dp").child(Objects.requireNonNull(filepath.getLastPathSegment()));
                storageReference.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("imageURL", uri.toString());

                                reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(mctx, "pic updated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
