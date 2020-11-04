package com.max.hobnob;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.max.hobnob.Adapter.UserAdapter;
import com.max.hobnob.Model.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class contactFragment extends Fragment {
    private RecyclerView recyclerView;
    private Context mContext;
    private List<User> mUsers=new ArrayList<>();
    List<String> contact_list = new ArrayList<>();
    TextView no_contacts;
    AVLoadingIndicatorView loadingIndicatorView;
    UserAdapter userAdapter;
    public contactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onPause() {
        super.onPause();
        mUsers.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        mContext = view.getContext();

        loadingIndicatorView = view.findViewById(R.id.avi2);
        loadingIndicatorView.smoothToShow();
        no_contacts=view.findViewById(R.id.no_contact_tv);
        SearchView searchView=view.findViewById(R.id.search);
        recyclerView = view.findViewById(R.id.contacts_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



       load_contacts();
       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               return false;
           }

           @Override
           public boolean onQueryTextChange(String newText) {
               Objects.requireNonNull( userAdapter.getFilter()).filter(newText);
               return false;
           }
       });
        return view;
    }

    private void load_contacts()
    {

        contact_list.clear();
        mUsers.clear();
        Cursor contacts = mContext.getContentResolver().query(
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
                            if(!contact_data.getId().equals(FirebaseAuth.getInstance().getUid()))
                            if (contact_data.getPhone().contains(s)) {
                                mUsers.add(contact_data);
                                break;
                            }
                        }
                    }
                    loadingIndicatorView.smoothToHide();
                    loadingIndicatorView.setVisibility(View.GONE);
                     userAdapter = new UserAdapter(getContext(), mUsers, false);
                    recyclerView.setAdapter(userAdapter);
                    if(userAdapter.getItemCount()<1)
                    {
                        no_contacts.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        no_contacts.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            contacts.close();
        }

    }


}
