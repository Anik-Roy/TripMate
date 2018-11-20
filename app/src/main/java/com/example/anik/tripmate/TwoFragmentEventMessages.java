package com.example.anik.tripmate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.v4.provider.FontsContractCompat.FontRequestCallback.RESULT_OK;

/**
 * Created by anik on 2/13/18.
 */

public class TwoFragmentEventMessages extends Fragment implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userId = "", userEmail = "";

    static Activity ac;
    FirebaseAuth mAuth;
    DatabaseReference mRootRef, mFriendDatabase, mMessageRef;

    Toolbar mChatToolbar;
    String uid, name;

    TextView displayName, lastSeen;
    EditText messageText;
    CircleImageView friendImage;
    ImageButton sendImage, sendMessage;
    RecyclerView mMessagesRecyclerView;
    MessageGroupAdapter messageAdapter;
    List<Messages> messageList = new ArrayList<>();
    LinearLayoutManager mLinearLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private static final int total_item_to_load = 20;
    private int mCurrentPage = 1;
    private int itemPosition = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private static int SELECT_FILE = 1;
    static boolean isLoadMessage = true;
    int pos;

    public TwoFragmentEventMessages() {
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
        View view = inflater.inflate(R.layout.activity_event_messages, container, false);

        ac = getActivity();
        isLoadMessage = true;

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        //mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        storageReference = FirebaseStorage.getInstance().getReference();

        displayName = (TextView) view.findViewById(R.id.displayName);
        lastSeen = (TextView) view.findViewById(R.id.lastSeen);
        friendImage = (CircleImageView) view.findViewById(R.id.frindImage);
        messageText = (EditText) view.findViewById(R.id.messageText);
        sendImage = (ImageButton) view.findViewById(R.id.sendImage);
        sendMessage = (ImageButton) view.findViewById(R.id.sendMessage);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeMessageLayout);
        mMessagesRecyclerView = (RecyclerView) view.findViewById(R.id.messageRecyclerView);
        messageAdapter = new MessageGroupAdapter(getContext(), messageList);

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessagesRecyclerView.setAdapter(messageAdapter);

        mMessagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    pos = mMessagesRecyclerView.getAdapter().getItemCount() - 1;
                    if(pos < 0)
                        pos = 0;
                    mMessagesRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMessagesRecyclerView.smoothScrollToPosition(pos);
                        }
                    }, 100);
                }
            }
        });

        sendMessage.setOnClickListener(this);
        sendImage.setOnClickListener(this);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage += 1;
                itemPosition = 0;
                loadMoreMessages();
            }
        });

        loadMessages();

        return view;
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("GroupEvent").child(EventFullInfo.eventRoot).
                child(EventFullInfo.placeName).child("messages");

        Query query = messageRef.orderByKey().endAt(mLastKey).limitToLast(20);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                assert messages != null;
                messages.setKey(messageKey);

                if (!mPrevKey.equals(messageKey)) {
                    messageList.add(itemPosition++, messages);
                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPosition == 1) {
                    mLastKey = messageKey;
                }
                messageAdapter.notifyDataSetChanged();
                //mMessagesRecyclerView.scrollToPosition(messageList.size() - 1);
                mSwipeRefreshLayout.setRefreshing(false);
                mLinearLayoutManager.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int sz = messageList.size();
                Messages newMessage = dataSnapshot.getValue(Messages.class);

                for (int i = 0; i < sz; i++) {
                    Messages m = messageList.get(i);

                    if (m.getKey().equals(dataSnapshot.getKey())) {
                        //Messages mm = messageAdapter.getItem(i);
                        messageList.add(i, newMessage);
                        messageAdapter.notifyDataSetChanged();
                        mMessagesRecyclerView.scrollToPosition(messageList.size() - 1);
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {
        final DatabaseReference messageRef = mRootRef.child("GroupEvent").child(EventFullInfo.eventRoot).
                                child(EventFullInfo.placeName).child("messages");

        Query query = messageRef.limitToLast(mCurrentPage*total_item_to_load);

        //if(isLoadMessage) {
            query.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    String messageKey = dataSnapshot.getKey();
                    itemPosition++;

                    if (itemPosition == 1) {
                        mLastKey = dataSnapshot.getKey();
                        mPrevKey = mLastKey;
                    }

                    try {
                        messageList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        mMessagesRecyclerView.scrollToPosition(messageList.size() - 1);
                        mSwipeRefreshLayout.setRefreshing(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    int sz = messageList.size();
                    Messages newMessage = dataSnapshot.getValue(Messages.class);

                    for (int i = 0; i < sz; i++) {
                        Messages m = messageList.get(i);

                        if (m.getKey().equals(dataSnapshot.getKey())) {
                            //Messages mm = messageAdapter.getItem(i);
                            //messageList.remove(i);
                            messageList.set(i, newMessage);
                            messageAdapter.notifyDataSetChanged();
                            mMessagesRecyclerView.scrollToPosition(messageList.size() - 1);
                            mSwipeRefreshLayout.setRefreshing(false);
                            return;
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        //}
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.sendMessage) {
            sendMessage();
        }

        else if(view.getId() == R.id.sendImage) {
            boolean result = Utility.checkPermission(getContext());

            if(result)
                sendImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    sendImage();
                } else {
                    Toast.makeText(getContext(), "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_FILE) {
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
            uploadImage();
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child(mCurrentUser.getUid()).child(UUID.randomUUID() + filePath.getLastPathSegment());

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                                assert downloadUrl != null;

                                String currentUserRef = "GroupEvent/"+EventFullInfo.eventRoot+"/"+EventFullInfo.placeName+"/messages";

                                DatabaseReference userMessagePush = mRootRef.child("GroupEvent").child(EventFullInfo.eventRoot).child(EventFullInfo.placeName)
                                        .child("messages").push();
                                String pushId = userMessagePush.getKey();

                                Map messageMap = new HashMap();
                                messageMap.put("message", downloadUrl.toString());
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from", mCurrentUser.getUid());
                                messageMap.put("key", pushId);

                                Map userMap = new HashMap();
                                userMap.put(currentUserRef+"/"+pushId, messageMap);

                                mRootRef.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(databaseError != null) {
                                            Log.d("message_log", databaseError.getMessage().toString());
                                        }
                                    }
                                });
                                progressDialog.dismiss();
                            }})

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void sendImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void sendMessage() {
        String message = messageText.getText().toString();
        String currentUserRef = "GroupEvent/"+EventFullInfo.eventRoot+"/"+EventFullInfo.placeName+"/messages";

        DatabaseReference userMessagePush = mRootRef.child("GroupEvent").child(EventFullInfo.eventRoot).child(EventFullInfo.placeName)
                .child("messages").push();
        String pushId = userMessagePush.getKey();

        if(!TextUtils.isEmpty(message)) {

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUser.getUid());
            messageMap.put("key", pushId);
            messageText.setText("");

            Map<String, Object> userMap = new HashMap<>();
            userMap.put(currentUserRef+"/"+pushId, messageMap);

            mRootRef.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null) {
                        Log.d("message_log", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
