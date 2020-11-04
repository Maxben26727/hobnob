package com.max.hobnob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.max.hobnob.Adapter.GroupAdapter;
import com.max.hobnob.Adapter.UserAdapter;
import com.max.hobnob.Model.Chatlist;
import com.max.hobnob.Model.User;
import com.max.hobnob.Model.group;
import com.max.hobnob.Model.grouplist;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;


public class groupFragment extends Fragment {
private Context mctx;
    private RecyclerView recyclerView;

    private List<group> groups;
    AVLoadingIndicatorView loadingIndicatorView;
    private FirebaseUser fuser;
    private DatabaseReference reference;
TextView no_groups;
    private List<grouplist> grouplists;
private ImageButton add_group;
    public groupFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.fragment_group, container, false);
        recyclerView = view.findViewById(R.id.group_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadingIndicatorView=view.findViewById(R.id.avi_group);
        loadingIndicatorView.smoothToShow();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        no_groups=view.findViewById(R.id.no_group_tv);
        grouplists = new ArrayList<>();
        mctx=view.getContext();
        add_group=view.findViewById(R.id.add_group);

        reference = FirebaseDatabase.getInstance().getReference("GroupList").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                grouplists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    grouplist grouplist = snapshot.getValue(grouplist.class);
                    grouplists.add(grouplist);
                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        add_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mctx,AddGroupActivity.class);
                intent.putExtra("for","create");
                startActivity(intent);
            }
        });





        return view;
    }
    private void chatList() {
        groups = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    group group = snapshot.getValue(group.class);
                    for (grouplist grouplist : grouplists){
                        assert group != null;
                        if (String.valueOf(group.getGroupID()).equals(grouplist.getGroupID())){
                            groups.add(group);
                        }
                    }
                }
                loadingIndicatorView.smoothToHide();
                loadingIndicatorView.setVisibility(View.GONE);
               GroupAdapter groupAdapter = new GroupAdapter(getContext(), groups);
                recyclerView.setAdapter(groupAdapter);
                if(groupAdapter.getItemCount()<1)
                {
                    no_groups.setVisibility(View.VISIBLE);
                }
                else
                {
                    no_groups.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
