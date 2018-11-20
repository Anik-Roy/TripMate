package com.example.anik.tripmate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by anik on 2/13/18.
 */

public class TwoFragmentEventDescription extends Fragment {

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userId = "", userEmail = "";

    EditText descriptionText, meetingText, maximumTravelerText;
    Button saveButton;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public TwoFragmentEventDescription() {
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

        //return inflater.inflate(R.layout.fragment_one, container, false);
        View view = inflater.inflate(R.layout.activity_event_description, container, false);

        auth = FirebaseAuth.getInstance();
        newStudent = FirebaseDatabase.getInstance().getReference().child("event");
        mCurrentUser = auth.getCurrentUser();

        descriptionText = (EditText) view.findViewById(R.id.descriptionText);
        meetingText = (EditText) view.findViewById(R.id.meetingText);
        maximumTravelerText = (EditText) view.findViewById(R.id.maximumTravelerText);
        saveButton = (Button) view.findViewById(R.id.saveButton);

        if(!mCurrentUser.getUid().equals(OneFragmentEventInfo.eventRoot)) {
            descriptionText.setFocusable(false);
            meetingText.setFocusable(false);
            maximumTravelerText.setFocusable(false);
            saveButton.setVisibility(View.INVISIBLE);
        }

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.show();
        progressDialog.setTitle("Please wait!");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        newStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(OneFragmentEventInfo.eventRoot).exists()) {

                    final DatabaseReference tempRef = newStudent.child(OneFragmentEventInfo.eventRoot);

                    ValueEventListener valueEventListener = tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                String place = data.getKey();
                                String from = "", to = "", description = "", meetingLocation = "", maximumTraveler = "";
                                Uri uri = null;

                                if(place.equals(OneFragmentEventInfo.placeName)) {

                                    EventInfo eventInfo = data.getValue(EventInfo.class);

                                    try {
                                        if (eventInfo.getDescription() != null)
                                            description = eventInfo.getDescription();
                                        if (eventInfo.getMeetingLocation() != null)
                                            meetingLocation = eventInfo.getMeetingLocation();

                                        if (eventInfo.getMaximumTraver() != null) {
                                            //maximumTravelerText.setText(eventInfo.getMaximumTraver());
                                            maximumTraveler = eventInfo.getMaximumTraver();
                                        }
                                        descriptionText.setText(description);
                                        meetingText.setText(meetingLocation);
                                        maximumTravelerText.setText(maximumTraveler);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            progressDialog.dismiss();
                            //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("photolink", "onCancelled", databaseError.toException());
                            progressDialog.dismiss();
                            //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(), "Event Deleted", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mCurrentUser.getUid().equals(OneFragmentEventInfo.eventRoot)) {

                    Map map = new HashMap();
                    map.put("description", descriptionText.getText().toString());
                    map.put("meetingLocation", meetingText.getText().toString());
                    map.put("maximumTraver", maximumTravelerText.getText().toString());

                    newStudent.child(OneFragmentEventInfo.eventRoot).child(OneFragmentEventInfo.placeName)
                            .updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getContext(), "Saved", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getContext(), "There was an error", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        meetingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
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
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getContext(), data);
                //Log.i(TAG, "Place: " + place.getName());
                //newStudent.child("hometown").setValue(place.getName().toString());
                meetingText.setText(place.getName().toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getContext(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
