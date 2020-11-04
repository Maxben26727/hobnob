package com.max.hobnob;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.max.hobnob.Model.User;
import com.max.hobnob.notification.Token;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    Dialog dialog;
    final int PERMISSION_CODE=555;
    Bitmap bitmap;
    AVLoadingIndicatorView dialog_pbar;
    private Uri filepath=null;
    final int IMAGE_REQUEST_CODE = 999;
    CircleImageView pic;
    String uid;
    TextView percentage;

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MainActivity.this);

            }
        }
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dialog.dismiss();
                MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.frame, new contactFragment()).commit();

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
                filepath = result.getUri();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(filepath);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    dialog.show();
                    pic.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
         LinearLayout chat,contact,profile,group;
        final TextView nav1,nav2,nav3,nav4;
        nav1=findViewById(R.id.nav1);
        nav2=findViewById(R.id.nav2);
        nav3=findViewById(R.id.nav3);
        nav4=findViewById(R.id.nav4);
        chat=findViewById(R.id.chats_cv);
        contact=findViewById(R.id.conatct_cv);
        profile=findViewById(R.id.profile_cv);
        group=findViewById(R.id.group_cv);
        nav2.setTextColor(Color.WHITE);





        uid=FirebaseAuth.getInstance().getUid();
        final Intent i=getIntent();
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.startup_profile_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.color.transparent);
        dialog.setCancelable(false);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Query dref=FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(uid)  ;   //check if user is null
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if (dataSnapshot.getChildrenCount() < 1) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    } else if (dataSnapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user.getImageURL().equals("not_set")||user.getUsername().equals("hobnob")) {
                            final EditText username, about;
                            ImageButton pic_select;
                            final Button next;
                            dialog_pbar = dialog.findViewById(R.id.avi_dialog);
                            username = dialog.findViewById(R.id.username);
                            about = dialog.findViewById(R.id.about);
                            next = dialog.findViewById(R.id.next);
                            pic_select = dialog.findViewById(R.id.profile_select_btn);
                            pic = dialog.findViewById(R.id.profile_image_reg);
                            percentage=dialog.findViewById(R.id.percentage);
                            dialog.show();
                            pic_select.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_REQUEST_CODE);
                                    dialog.dismiss();
                                }
                            });
                            next.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    next.setVisibility(View.GONE);
                                    dialog_pbar.setVisibility(View.VISIBLE);
                                    percentage.setVisibility(View.VISIBLE);
                                    if (username.length() < 1 || about.length() < 1) {
                                        Toast.makeText(MainActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                                        dialog_pbar.smoothToHide();
                                        dialog_pbar.setVisibility(View.GONE);
                                        percentage.setVisibility(View.GONE);

                                        next.setVisibility(View.VISIBLE);
                                    } else if (filepath == null) {
                                        Toast.makeText(MainActivity.this, "select a profile pic", Toast.LENGTH_SHORT).show();
                                        dialog_pbar.smoothToHide();
                                        dialog_pbar.setVisibility(View.GONE);
                                        percentage.setVisibility(View.GONE);

                                        next.setVisibility(View.VISIBLE);
                                    } else {
                                        final StorageReference storageReference = FirebaseStorage.getInstance()
                                                .getReference().child("user_dp").child(Objects.requireNonNull(filepath.getLastPathSegment()));
                                        storageReference.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String imgdata = uri.toString();

                                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);

                                                        HashMap<String, Object> hashMap = new HashMap<>();
                                                        hashMap.put("username", username.getText().toString());
                                                        hashMap.put("about", about.getText().toString());
                                                        hashMap.put("imageURL", imgdata);
                                                        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    percentage.setVisibility(View.GONE);
                                                                    dialog_pbar.setVisibility(View.GONE);
                                                                    dialog.dismiss();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                percentage.setVisibility(View.GONE);
                                                dialog_pbar.smoothToHide();
                                                dialog_pbar.setVisibility(View.GONE);
                                                next.setVisibility(View.VISIBLE);
                                                Toast.makeText(MainActivity.this, "Can't Update make sure Internet is connected", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                                double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                                                percentage.setText(String.format("%s%%", String.valueOf(Math.round(progress))));
                                            }
                                        });
                                    }
                                }

                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError){

            }

        });
















        getSupportFragmentManager().beginTransaction().replace(R.id.frame, new chatFragment()).commit();
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav1.setTextColor(Color.WHITE);
                nav2.setTextColor(Color.GRAY);
                nav3.setTextColor(Color.GRAY);
                nav4.setTextColor(Color.GRAY);
                if(checkContactReadPermission())
                {
                    MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.frame, new contactFragment()).commit();
                }
                else {
                    dialog = new Dialog(MainActivity.this);
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
                    title.setText("Contact Permission Required");
                    message.setText("u can't send message to your contacts if u are not allowed to read ur contacts");
                    dialog.show();
                    yes.setText("OK");
                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_CODE);

                        }
                    });
                }
            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nav1.setTextColor(Color.GRAY);
                nav2.setTextColor(Color.WHITE);
                nav3.setTextColor(Color.GRAY);
                nav4.setTextColor(Color.GRAY);

                MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.frame, new chatFragment()).commit();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav1.setTextColor(Color.GRAY);
                nav2.setTextColor(Color.GRAY);
                nav3.setTextColor(Color.GRAY);
                nav4.setTextColor(Color.WHITE);
                MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.frame, new profileFragment()).commit();
            }
        });

        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav1.setTextColor(Color.GRAY);
                nav2.setTextColor(Color.GRAY);
                nav3.setTextColor(Color.WHITE);
                nav4.setTextColor(Color.GRAY);
                MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.frame, new groupFragment()).commit();

            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String mtoken=instanceIdResult.getToken();
                updateToken(mtoken);
            }
        });

    }

    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshToken);
        assert firebaseUser != null;
        reference.child(firebaseUser.getUid()).setValue(token);
    }

    private void status(String status) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }
    private boolean checkContactReadPermission()
    {
        String permission = android.Manifest.permission.READ_CONTACTS;
        int res = MainActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
        String localTime = format.format(cal.getTime());
        status("last seen:"+localTime);
    }
}
