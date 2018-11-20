package com.example.anik.tripmate;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class AddEvent extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent, tempStudent;
    FirebaseUser mCurrentUser;

    EditText addPlaces;
    TextView fromText, toText;
    ImageView image;

    Button from, to, saveEvent;
    String userId = "", userEmail = "";

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int SELECT_FILE = 71;
    int photoSet = 0;
    boolean flag = false, flag2 = false;

    Map<String, Object> eventMap = new HashMap<>();
    String eventRoot = null, place = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();
        //Toast.makeText(getContext(), mCurrentUser.getEmail(), Toast.LENGTH_LONG).show();

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

        newStudent = FirebaseDatabase.getInstance().getReference();
        addPlaces = (EditText) findViewById(R.id.selectPlaces);
        from = (Button) findViewById(R.id.from);
        to = (Button) findViewById(R.id.to);

        fromText = (TextView) findViewById(R.id.fromText);
        toText = (TextView) findViewById(R.id.toText);
        image = (ImageView) findViewById(R.id.imageView);
        saveEvent = (Button) findViewById(R.id.saveEvent);

        from.setOnClickListener(this);
        to.setOnClickListener(this);

        Intent intent = getIntent();
        eventRoot = intent.getStringExtra("eventRoot");
        place = intent.getStringExtra("place");

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setTitle("Please wait!");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        if(eventRoot != null) {
            final DatabaseReference newStudent2 = FirebaseDatabase.getInstance().getReference().child("event");

            newStudent2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.child(eventRoot).exists()) {

                        final DatabaseReference tempRef = newStudent2.child(eventRoot);

                        ValueEventListener valueEventListener = tempRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    String place2 = data.getKey();
                                    String from = "", to = "";
                                    Uri uri = null;

                                    if (place2.equals(place)) {

                                        EventInfo eventInfo = data.getValue(EventInfo.class);

                                        from = eventInfo.getFrom();
                                        to = eventInfo.getTo();
                                        uri = Uri.parse(eventInfo.getImage());

                                        fromText.setText(from);
                                        toText.setText(to);
                                        addPlaces.setText(place2);

                                        fromText.setVisibility(View.VISIBLE);
                                        toText.setVisibility(View.VISIBLE);

                                        //Glide.with(AddEvent.this).load(uri).into(image);

                                        try {
                                            Glide
                                                    .with(AddEvent.this)
                                                    .load(uri)
                                                    .asBitmap()
                                                    .into(new SimpleTarget<Bitmap>(100, 100) {
                                                        @Override
                                                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                                            image.setImageBitmap(resource); // Possibly runOnUiThread()
                                                            photoSet = 1;
                                                            flag2 = true;
                                                        }
                                                    });
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
                    } else {
                        Toast.makeText(AddEvent.this, "Event Deleted", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i("Error", "Database Error");
                }
            });
        }
        else {
            progressDialog.dismiss();
        }
        addPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(AddEvent.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_SHORT).show();

                boolean result = Utility.checkPermission(AddEvent.this);

                if(result) {
                    galleryIntent();
                }
            }
        });

        saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!addPlaces.getText().toString().equals("Select Place")) {
                    if(!fromText.getText().toString().isEmpty()) {

                        if(!toText.getText().toString().isEmpty()) {
                            flag = false;

                            if(photoSet == 1) {

                                tempStudent = FirebaseDatabase.getInstance().getReference().child("event");

                                //newStudent.child("event").child(mCurrentUser.getUid()).child(addPlaces.getText().toString()).child("going").removeValue();

                                Query query = newStudent.child("event").child(mCurrentUser.getUid()).child(addPlaces.getText().toString()).child("going");

                                assert query != null;
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(DataSnapshot dataSnapshot) {
                                         if(dataSnapshot.exists()) {

                                             int cnt = 0 ;

                                             for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                 if(Objects.equals(data.getValue(), mCurrentUser.getUid())) {
                                                     cnt++;

                                                     if(cnt > 1) {
                                                         data.getRef().removeValue();
                                                         cnt--;
                                                     }
                                                 }
                                             }
                                         }
                                         else {
                                             String key = newStudent.child("event").child(mCurrentUser.getUid()).child(addPlaces.getText().toString()).child("going").push().getKey();
                                             eventMap.put("event/" + mCurrentUser.getUid() + "/" + addPlaces.getText().toString() + "/" + "going/" + key, mCurrentUser.getUid());
                                         }
                                     }

                                     @Override
                                     public void onCancelled(DatabaseError databaseError) {

                                     }
                                });

                                eventMap.put("event/"+mCurrentUser.getUid()+"/"+addPlaces.getText().toString()+"/"+"from", fromText.getText().toString());
                                eventMap.put("event/"+mCurrentUser.getUid()+"/"+addPlaces.getText().toString()+"/"+"to", toText.getText().toString());

                                //newStudent.child("event").child(mCurrentUser.getUid()).child(addPlaces.getText().toString()).child("from").setValue(fromText.getText().toString());
                                //newStudent.child("event").child(mCurrentUser.getUid()).child(addPlaces.getText().toString()).child("to").setValue(toText.getText().toString());
                                //newStudent.child("event").child(mCurrentUser.getUid()).child(addPlaces.getText().toString()).child("going").push().setValue(mCurrentUser.getUid());

                                uploadImage();
                            }

                            else {
                                Toast.makeText(getApplicationContext(), "Set a image for your event", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Set your event ending period", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Set your event starting period", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please select a place", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    galleryIntent();
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getApplicationContext(), data);
                //Log.i(TAG, "Place: " + place.getName());
                addPlaces.setText(place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getApplicationContext(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

        else if(requestCode == SELECT_FILE) {
            if(resultCode == RESULT_OK) {
                try {
                    onSelectFromGalleryResult(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Uri filePath;

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) throws IOException {
        Bitmap bm = null;
        if (data != null) {
            filePath = data.getData();
            bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);

            String path = ImageFilePath.getPath(this.getApplicationContext(), filePath);
            image.setImageBitmap(bm);
            if(bm != null)
                photoSet = 1;
        }
    }

    private void uploadImage() {
        if (filePath != null || flag2) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                if(flag2) {
                    newStudent = FirebaseDatabase.getInstance().getReference();
                    newStudent.updateChildren(eventMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddEvent.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddEvent.this, "Failed", Toast.LENGTH_SHORT).show();

                                }
                            });
                }

                else {
                    final StorageReference ref = storageReference.child(mCurrentUser.getUid()).child(UUID.randomUUID() + filePath.getLastPathSegment());

                    ref.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                                    assert downloadUrl != null;
                                    eventMap.put("event/" + mCurrentUser.getUid() + "/" + addPlaces.getText().toString() + "/" + "image", downloadUrl.toString());

                                    newStudent = FirebaseDatabase.getInstance().getReference();
                                    newStudent.updateChildren(eventMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(AddEvent.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }

                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(AddEvent.this, "Failed", Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                    //newStudent.child("event").child(mCurrentUser.getUid()).child(addPlaces.getText().toString()).child("image").setValue(downloadUrl.toString());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddEvent.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });
                }
            }
        }
    }

    Calendar myCalendar = Calendar.getInstance();

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.from:
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String date2 = String.valueOf(dayOfMonth)+"."+String.valueOf(monthOfYear+1)+"."
                                +String.valueOf(year);
                        fromText.setText(date2);
                        fromText.setVisibility(View.VISIBLE);
                    }

                };

                new DatePickerDialog(AddEvent.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)){
                }.show();

                break;

            case R.id.to:
                DatePickerDialog.OnDateSetListener date2 = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String date2 = String.valueOf(dayOfMonth)+"."+String.valueOf(monthOfYear+1)+"."
                                +String.valueOf(year);
                        toText.setText(date2);
                        toText.setVisibility(View.VISIBLE);
                    }
                };

                new DatePickerDialog(AddEvent.this, date2, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)){
                }.show();

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Intent intent = new Intent(this, ExistingEvent.class);
        //startActivity(intent);
        finish();
    }
}
