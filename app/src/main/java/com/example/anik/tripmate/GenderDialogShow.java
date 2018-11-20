package com.example.anik.tripmate;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by anik on 2/5/18.
 */

public class GenderDialogShow extends Dialog {

    public Context c;
    public Dialog d;

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private static DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    public GenderDialogShow(Context a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gender_custom_dialog);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();
        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        final RadioGroup radioSexGroup = (RadioGroup) findViewById(R.id.radioGrp);

        int selectedid = radioSexGroup.getCheckedRadioButtonId();

        final RadioButton male = (RadioButton) findViewById(R.id.radioM);
        final RadioButton female = (RadioButton) findViewById(R.id.radioF);

        radioSexGroup.clearCheck();

        if(ThreeFragment.genderSelector.getText().toString().equals("Male")) {
            male.setChecked(true);
            female.setChecked(false);
        }

        else if(ThreeFragment.genderSelector.getText().toString().equals("Female")) {
            female.setChecked(true);
            male.setChecked(false);
        }

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreeFragment.genderSelector.setText("Male");
                newStudent.child("gender").setValue("Male");
                male.setChecked(true);
                female.setChecked(false);
            }
        });

        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreeFragment.genderSelector.setText("Female");
                newStudent.child("gender").setValue("Female");
                male.setChecked(false);
                female.setChecked(true);
            }
        });
    }
}
