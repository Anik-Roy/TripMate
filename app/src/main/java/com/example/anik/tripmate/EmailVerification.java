package com.example.anik.tripmate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anik on 3/8/18.
 */

public class EmailVerification extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    private DatabaseReference newStudent;

    TextView emailText;
    Button verifyButton, buttonSignup;
    Handler handler;
    boolean flag = false;
    Map<String, Object> map;
    String UserName, UserNationality, UserGender, UserBirthday, UserYear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_account_email);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        Intent data = getIntent();
        UserName = data.getStringExtra("UserName");
        UserNationality = data.getStringExtra("UserNationality");
        UserGender = data.getStringExtra("UserGender");
        UserBirthday = data.getStringExtra("UserBirthday");
        UserYear = data.getStringExtra("UserYear");

        emailText = (TextView) findViewById(R.id.emailText);
        verifyButton = (Button) findViewById(R.id.verifyButton);
        buttonSignup = (Button) findViewById(R.id.btn_signup);

        //Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();

        handler = new Handler();
        handler.postDelayed(mRunnable, 1000);

        try {
            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                Intent intent = new Intent(EmailVerification.this, ExistingEvent.class);
                startActivity(intent);
                finish();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EmailVerification.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(EmailVerification.this);
                progressDialog.show();

                if(mCurrentUser != null) {

                    mCurrentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String email = mCurrentUser.getEmail();
                            emailText.setText("Verification email sent to:\n"+email+"\n\nCheck your email.");
                            emailText.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            emailText.setText("Email Verification Failed!\nClick for send verification email again or check your email.");
                            emailText.setVisibility(View.VISIBLE);
                            progressDialog.hide();
                        }
                    });
                }

                else {
                    emailText.setText("Email Verification Failed!\nClick for send verification email again or check your email.");
                    emailText.setVisibility(View.VISIBLE);
                    progressDialog.hide();
                }
            }
        });
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            //Toast.makeText(EmailVerification.this, "running", Toast.LENGTH_SHORT).show();
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                try {
                    FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {

                                newStudent = FirebaseDatabase.getInstance().getReference();
                                mCurrentUser = mAuth.getCurrentUser();

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                map = new HashMap<String, Object>();
                                map.put(mCurrentUser.getUid() + "/locationShare", "false");
                                map.put(mCurrentUser.getUid() + "/device_token", deviceToken);
                                map.put(mCurrentUser.getUid() + "/uid", mCurrentUser.getUid());
                                map.put(mCurrentUser.getUid() + "/email", mCurrentUser.getEmail());

                                if (UserName != null && !UserName.equals(""))
                                    map.put(mCurrentUser.getUid() + "/name", UserName);
                                else
                                    map.put(mCurrentUser.getUid() + "/name", "default");

                                //map.put(mCurrentUser.getUid() + "/lastSeen", ServerValue.TIMESTAMP);

                                if (UserNationality != null && !UserNationality.equals("")) {
                                    map.put(mCurrentUser.getUid() + "/nationality", UserNationality);
                                }

                                if (UserGender != null && !UserGender.equals("")) {
                                    map.put(mCurrentUser.getUid() + "/gender", UserGender);
                                }

                                if (UserBirthday != null && !UserBirthday.equals("")) {
                                    map.put(mCurrentUser.getUid() + "/birthday", UserBirthday);
                                }

                                if (UserYear != null && !UserYear.equals("")) {
                                    map.put(mCurrentUser.getUid() + "/year", UserYear);
                                }

                                newStudent.child("Users").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Toast.makeText(SplashActivity.this, FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                                        handler.removeCallbacks(mRunnable);
                                        Intent intent = new Intent(EmailVerification.this, ExistingEvent.class);
                                        startActivity(intent);
                                        finish();
                                        System.exit(0);
                                    }
                                });
//                                newStudent.child("Users").updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
//                                    @Override
//                                    public void onComplete(@NonNull Task task) {
//                                        //Toast.makeText(SplashActivity.this, FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
//                                        handler.removeCallbacks(mRunnable);
//                                        Intent intent = new Intent(EmailVerification.this, ExistingEvent.class);
//                                        startActivity(intent);
//                                        finish();
//                                        System.exit(0);
//                                    }
//                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                handler.removeCallbacks(mRunnable);
                Intent intent = new Intent(EmailVerification.this, LoginActivity.class);
                startActivity(intent);
                finish();
                System.exit(0);
            }
            handler.postDelayed(mRunnable, 1000);
        }
    };
}
