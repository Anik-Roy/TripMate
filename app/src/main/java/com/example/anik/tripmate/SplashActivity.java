package com.example.anik.tripmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anik on 1/27/18.
 */

public class SplashActivity extends Activity {
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    private DatabaseReference newStudent;

    //ProgressBar progressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        //progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        //progressBar.setVisibility(View.VISIBLE);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            //Toast.makeText(SplashActivity.this, "Not null", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {

                        if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                            newStudent = FirebaseDatabase.getInstance().getReference();
                            mCurrentUser = mAuth.getCurrentUser();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            Map map = new HashMap();
                            map.put(mCurrentUser.getUid() + "/device_token", deviceToken);
                            map.put(mCurrentUser.getUid() + "/uid", mCurrentUser.getUid());
                            map.put(mCurrentUser.getUid() + "/email", mCurrentUser.getEmail());

                            newStudent.child("Users").updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()) {
                                        Intent intent = new Intent(SplashActivity.this, ExistingEvent.class);
                                        startActivity(intent);
                                        //progressBar.setVisibility(View.GONE);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(SplashActivity.this, "Please reload the app again.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            Intent intent = new Intent(SplashActivity.this, EmailVerification.class);
                            startActivity(intent);
                            //progressBar.setVisibility(View.GONE);
                            finish();
                        }
                    }

                    else {

                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        //progressBar.setVisibility(View.GONE);
                        finish();
                    }
                }
            });
        }

        else {

            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            //progressBar.setVisibility(View.GONE);
            finish();
        }
    }
}
