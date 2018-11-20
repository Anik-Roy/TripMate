package com.example.anik.tripmate;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ybs.countrypicker.CountryPicker;
import com.ybs.countrypicker.CountryPickerListener;

/**
 * Created by anik on 1/28/18.
 */

public class TwoFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userId = "", userEmail = "", country = "", languageCanSpeak= "";

    static EditText visitedCountryList;

    static EditText about;
    static EditText language;
    static EditText interest;
    static EditText travel;

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_two, container, false);


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

        //TextInputLayout aboutMeLayout = (TextInputLayout) view.findViewById(R.id.aboutMeLayout);

        //aboutMeLayout.setHintTextAppearance(R.style.GreenTextInputLayout);

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        newStudent.child("email").setValue(mCurrentUser.getEmail());

        Button visitedCountry = (Button) view.findViewById(R.id.countryEdit);
        visitedCountryList = (EditText) view.findViewById(R.id.countryVisitedList);

        about = (EditText) view.findViewById(R.id.aboutMe);
        language = (EditText) view.findViewById(R.id.languageEdit);
        interest = (EditText) view.findViewById(R.id.interestEdit);
        travel = (EditText) view.findViewById(R.id.travelEdit);

        newStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    UserInfo userInfo = snapshot.getValue(UserInfo.class);

                    if(userInfo.getVisitedCountry() != null) {
                        visitedCountryList.setText(userInfo.getVisitedCountry());
                        visitedCountryList.setVisibility(View.VISIBLE);
                        country = visitedCountryList.getText().toString();
                    }

                    if(userInfo.getAbout() != null) {
                        about.setText(userInfo.getAbout());
                    }

                    if(userInfo.getLanguage() != null) {
                        language.setText(userInfo.getLanguage());
                    }

                    if(userInfo.getInterest() != null) {
                        interest.setText(userInfo.getInterest());
                    }

                    if(userInfo.getTravel() != null) {
                        travel.setText(userInfo.getTravel());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });


//        LayoutInflater inflater1 = getLayoutInflater();
//        View anotherView = inflater1.inflate(R.layout.activity_edit_profile, null);

//        Toolbar toolbar = (Toolbar) anotherView.findViewById(R.id.toolbar);
//
//        Button button = (Button) toolbar.findViewById(R.id.saveButton);
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!about.getText().toString().equals("")) {
//                    newStudent.child("about").setValue(about.getText().toString());
//                }
//                if(!interest.getText().toString().equals("")) {
//                    newStudent.child("interest").setValue(interest.getText().toString());
//                }
//                if(!travel.getText().toString().equals("")) {
//                    newStudent.child("travel").setValue(travel.getText().toString());
//                }
//                if(!language.getText().toString().equals("")) {
//                    newStudent.child("language").setValue(language.getText().toString());
//                }
//                Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
//            }
//        });

        visitedCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for this fragment
                CountryPicker picker = CountryPicker.newInstance("Select Country");  // dialog title
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                        //newStudent.child("visitedCountry").push().setValue(name);

                        if(country.equals(""))
                            country += name;
                        else
                            country += ", "+name;
                        visitedCountryList.setText(country);
                        visitedCountryList.setVisibility(View.VISIBLE);

                        Toast.makeText(getContext(), "Country Added", Toast.LENGTH_SHORT).show();
                    }
                });

                picker.show(getActivity().getSupportFragmentManager(), "COUNTRY_PICKER");

            }
        });

        return view;
    }
}
