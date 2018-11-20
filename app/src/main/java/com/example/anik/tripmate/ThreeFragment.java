package com.example.anik.tripmate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
//import com.tsongkha.spinnerdatepicker.DatePicker;
//import com.tsongkha.spinnerdatepicker.DatePickerDialog;
//import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.util.Calendar;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by anik on 1/28/18.
 */

public class ThreeFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private static DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userId = "", userEmail = "";
    static TextInputEditText date, homeTown;
    static EditText name;
    static TextInputEditText genderSelector;
    static EditText occupation;
    static EditText website;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public ThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_three, container, false);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    userId = firebaseUser.getUid();
                    userEmail = firebaseUser.getEmail();
                } else {
                    Log.i("Email", "No User");
                }
            }
        };

        name = (EditText) view.findViewById(R.id.name);
        occupation = (EditText) view.findViewById(R.id.occupation);
        website = (EditText) view.findViewById(R.id.website);
        genderSelector = (TextInputEditText) view.findViewById(R.id.genderSelector);
        date = (TextInputEditText) view.findViewById(R.id.birthdayPicker);
        homeTown = (TextInputEditText) view.findViewById(R.id.homeTown);

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        newStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    UserInfo userInfo = snapshot.getValue(UserInfo.class);

                    if(userInfo.getName() != null) {
                        name.setText(userInfo.getName());
                    }

                    if(userInfo.getOccupation() != null) {
                        occupation.setText(userInfo.getOccupation());
                    }

                    if(userInfo.getWebsite() != null) {
                        website.setText(userInfo.getWebsite());
                    }

                    if(userInfo.getBirthday() != null) {
                        date.setText(userInfo.getBirthday());
                    }

                    if(userInfo.getHometown() != null) {
                        homeTown.setText(userInfo.getHometown());
                    }

                    if(userInfo.getGender() != null) {
                        genderSelector.setText(userInfo.getGender());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        homeTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        genderSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenderDialogShow genderDialog = new GenderDialogShow(getContext());
                genderDialog.show();

                if(!genderSelector.getText().toString().equals("")) {
                    newStudent.child("gender").setValue(genderSelector.getText().toString());
                }
            }
        });

        return view;
        //return inflater.inflate(R.layout.fragment_three, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getContext(), data);
                //Log.i(TAG, "Place: " + place.getName());
                //newStudent.child("hometown").setValue(place.getName().toString());
                homeTown.setText(place.getName().toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getContext(), data);
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

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            date.setText(String.valueOf(day)+"."+String.valueOf(month+1)+"."
                                            +String.valueOf(year));

            //if(!date.getText().equals("")) {
                //newStudent.child("birthday").setValue(date.getText().toString());
            //}
        }
    }
}
