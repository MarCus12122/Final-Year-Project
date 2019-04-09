package com.example.rahulsingh.pseudo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity
{
    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendReqButton, CancelFriendReqButton;

    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        InitializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).into(userProfileImage);
                    userName.setText("@ " + myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);
                    userRelation.setText("Relationship: " + myRelationStatus);
                    userDOB.setText("DOB: " + myDOB);

                    ButtonStatus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        CancelFriendReqButton.setVisibility(View.INVISIBLE);
        CancelFriendReqButton.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {
            SendFriendReqButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SendFriendReqButton.setEnabled(false);
                    if(CURRENT_STATE.equals("not friends"))
                    {
                        SendFriendRequest();
                    }

                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }

                    if(CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }

                    if(CURRENT_STATE.equals("friends"))
                    {
                        UnfriendAnExistingFriend();
                    }
                }
            });
        }
        else
        {
            CancelFriendReqButton.setVisibility(View.INVISIBLE);
            SendFriendReqButton.setVisibility(View.INVISIBLE);
        }
    }

    private void UnfriendAnExistingFriend()
    {
        FriendsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendFriendReqButton.setEnabled(true);
                                CURRENT_STATE = "not friends";
                                SendFriendReqButton.setText("Send Request");

                                CancelFriendReqButton.setVisibility(View.INVISIBLE);
                                CancelFriendReqButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptFriendRequest()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                FriendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            FriendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if(task.isSuccessful())
                                                    {
                                                        SendFriendReqButton.setEnabled(true);
                                                        CURRENT_STATE = "friends";
                                                        SendFriendReqButton.setText("Unfriend");

                                                        CancelFriendReqButton.setVisibility(View.INVISIBLE);
                                                        CancelFriendReqButton.setEnabled(false);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendFriendReqButton.setEnabled(true);
                                CURRENT_STATE = "not friends";
                                SendFriendReqButton.setText("Send Request");

                                CancelFriendReqButton.setVisibility(View.INVISIBLE);
                                CancelFriendReqButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void ButtonStatus()
    {
        FriendRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(receiverUserId))
                {
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent"))
                    {
                        CURRENT_STATE = "request_sent";
                        SendFriendReqButton.setText("Cancel Request");

                        CancelFriendReqButton.setVisibility(View.INVISIBLE);
                        CancelFriendReqButton.setEnabled(false);
                    }

                    else if(request_type.equals("received"))
                    {
                        CURRENT_STATE = "request_received";
                        SendFriendReqButton.setText("Accept request");

                        CancelFriendReqButton.setVisibility(View.VISIBLE);
                        CancelFriendReqButton.setEnabled(true);

                        CancelFriendReqButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                CancelFriendRequest();
                            }
                        });
                    }
                }

                else
                {
                    FriendsRef.child(senderUserId).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.hasChild(receiverUserId))
                            {
                                CURRENT_STATE = "friends";
                                SendFriendReqButton.setText("Unfriend");

                                CancelFriendReqButton.setVisibility(View.INVISIBLE);
                                CancelFriendReqButton.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendFriendReqButton.setEnabled(true);
                                CURRENT_STATE = "request_sent";
                                SendFriendReqButton.setText("Cancel Request");

                                CancelFriendReqButton.setVisibility(View.INVISIBLE);
                                CancelFriendReqButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void InitializeFields()
    {
        userName = (TextView) findViewById(R.id.person_username);
        userProfName = (TextView)findViewById(R.id.person_full_name);
        userStatus = (TextView)findViewById(R.id.person_profile_status);
        userCountry = (TextView)findViewById(R.id.person_country);
        userGender = (TextView)findViewById(R.id.person_gender);
        userRelation = (TextView)findViewById(R.id.person_relationship_status);
        userDOB = (TextView)findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView)findViewById(R.id.person_profile_pic);

        SendFriendReqButton = (Button)findViewById(R.id.person_send_friend_request_btn);
        CancelFriendReqButton = (Button)findViewById(R.id.person_decline_friend_request_btn);

        CURRENT_STATE = "not friends";
    }
}
