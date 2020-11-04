package com.max.hobnob;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.max.hobnob.Adapter.UserAdapter;
import com.max.hobnob.Model.Chatlist;
import com.max.hobnob.Model.User;
import com.max.hobnob.notification.Token;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class chatFragment extends Fragment {
    private RecyclerView recyclerView;
    Context mctx;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    AVLoadingIndicatorView loadingIndicatorView;
    FirebaseUser fuser;
    DatabaseReference reference;

    private List<Chatlist> usersList;
    TextView no_chat;
    public chatFragment() {
        // Required empty public constructor
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chat, container, false);
        mctx=view.getContext();
        recyclerView = view.findViewById(R.id.chats_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadingIndicatorView=view.findViewById(R.id.avi);
        loadingIndicatorView.smoothToShow();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
       no_chat=view.findViewById(R.id.no_chat_tv);
        usersList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;
    }
    private void chatList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : usersList){
                        assert user != null;
                        if (String.valueOf(user.getId()).equals(chatlist.getId())){
                        mUsers.add(user);
                         }
                    }
                }
                loadingIndicatorView.smoothToHide();
                loadingIndicatorView.setVisibility(View.GONE);
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
                if(userAdapter.getItemCount()<1)
                {
                    no_chat.setVisibility(View.VISIBLE);
                }
                else
                {
                    no_chat.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
