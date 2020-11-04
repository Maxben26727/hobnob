package com.max.hobnob;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.max.hobnob.Adapter.UserAdapter;
import com.max.hobnob.Model.User;
import com.max.hobnob.Model.grouplist;
import com.max.hobnob.Model.memberlist;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddGroupActivity extends AppCompatActivity {
    static List<String> selected_user=new ArrayList<>();
    private List<User> mUsers=new ArrayList<>();
    List<String> contact_list = new ArrayList<>();
    final int IMAGE_REQUEST_CODE = 999;
    private Uri filepath;
    TextView title;
    AVLoadingIndicatorView loadingIndicatorView;
    CircleImageView gicon;
    RecyclerView recyclerView;
    Bitmap bitmap;
    Intent i;
    String for_what;
    ImageButton update_group;
    ImageButton grupname_edit;
    ImageButton grupdesc_edit;
    ImageButton add_person;

    Button create;
    ImageButton gicon_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        selected_user.clear();
        setContentView(R.layout.activity_add_group);
         update_group=findViewById(R.id.btn_update_group);
         grupname_edit=findViewById(R.id.edit_group_name_btn);
         grupdesc_edit=findViewById(R.id.edit_group_desc_btn);
         add_person=findViewById(R.id.add_person);

         create=findViewById(R.id.btn_create_group);
         gicon_btn=findViewById(R.id.gicon_select_btn);
        gicon=findViewById(R.id.group_icon);
        loadingIndicatorView=findViewById(R.id.avi_create_grp);
        recyclerView=findViewById(R.id.select_member);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        final EditText gname_ed=findViewById(R.id.groupName);
        final EditText gdesc_ed=findViewById(R.id.groupdesc);
        title=findViewById(R.id.title);
         i=getIntent();
         for_what=i.getStringExtra("for");
        if(for_what.equals("create")) {
            title.setText("ADD NEW GROUP");
            addalluser();


            create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    create.setVisibility(View.GONE);
                    loadingIndicatorView.smoothToShow();
                    loadingIndicatorView.setVisibility(View.VISIBLE);
                    final String group_name = gname_ed.getText().toString();
                    final String group_desc = gdesc_ed.getText().toString();
                    if (TextUtils.isEmpty(group_name) || TextUtils.isEmpty(group_desc)) {
                        Toast.makeText(AddGroupActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    } else if (bitmap == null) {
                        Toast.makeText(AddGroupActivity.this, "Select a Icon for Group", Toast.LENGTH_SHORT).show();
                    } else if (selected_user.size() < 2) {
                        Toast.makeText(AddGroupActivity.this, "U cant create group with only one Member", Toast.LENGTH_SHORT).show();
                    } else {
                        final StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReference().child("group_icon").child(filepath.getLastPathSegment());
                        storageReference.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imgdata = uri.toString();
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        assert firebaseUser != null;
                                        String userid = firebaseUser.getUid();
                                        selected_user.add(userid);
                                        final DatabaseReference ref_group = FirebaseDatabase.getInstance().getReference("Groups");
                                        final String groupID = ref_group.push().getKey();
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("groupID", groupID);
                                        hashMap.put("groupName", group_name);
                                        hashMap.put("adminId", userid);
                                        hashMap.put("groupDesc", group_desc);
                                        hashMap.put("groupPic", imgdata);
                                        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                        hashMap.put("date", date);
                                        ref_group.child(groupID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    HashMap<String, Object> userlisthashmap = new HashMap<>();
                                                    DatabaseReference groupList;
                                                    HashMap<String, Object> groplisthashmap = new HashMap<>();
                                                    groplisthashmap.put("groupID", groupID);
                                                    for (String s : selected_user) {
                                                        userlisthashmap.put("memberID", s);
                                                        groupList = FirebaseDatabase.getInstance().getReference("GroupList").child(s).child(groupID);
                                                        ref_group.child(groupID).child("members").child(s).setValue(userlisthashmap);
                                                        groupList.setValue(groplisthashmap);


                                                    }
                                                    Toast.makeText(AddGroupActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                                                    loadingIndicatorView.smoothToHide();
                                                    loadingIndicatorView.setVisibility(View.GONE);
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingIndicatorView.setVisibility(View.GONE);
                                create.setVisibility(View.VISIBLE);
                                Toast.makeText(AddGroupActivity.this, "Failed,Check your Internet Connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });


        }else if(for_what.equals("info")){
            create.setVisibility(View.GONE);
            title.setText("GROUP INFO");
            if(FirebaseAuth.getInstance().getUid().equals(i.getStringExtra("adminID"))) {
                add_person.setVisibility(View.VISIBLE);
            }
            grupname_edit.setVisibility(View.VISIBLE);
            grupdesc_edit.setVisibility(View.VISIBLE);
            Glide.with(AddGroupActivity.this).load(i.getStringExtra("groupicon")).into(gicon);
            String groupName=i.getStringExtra("groupName");
            String groupDesc=i.getStringExtra("groupdesc");
            gname_ed.setEnabled(false);
            gdesc_ed.setEnabled(false);
            gname_ed.setText(groupName);
            gdesc_ed.setText(groupDesc);
            loadmembers();

            grupname_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(gname_ed.isEnabled())
                    {
                        gname_ed.setEnabled(false);
                        grupname_edit.setBackgroundColor(Color.argb(0,0,0,0));
                        String new_name=gname_ed.getText().toString();
                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups").child(Objects.requireNonNull(i.getStringExtra("groupID")));
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("groupName",new_name);
                        reference.updateChildren(hashMap);
                    }
                    else
                    {
                        gname_ed.setEnabled(true);
                        grupname_edit.setBackground(getResources().getDrawable(R.drawable.greengradientbg,null));
                    }
                }
            });
            grupdesc_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(gdesc_ed.isEnabled())
                    {
                        gdesc_ed.setEnabled(false);
                        grupdesc_edit.setBackgroundColor(Color.argb(0,0,0,0));
                        String new_name=gdesc_ed.getText().toString();
                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups").child(Objects.requireNonNull(i.getStringExtra("groupID")));
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("groupDesc",new_name);
                        reference.updateChildren(hashMap);
                    }
                    else
                    {
                        gdesc_ed.setEnabled(true);
                        grupdesc_edit.setBackground(getResources().getDrawable(R.drawable.greengradientbg,null));
                    }
                }
            });
            add_person.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addalluser();
                    add_person.setVisibility(View.GONE);
                    update_group.setVisibility(View.VISIBLE);
                }
            });
            update_group.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selected_user.size()>0) {
                        DatabaseReference ref_group = FirebaseDatabase.getInstance().getReference("Groups");
                        HashMap<String, Object> userlisthashmap = new HashMap<>();
                        DatabaseReference groupList;
                        HashMap<String, Object> groplisthashmap = new HashMap<>();
                        String groupID = i.getStringExtra("groupID");
                        groplisthashmap.put("groupID", groupID);
                        for (String s : selected_user) {
                            userlisthashmap.put("memberID", s);
                            groupList = FirebaseDatabase.getInstance().getReference("GroupList").child(s).child(groupID);
                            ref_group.child(groupID).child("members").child(s).updateChildren(userlisthashmap);
                            groupList.updateChildren(groplisthashmap);
                        }
                        update_group.setVisibility(View.GONE);
                        add_person.setVisibility(View.VISIBLE);
                        loadmembers();
                    } else
                    {
                        update_group.setVisibility(View.GONE);
                        add_person.setVisibility(View.VISIBLE);
                        loadmembers();
                    }
                }

            });
        }
        gicon_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(AddGroupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_REQUEST_CODE);

            }
        });
    }
    public void loadmembers()
    {
        final List<User> members=new ArrayList<>();
        final UserAdapterforGroup adapter = new UserAdapterforGroup(AddGroupActivity.this, members,"read");
        recyclerView.setAdapter(adapter);
        String groupID=i.getStringExtra("groupID");
        DatabaseReference group_user_ref=FirebaseDatabase.getInstance().getReference("Groups");
        final DatabaseReference user_ref=FirebaseDatabase.getInstance().getReference("Users");
        group_user_ref.child(groupID).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    //for getting members same grouplist model used
                    memberlist member=snapshot.getValue(memberlist.class);
                    assert member != null;
                    user_ref.child(member.getMemberID()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User data=dataSnapshot.getValue(User.class);
                            members.add(data);
                            adapter.notifyDataSetChanged();
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
    public void addalluser()
    {
        contact_list.clear();
        mUsers.clear();
        Cursor contacts = this.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );


        if (contacts != null) {
            while (contacts.moveToNext()) {
                contact_list.add(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim());
            }
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User contact_data = snapshot.getValue(User.class);
                        for (String s : contact_list) {
                            assert contact_data != null;
                            if (contact_data.getPhone().contains(s)) {
                                mUsers.add(contact_data);
                                break;
                            }
                        }
                    }

                    UserAdapterforGroup adapter = new UserAdapterforGroup(AddGroupActivity.this, mUsers,"edit");
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            contacts.close();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddGroupActivity.this);


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
                    gicon.setImageBitmap(bitmap);
                    if(for_what.equals("info"))
                    {
                        final String groupID=i.getStringExtra("groupID");
                        final DatabaseReference group_user_ref=FirebaseDatabase.getInstance().getReference("Groups");

                        final StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReference().child("group_icon").child(filepath.getLastPathSegment());
                        storageReference.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                               storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                   @Override
                                   public void onSuccess(Uri uri) {
                                        HashMap<String,Object> hashMap=new HashMap<>();
                                        hashMap.put("groupPic",uri.toString());
                                       group_user_ref.child(groupID).updateChildren(hashMap);
                                   }
                               }) ;
                            }
                        });


                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }




    public static class UserAdapterforGroup extends  RecyclerView.Adapter<UserAdapterforGroup.ViewHolder> {

        private Context mContext;
        private String type;
        private List<User> mUsers;



        public UserAdapterforGroup(Context mContext, List<User> mUsers,String type) {
            this.mUsers = mUsers;
            this.mContext = mContext;
            this.type = type;
        }

        @NonNull
        @Override
        public UserAdapterforGroup.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.user_item_for_group, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final UserAdapterforGroup.ViewHolder holder, final int position) {
             final User user = mUsers.get(position);
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
            holder.username.setText(user.getUsername());
            if(type.equals("edit"))
            {
                holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {
                        selected_user.add(user.getId());
                    }
                    if(!isChecked)
                    {
                        selected_user.remove(user.getId());

                    }
                }
            });}
            else
            {
                holder.checkBox.setVisibility(View.GONE);
            }

        }


        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView username;
            ImageView profile_image;
            CheckBox checkBox;


            ViewHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                profile_image = itemView.findViewById(R.id.profile_image);
                checkBox=itemView.findViewById(R.id.checkBox);
            }
        }


    }

}
