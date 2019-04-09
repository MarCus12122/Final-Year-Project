package com.example.rahulsingh.pseudo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = (RecyclerView)findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    public void updateUserStatus(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        UsersRef.child(online_user_id).child("userState").updateChildren(currentStateMap);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        updateUserStatus("online");
    }



    private void DisplayAllFriends()
    {
        FirebaseRecyclerAdapter<Friends, friendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, friendsViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_display_layout,
                        friendsViewHolder.class,
                        FriendsRef
                )
        {
            @Override
            protected void populateViewHolder(final friendsViewHolder viewHolder, Friends model, int position)
            {
                viewHolder.setDate(model.getDate());
                final String usersIDs = getRef(position).getKey();

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        final String userName = dataSnapshot.child("fullname").getValue().toString();
                        final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                        final String type;

                        if(dataSnapshot.hasChild("userState"))
                        {
                            type = dataSnapshot.child("userState").child("type").getValue().toString();

                            if(type.equals("online"))
                            {
                                viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                            }
                        }

                        viewHolder.setFullname(userName);
                        viewHolder.setProfileimage(getApplicationContext(), profileImage);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                CharSequence options[] = new CharSequence[]
                                        {
                                                userName + "'s Profile",
                                                "Send Message"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                builder.setTitle("Select option");

                                builder.setItems(options, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        if(i==0)
                                        {
                                            Intent profileintent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                            profileintent.putExtra("visit_user_id", usersIDs);
                                            startActivity(profileintent);
                                        }

                                        if(i==1)
                                        {
                                            Intent Chatintent = new Intent(FriendsActivity.this, ChatActivity.class);
                                            Chatintent.putExtra("visit_user_id", usersIDs);
                                            Chatintent.putExtra("userName", userName);
                                            startActivity(Chatintent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageView onlineStatusView;

        public friendsViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;

            onlineStatusView = (ImageView)itemView.findViewById(R.id.all_users_online_icon);
        }

        public void setProfileimage(Context applicationContext, String profileimage)
        {
            CircleImageView myImage = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).into(myImage);
        }

        public void setFullname(String fullname)
        {
            TextView myName = (TextView)mView.findViewById(R.id.all_users_profile_name);
            myName.setText(fullname);
        }

        public void setDate(String date)
        {
            TextView friendsDate = (TextView)mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends since: " + date);
        }
    }
}
