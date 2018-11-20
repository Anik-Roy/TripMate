package com.example.anik.tripmate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    static Activity ac;
    static boolean isAlive = false;
    FirebaseAuth mAuth;
    DatabaseReference mRootRef, mFriendDatabase, mMessageRef;
    FirebaseUser mCurrentUser;
    StorageReference storageReference;

    Toolbar mChatToolbar;
    String uid, name;

    TextView displayName, lastSeen;
    EditText messageText;
    CircleImageView friendImage;
    ImageButton sendImage, sendMessage;
    RecyclerView mMessagesRecyclerView;
    MessageAdapter messageAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ac = this;
        isLoadMessage = true;

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        name = intent.getStringExtra("name");

        mChatToolbar = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(mChatToolbar);
        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setTitle(name);

        try {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
            actionBar.setCustomView(action_bar_view);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        storageReference = FirebaseStorage.getInstance().getReference();

        displayName = (TextView) findViewById(R.id.displayName);
        lastSeen = (TextView) findViewById(R.id.lastSeen);
        friendImage = (CircleImageView) findViewById(R.id.frindImage);
        messageText = (EditText) findViewById(R.id.messageText);
        sendImage = (ImageButton) findViewById(R.id.sendImage);
        sendMessage = (ImageButton) findViewById(R.id.sendMessage);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeMessageLayout);
        mMessagesRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        messageAdapter = new MessageAdapter(this, messageList);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessagesRecyclerView.setAdapter(messageAdapter);

        loadMessages();

        mChatToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoadMessage = false;
                finish();
            }
        });

        displayName.setText(name);

        displayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, SingleUserInfo.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

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

        mFriendDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(isAlive) {
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                    try {
                        if (userInfo.getName() != null)
                            displayName.setText(userInfo.getName());
                        else
                            displayName.setText(userInfo.getEmail());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (userInfo.getAvailable() != null) {
                            String online = userInfo.getAvailable();

                            if (online.equals("true")) {
                                lastSeen.setText("online");
                            } else {
                                ConvertTimeStamp convertTimeStamp = new ConvertTimeStamp();
                                Long lastTime = userInfo.getLastSeen();
                                //System.out.println(lastTime);
                                String ls = convertTimeStamp.getTimeAgo(lastTime, ChatActivity.this);
                                lastSeen.setText(ls);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (!ChatActivity.this.isFinishing() && userInfo.getUserImage() != null) {
                            //Toast.makeText(ChatActivity.this, userInfo.getUserImage(), Toast.LENGTH_SHORT).show();
                            String image = userInfo.getUserImage();
                            //Glide.with(ChatActivity.this).load(Uri.parse(image)).placeholder(R.drawable.userimage).into(friendImage);
                            Picasso.with(ChatActivity.this).load(Uri.parse(image)).into(friendImage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(isAlive) {
            mRootRef.child("Chat").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(uid)) {
                        Map<String, Object> chatAddMap = new HashMap<String, Object>();

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (data.getKey().equals(uid)) {
                                for (DataSnapshot d : data.getChildren()) {
                                    if (d.getKey().equals("last")) {
                                        if (!Objects.equals(d.getValue(), mCurrentUser.getUid())) {
                                            //Toast.makeText(ChatActivity.this, d.getValue().toString(), Toast.LENGTH_SHORT).show();
                                            chatAddMap.put("seen", true);
                                            chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                                            Map<String, Object> chatUserMap = new HashMap<String, Object>();
                                            chatUserMap.put("Chat" + "/" + mCurrentUser.getUid() + "/" + uid, chatAddMap);
                                            chatUserMap.put("Chat" + "/" + uid + "/" + mCurrentUser.getUid(), chatAddMap);

                                            mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError != null) {
                                                        Log.d("chat_log", databaseError.getMessage());
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
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
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUser.getUid()).child(uid);
        Query query = messageRef.orderByKey().endAt(mLastKey).limitToLast(20);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(isAlive) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    String messageKey = dataSnapshot.getKey();
                    assert messages != null;
                    messages.setKey(messageKey);

                    if (!mPrevKey.equals(messageKey)) {
                        try {
                            if (!messages.getFrom().equals(mCurrentUser.getUid())) {
                                messages.setSeen(true);
                                //messageList.add(itemPosition++, messages);

                                String currentUserRef = "messages/" + mCurrentUser.getUid() + "/" + uid;
                                String chatUserRef = "messages/" + uid + "/" + mCurrentUser.getUid();

                                Map<String, java.io.Serializable> messageMap = new HashMap<String, java.io.Serializable>();
                                messageMap.put("message", messages.getMessage());
                                messageMap.put("seen", true);
                                messageMap.put("type", messages.getType());
                                messageMap.put("time", messages.getTime());
                                messageMap.put("from", messages.getFrom());
                                messageMap.put("to", messages.getTo());
                                messageMap.put("key", messageKey);

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put(currentUserRef + "/" + messageKey, messageMap);
                                userMap.put(chatUserRef + "/" + messageKey, messageMap);

                                mRootRef.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.d("message_log", databaseError.getMessage().toString());
                                        }
                                    }
                                });

                                //messageList.add(messages);
                                //messageAdapter.notifyDataSetChanged();
                                //mMessagesRecyclerView.scrollToPosition(messageList.size() - 1);
                                //mSwipeRefreshLayout.setRefreshing(false);
                                //Toast.makeText(ChatActivity.this, String.valueOf(messageList.size()), Toast.LENGTH_LONG).show();

                            } else {
                                //messageList.add(messages);
                                //messageAdapter.notifyDataSetChanged();
                                //mMessagesRecyclerView.scrollToPosition(messageList.size() - 1);
                                //mSwipeRefreshLayout.setRefreshing(false);
                                //Toast.makeText(ChatActivity.this, String.valueOf(messageList.size()), Toast.LENGTH_LONG).show();
                                //messageList.add(itemPosition++, messages);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(isAlive) {
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
        final DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUser.getUid()).child(uid);
        Query query = messageRef.limitToLast(mCurrentPage*total_item_to_load);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(isAlive) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    String messageKey = dataSnapshot.getKey();
                    itemPosition++;

                    if (itemPosition == 1) {
                        mLastKey = dataSnapshot.getKey();
                        mPrevKey = mLastKey;
                    }

                    try {
                        //if (!messages.getFrom().equals(mCurrentUser.getUid())) {
                        //  messages.setSeen(true);
                        //}

                        if (!messages.getFrom().equals(mCurrentUser.getUid())) {
                            messages.setSeen(true);

                            String currentUserRef = "messages/" + mCurrentUser.getUid() + "/" + uid;
                            String chatUserRef = "messages/" + uid + "/" + mCurrentUser.getUid();

                            Map<String, java.io.Serializable> messageMap = new HashMap<String, java.io.Serializable>();
                            messageMap.put("message", messages.getMessage());
                            messageMap.put("seen", true);
                            messageMap.put("type", messages.getType());
                            messageMap.put("time", messages.getTime());
                            messageMap.put("from", messages.getFrom());
                            messageMap.put("to", messages.getTo());
                            messageMap.put("key", messageKey);

                            Map<String, Object> userMap = new HashMap<String, Object>();
                            userMap.put(currentUserRef + "/" + messageKey, messageMap);
                            userMap.put(chatUserRef + "/" + messageKey, messageMap);

                            mRootRef.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        Log.d("message_log", databaseError.getMessage().toString());
                                    }
                                }
                            });

                            messageList.add(messages);
                            messageAdapter.notifyDataSetChanged();
                            mMessagesRecyclerView.scrollToPosition(messageList.size() - 1);
                            mSwipeRefreshLayout.setRefreshing(false);
                            //Toast.makeText(ChatActivity.this, String.valueOf(messageList.size()), Toast.LENGTH_LONG).show();
                        } else {
                            messageList.add(messages);
                            messageAdapter.notifyDataSetChanged();
                            mMessagesRecyclerView.scrollToPosition(messageList.size() - 1);
                            mSwipeRefreshLayout.setRefreshing(false);
                            //Toast.makeText(ChatActivity.this, String.valueOf(messageList.size()), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(isAlive) {
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

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.sendMessage) {
            sendMessage();
        }

        else if(view.getId() == R.id.sendImage) {
            boolean result = Utility.checkPermission(ChatActivity.this);

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
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
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
            final ProgressDialog progressDialog = new ProgressDialog(this);
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

                                mRootRef.child("Chat").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //if(!dataSnapshot.hasChild(uid)) {
                                            Map chatAddMap = new HashMap();
                                            chatAddMap.put("seen", false);
                                            chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                                            chatAddMap.put("last", mCurrentUser.getUid());

                                            Map chatUserMap = new HashMap();
                                            chatUserMap.put("Chat"+"/"+mCurrentUser.getUid()+"/"+uid, chatAddMap);
                                            chatUserMap.put("Chat"+"/"+uid+"/"+mCurrentUser.getUid(), chatAddMap);

                                            mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if(databaseError != null) {
                                                        Log.d("chat_log", databaseError.getMessage().toString());
                                                    }
                                                }
                                            });
                                        //}
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                String currentUserRef = "messages/"+mCurrentUser.getUid()+"/"+uid;
                                String chatUserRef = "messages/"+uid+"/"+mCurrentUser.getUid();

                                DatabaseReference userMessagePush = mRootRef.child("messages").child(mCurrentUser.getUid()).child(uid).push();
                                String pushId = userMessagePush.getKey();

                                Map messageMap = new HashMap();
                                messageMap.put("message", downloadUrl.toString());
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from", mCurrentUser.getUid());
                                messageMap.put("to", uid);
                                messageMap.put("key", pushId);

                                Map userMap = new HashMap();
                                userMap.put(currentUserRef+"/"+pushId, messageMap);
                                userMap.put(chatUserRef+"/"+pushId, messageMap);

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
                                Toast.makeText(ChatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        String currentUserRef = "messages/"+mCurrentUser.getUid()+"/"+uid;
        String chatUserRef = "messages/"+uid+"/"+mCurrentUser.getUid();

        DatabaseReference userMessagePush = mRootRef.child("messages").child(mCurrentUser.getUid()).child(uid).push();
        String pushId = userMessagePush.getKey();

        if(!TextUtils.isEmpty(message)) {

            mRootRef.child("Chat").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //if(!dataSnapshot.hasChild(uid)) {
                        Map<String, Object> chatAddMap = new HashMap<String, Object>();
                        chatAddMap.put("seen", false);
                        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                        chatAddMap.put("last", mCurrentUser.getUid());

                        Map<String, Object> chatUserMap = new HashMap<>();
                        chatUserMap.put("Chat"+"/"+mCurrentUser.getUid()+"/"+uid, chatAddMap);
                        chatUserMap.put("Chat"+"/"+uid+"/"+mCurrentUser.getUid(), chatAddMap);

                        mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null) {
                                    Log.d("chat_log", databaseError.getMessage().toString());
                                }
                            }
                        });
                    //}
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUser.getUid());
            messageMap.put("to", uid);
            messageMap.put("key", pushId);
            messageText.setText("");

            Map<String, Object> userMap = new HashMap<>();
            userMap.put(currentUserRef+"/"+pushId, messageMap);
            userMap.put(chatUserRef+"/"+pushId, messageMap);

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
    protected void onStart() {
        super.onStart();
        isAlive = true;
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
    protected void onStop() {
        super.onStop();
        isAlive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        isLoadMessage = false;
        //Intent intent = new Intent(ChatActivity.this, ExistingEvent.class);
        //startActivity(intent);
        finish();
    }
}
