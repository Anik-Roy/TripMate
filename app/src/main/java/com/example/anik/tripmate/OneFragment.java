package com.example.anik.tripmate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by anik on 1/28/18.
 */

public class OneFragment extends Fragment implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userChoosenTask;
    String mCurrentPhotoPath;
    private Uri filePath = null;
    String userId = "", userEmail = "";

    int REQUEST_CAMERA = 1;
    int SELECT_FILE = 71;

    static int check = 0;

    Button uploadNewImage;
    Button deletePhoto;

    List<PhotoAlbum> albumList = new ArrayList<>();
    public static List<String> keyIdForFirebaseDatabaseImages = new ArrayList<>();
    RecyclerView recyclerView;
    private AlbumsAdapter adapter;

    public OneFragment() {
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

        View view = inflater.inflate(R.layout.fragment_one, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        albumList = new ArrayList<>();
        adapter = new AlbumsAdapter(getActivity(), albumList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(0), true)); // dpToPx 10 chilo
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        uploadNewImage = (Button) view.findViewById(R.id.addNewImage);
        uploadNewImage.setOnClickListener(this);

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        newStudent.child("email").setValue(mCurrentUser.getEmail());

        newStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("profilephoto").exists()) {
                    final DatabaseReference tempRef = newStudent.child("profilephoto");

                    ValueEventListener valueEventListener = tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            albumList.clear();

                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                Uri uri = Uri.parse(data.getValue(String.class));

                                PhotoAlbum photo = new PhotoAlbum(uri);
                                albumList.add(photo);
                                adapter.notifyDataSetChanged();
                                keyIdForFirebaseDatabaseImages.add(data.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("photolink", "onCancelled", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });


        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addNewImage) {
            final String[] items = {"Take Photo", "Choose From Gallery", "Cancel"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Take Photo")
                    .setItems(items, new DialogInterface.OnClickListener() {

                        boolean result = Utility.checkPermission(getContext());

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (items[which].equals("Take Photo")) {
                                userChoosenTask = "Take Photo";
                                if (result)
                                    try {
                                        cameraIntent();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            } else if (items[which].equals("Choose From Gallery")) {
                                userChoosenTask = "Choose from Gallery";
                                if (result) {
                                    galleryIntent();
                                }
                            } else if (items[which].equals("Cancel")) {
                                dialog.dismiss();
                            }
                        }
                    }).show();
        }
    }

    private void cameraIntent() throws IOException {

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI;

//                if(Build.VERSION.SDK_INT <= 23) {
//                    photoURI = Uri.fromFile(createImageFile());
//                    Log.i("SDK", String.valueOf(Build.VERSION.SDK_INT));
//                }
//                else
                photoURI = FileProvider.getUriForFile(getContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        createImageFile());

                //Toast.makeText(this, photoURI.toString(), Toast.LENGTH_LONG).show();

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    intent.setClipData(ClipData.newRawUri("", photoURI));
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
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
                    if (userChoosenTask.equals("Take Photo"))
                        try {
                            cameraIntent();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    else if (userChoosenTask.equals("Choose from Gallery"))
                        galleryIntent();
                } else {
                    Toast.makeText(getActivity(), "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        } else {
            Log.i("Result Code", String.valueOf(resultCode));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            filePath = data.getData();
            String path = ImageFilePath.getPath(getActivity().getApplicationContext(), filePath);

            PhotoAlbum album = new PhotoAlbum(filePath);

            if(check == 1) {
                albumList.clear();
                check = 0;
            }

            albumList.add(album);
            adapter.notifyDataSetChanged();
            uploadImage();
            mCurrentPhotoPath = path;

        }
    }

    private void onCaptureImageResult(Intent data) {

        Uri imageUri = Uri.parse(mCurrentPhotoPath);
        File file = new File(imageUri.getPath());
        try {
            InputStream ims = new FileInputStream(file);

            filePath = imageUri;
            String path = ImageFilePath.getPath(getActivity().getApplicationContext(), filePath);

            PhotoAlbum album = new PhotoAlbum(filePath);

            if(check == 1) {
                albumList.clear();
                check = 0;
            }

            albumList.add(album);
            adapter.notifyDataSetChanged();
            uploadImage();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // ScanFile so it will be appeared on Gallery
        MediaScannerConnection.scanFile(getContext(),
                new String[]{imageUri.getPath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    private void uploadImage() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child(mCurrentUser.getUid()).child(filePath.getLastPathSegment());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                            assert downloadUrl != null;

                            newStudent.child("profilephoto").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        DatabaseReference photoRef = newStudent.child("profilephoto").push();
                                        photoRef.setValue(downloadUrl.toString());

                                        //newStudent.child("profilephoto").push().setValue(downloadUrl.toString());
                                        keyIdForFirebaseDatabaseImages.add(photoRef.getKey());

                                        newStudent.child("uid").setValue(mCurrentUser.getUid());

                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        newStudent.child("userImage").setValue(downloadUrl.toString());
                                        DatabaseReference photoRef = newStudent.child("profilephoto").push();
                                        photoRef.setValue(downloadUrl.toString());

                                        //newStudent.child("profilephoto").push().setValue(downloadUrl.toString());
                                        keyIdForFirebaseDatabaseImages.add(photoRef.getKey());

                                        newStudent.child("uid").setValue(mCurrentUser.getUid());

                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
//                            DatabaseReference photoRef = newStudent.child("profilephoto").push();
//                            photoRef.setValue(downloadUrl.toString());
//
//                            //newStudent.child("profilephoto").push().setValue(downloadUrl.toString());
//                            keyIdForFirebaseDatabaseImages.add(photoRef.getKey());
//
//                            newStudent.child("uid").setValue(mCurrentUser.getUid());
//                            newStudent.child("filepath").setValue(filePath.getLastPathSegment());
//
//                            progressDialog.dismiss();
//                            Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "TripMate");
        imagesFolder.mkdirs();

        File image = new File(imagesFolder, "QR_" + timeStamp + ".jpg");
        //Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

//    public String getRealPathFromURI(Uri contentUri) {
//
//        try {
//            String[] proj = {MediaStore.Images.Media.DATA};
//
//            //This method was deprecated in API level 11
//            //Cursor cursor = managedQuery(contentUri, proj, null, null, null);
//
//            CursorLoader cursorLoader = new CursorLoader(
//                    getContext(),
//                    contentUri, proj, null, null, null);
//            Cursor cursor = cursorLoader.loadInBackground();
//
//            int column_index =
//                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            return cursor.getString(column_index);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void onResume() {
        super.onResume();
        newStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("profilephoto").exists()) {
                    final DatabaseReference tempRef = newStudent.child("profilephoto");

                    ValueEventListener valueEventListener = tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            albumList.clear();

                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                Uri uri = Uri.parse(data.getValue(String.class));

                                PhotoAlbum photo = new PhotoAlbum(uri);
                                albumList.add(photo);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("photolink", "onCancelled", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });
    }
}