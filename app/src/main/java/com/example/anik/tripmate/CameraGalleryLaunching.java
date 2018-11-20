package com.example.anik.tripmate;

/**
 * Created by anik on 1/30/18.
 */

public class CameraGalleryLaunching {
//    FirebaseAuth auth;
//    FirebaseStorage storage;
//    StorageReference storageReference;
//    private DatabaseReference newStudent;
//    FirebaseUser mCurrentUser;
//
//    String userId = "", userEmail = "";
//    String userChoosenTask = "";
//    int REQUEST_CAMERA=1;
//    int SELECT_FILE = 71;
//    ImageView userImage;
//    Button editProfile;
//
//    Bitmap bm = null;
//    private Uri filePath = null;
//    String mCurrentPhotoPath;
//
//    SharedPreferences sharedPreferences, chooseSharedPreferences;
//
//    CacheStore cacheStore;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
//
//        setContentView(R.layout.activity_user_profile);
//
//        userImage = (ImageView) findViewById(R.id.userImage);
//        editProfile = (Button) findViewById(R.id.editProfile);
//
//        sharedPreferences = getSharedPreferences("userProfilePhoto", Context.MODE_PRIVATE);
//        chooseSharedPreferences = getSharedPreferences("chooser", Context.MODE_PRIVATE);
//
//        cacheStore = new CacheStore();
//        Bitmap cacheBitmap = cacheStore.getCacheFile("profilePic");
//
//        if(cacheBitmap != null) {
//          userImage.setImageBitmap(cacheBitmap);
//        }
//
//        auth = FirebaseAuth.getInstance();
//        storage = FirebaseStorage.getInstance();
//
//        storageReference = storage.getReference();
//
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//
//        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//                if (firebaseUser != null) {
//                    userId = firebaseUser.getUid();
//                    userEmail = firebaseUser.getEmail();
//                } else {
//                    Log.i("Email", "No User");
//                }
//            }
//        };
//
//        mCurrentUser = auth.getCurrentUser();
//        newStudent = FirebaseDatabase.getInstance().getReference().child(mCurrentUser.getUid());
//        newStudent.child("email").setValue(mCurrentUser.getEmail());
//
//        final DatabaseReference tempRef = newStudent.child("profilephoto");
//
//        ValueEventListener valueEventListener = tempRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.i("photolink", dataSnapshot.getValue(String.class));
//                Toast.makeText(UserProfile.this, dataSnapshot.getValue(String.class), Toast.LENGTH_LONG).show();
//
//                Glide
//                        .with(getApplicationContext())
//                        .load(dataSnapshot.getValue(String.class))
//                        .asBitmap()
//                        .into(new SimpleTarget<Bitmap>(100,100) {
//                            @Override
//                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//                                userImage.setImageBitmap(resource); // Possibly runOnUiThread()
//                            }
//                        });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w("photolink", "onCancelled", databaseError.toException());
//            }
//        });
//
//        editProfile.setOnClickListener(this);
//    }

//    @Override
//    public void onClick(View v) {
//        if(v.getId() == R.id.editProfile) {
//
//            Intent intent = new Intent(UserProfile.this, EditProfile.class);
//            startActivity(intent);

//            final String[] items = {"Take Photo", "Choose From Gallery", "Cancel"};
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
//            builder.setTitle("Take Photo")
//                    .setItems(items, new DialogInterface.OnClickListener() {
//
//                        boolean result=Utility.checkPermission(UserProfile.this);
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            if(items[which].equals("Take Photo")) {
//                                userChoosenTask="Take Photo";
//                                if(result)
//                                    try {
//                                        //SharedPreferences.Editor editor = chooseSharedPreferences.edit();
//                                        //editor.putString("chooser", "camera").apply();
//                                        cameraIntent();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                            }
//
//                            else if(items[which].equals("Choose From Gallery")) {
//                                userChoosenTask="Choose from Gallery";
//                                if(result) {
//                                    //SharedPreferences.Editor editor = chooseSharedPreferences.edit();
//                                    //editor.putString("chooser", "gallery").apply();
//                                    galleryIntent();
//                                }
//                            }
//
//                            else if(items[which].equals("Cancel")) {
//                                dialog.dismiss();
//                            }
//                        }
//                    }).show();
//        }
//    }

//    private void cameraIntent() throws IOException {
//
//        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
//
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//                //return;
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                Uri photoURI;
//
////                if(Build.VERSION.SDK_INT <= 23) {
////                    photoURI = Uri.fromFile(createImageFile());
////                    Log.i("SDK", String.valueOf(Build.VERSION.SDK_INT));
////
////                }
////                else
//                    photoURI = FileProvider.getUriForFile(UserProfile.this,
//                        BuildConfig.APPLICATION_ID + ".provider",
//                        createImageFile());
//
////                    Toast.makeText(this, photoURI.toString(), Toast.LENGTH_LONG).show();
//
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
//
//                    intent.setClipData(ClipData.newRawUri("", photoURI));
//                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                }
//
//                startActivityForResult(intent, REQUEST_CAMERA);
//
//            }
//        }
//    }
//
//    private void galleryIntent()
//    {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);//
//        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
//                                            grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    if(userChoosenTask.equals("Take Photo"))
//                        try {
//                            cameraIntent();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    else if(userChoosenTask.equals("Choose from Gallery"))
//                        galleryIntent();
//                } else {
//                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == Activity.RESULT_OK) {
//            Toast.makeText(this, "Dishoom", Toast.LENGTH_SHORT).show();
//
//            if (requestCode == SELECT_FILE)
//                onSelectFromGalleryResult(data);
//            else if (requestCode == REQUEST_CAMERA) {
//                onCaptureImageResult(data);
//            }
//        }
//        else {
//            Log.i("Result Code", String.valueOf(resultCode));
//        }
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }
//
//    @SuppressWarnings("deprecation")
//    private void onSelectFromGalleryResult(Intent data) {
//        bm=null;
//        if (data != null) {
//            try {
//                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
//                filePath = data.getData();
//                userImage.setImageBitmap(bm);
//
//                //Toast.makeText(this, "Gallery", Toast.LENGTH_LONG).show();
//                cacheStore.saveCacheFile("profilePic", bm);
//                uploadImage();
//                String path = ImageFilePath.getPath(getApplicationContext(), filePath);
//                //String path = getRealPathFromURI(filePath);
//                mCurrentPhotoPath = path;
//                Log.i("Hello", path);
//                @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString("userProfilePhoto", path).apply();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void onCaptureImageResult(Intent data) {
//
//        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("userProfilePhoto", mCurrentPhotoPath).apply();
//
//        Uri imageUri = Uri.parse(mCurrentPhotoPath);
//        File file = new File(imageUri.getPath());
//        try {
//            InputStream ims = new FileInputStream(file);
//            userImage.setImageBitmap(BitmapFactory.decodeStream(ims));
//            filePath = imageUri;
//            Bitmap photo = BitmapFactory.decodeFile(file.toString());
//
//            cacheStore.saveCacheFile("profilePic", photo);
//
//            uploadImage();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        // ScanFile so it will be appeared on Gallery
//        MediaScannerConnection.scanFile(UserProfile.this,
//                new String[]{imageUri.getPath()}, null,
//                new MediaScannerConnection.OnScanCompletedListener() {
//                    public void onScanCompleted(String path, Uri uri) {
//                    }
//                });
//    }
//
//    private void uploadImage() {
//        if(filePath != null)
//        {
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();
//
//            StorageReference ref = storageReference.child("images").child(filePath.getLastPathSegment());
//            //newStudent.child("profilepic").setValue(filePath);
//
//            ref.putFile(filePath)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                            //newStudent.child("profilephoto").setValue(downloadUrl.toString());
//                            //Toast.makeText(UserProfile.this, downloadUrl.toString(), Toast.LENGTH_LONG).show();
//                            progressDialog.dismiss();
//                            Toast.makeText(UserProfile.this, "Uploaded", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            progressDialog.dismiss();
//                            Toast.makeText(UserProfile.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
//                                    .getTotalByteCount());
//                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
//                        }
//                    });
//        }
//    }
//
//    private File createImageFile() throws IOException {
//        // Create an image file name
//
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//
//        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "TripMate");
//        imagesFolder.mkdirs();
//
//        File image = new File(imagesFolder, "QR_" + timeStamp + ".jpg");
//        //Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
//        return image;
//    }
//
//    public String getRealPathFromURI(Uri contentUri) {
//
//        try {
//            String[] proj = {MediaStore.Images.Media.DATA};
//
//            //This method was deprecated in API level 11
//            //Cursor cursor = managedQuery(contentUri, proj, null, null, null);
//
//            CursorLoader cursorLoader = new CursorLoader(
//                    this,
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
}
