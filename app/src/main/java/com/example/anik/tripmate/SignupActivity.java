package com.example.anik.tripmate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class SignupActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPassword, nationality;
    private static EditText date;
    static EditText gender;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference newStudent;
    private FirebaseUser mCurrentUser;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    static String y, getDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = (Button) this.findViewById(R.id.sign_in_button);
        btnSignUp = (Button) this.findViewById(R.id.sign_up_button);
        inputName = (EditText) this.findViewById(R.id.signupName);
        nationality = (EditText) this.findViewById(R.id.nationality);
        inputEmail = (EditText) this.findViewById(R.id.email);
        inputPassword = (EditText) this.findViewById(R.id.password);
        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        btnResetPassword = (Button) this.findViewById(R.id.btn_reset_password);
        gender = (EditText) this.findViewById(R.id.gender);
        date = (EditText) this.findViewById(R.id.birthday);

        gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenderDialog genderDialog = new GenderDialog(SignupActivity.this);
                genderDialog.show();
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new SignupActivity.DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        nationality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(SignupActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                final String userName = inputName.getText().toString();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(getApplicationContext(), "Enter User Name", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_LONG).show();
                    return;
                }

                String bd = date.getText().toString().trim();

                if(bd.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Give your birthdate", Toast.LENGTH_LONG).show();
                    return;
                }

                String gd = gender.getText().toString().trim();

                if(gd.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Give your gender", Toast.LENGTH_LONG).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user

                if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
//                                        newStudent = FirebaseDatabase.getInstance().getReference();
//                                        mCurrentUser = auth.getCurrentUser();
//
//                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
//
//                                        Map map = new HashMap();
//                                        map.put(mCurrentUser.getUid() + "/locationShare", "false");
//                                        map.put(mCurrentUser.getUid() + "/device_token", deviceToken);
//                                        map.put(mCurrentUser.getUid() + "/uid", mCurrentUser.getUid());
//                                        map.put(mCurrentUser.getUid() + "/email", mCurrentUser.getEmail());
//                                        map.put(mCurrentUser.getUid() + "/registeredFrom", ServerValue.TIMESTAMP);
//
//                                        newStudent.child("Users").updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
//                                            @Override
//                                            public void onComplete(@NonNull Task task) {
//
//                                            }
//                                        });

                                        //startActivity(new Intent(SignupActivity.this, ExistingEvent.class));

                                        Intent intent = new Intent(SignupActivity.this, EmailVerification.class);
                                        intent.putExtra("UserName", userName);
                                        intent.putExtra("UserNationality", nationality.getText().toString());
                                        intent.putExtra("UserGender", gender.getText().toString());
                                        intent.putExtra("UserBirthday", date.getText().toString());
                                        intent.putExtra("UserYear", y);
                                        startActivity(intent);
                                        //startActivity(new Intent(SignupActivity.this, EmailVerification.class));
                                        finish();
                                    }
                                }
                            });
                }

                else {
                    Toast.makeText(SignupActivity.this, "Email address is not valid!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                //Log.i(TAG, "Place: " + place.getName());
                //newStudent.child("hometown").setValue(place.getName().toString());
                nationality.setText(place.getName().toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        static String ubd;

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            y = String.valueOf(year);

            ubd = String.valueOf(day)+"."+String.valueOf(month+1)+"."
                    +String.valueOf(year);

            date.setText(ubd);

            //if(!date.getText().equals("")) {
            //newStudent.child("birthday").setValue(date.getText().toString());
            //}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
