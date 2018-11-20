package com.example.anik.tripmate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AddPinPost extends AppCompatActivity {
    FirebaseAuth mAuth;
    DatabaseReference mUserEventDatabase;
    FirebaseUser mCurrentUser;

    EditText et;
    Button post;

    String s, eventRoot, placeName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_pin_post);

        Intent intent = getIntent();

        eventRoot = intent.getStringExtra("eventRoot");
        placeName = intent.getStringExtra("place");

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mUserEventDatabase = FirebaseDatabase.getInstance().getReference().child("event").child(eventRoot).child(placeName);

        et = (EditText) findViewById(R.id.typePinpost);
        post = (Button) findViewById(R.id.savePinpost);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                s = et.getText().toString();

                if(s.isEmpty()) {
                    Toast.makeText(AddPinPost.this, "Pinpost cann't be empty!", Toast.LENGTH_SHORT).show();
                }

                else {
                    final String pushKey = mUserEventDatabase.child("PinPost").push().getKey();

                    Date c = Calendar.getInstance().getTime();

                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    final String formattedDate = df.format(c);

                    FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserInfo info = dataSnapshot.getValue(UserInfo.class);
                            try {
                                Map map = new HashMap();
                                map.put("pinpostText", s);
                                map.put("pinpostDate", formattedDate);
                                map.put("namePinPost", info.getName());

                                mUserEventDatabase.child("PinPost").child(pushKey).updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Toast.makeText(AddPinPost.this, "Post Added.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(AddPinPost.this, "An error occured in uploading your post. Check you provie your name.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
//                    mUserEventDatabase.child("PinPost").child(pushKey).setValue(s).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(AddPinPost.this, "Post Added. "+formattedDate, Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }
            }
        });
    }
}
