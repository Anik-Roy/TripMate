package com.example.anik.tripmate;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by anik on 2/6/18.
 */

public class ExistingEvent extends AppCompatActivity{
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userId = "", userEmail = "", detail = "";

    int position;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private NavigationView mNavigationView;

    // FOR NAVIGATION VIEW ITEM TEXT COLOR
    int[][] state = new int[][] {
            new int[] {-android.R.attr.state_enabled}, // disabled
            new int[] {android.R.attr.state_enabled}, // enabled
            new int[] {-android.R.attr.state_checked}, // unchecked
            new int[] { android.R.attr.state_pressed}  // pressed

    };
    int[] color = new int[] {
            Color.BLACK,
            Color.BLACK,
            Color.BLACK,
            Color.BLACK
    };

    CircleImageView userProfileImage;
    Uri imageDownloadUrl = null;
    TextView userName, userEmailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_event);

        toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        ColorStateList csl = new ColorStateList(state, color);
        mNavigationView.setItemTextColor(csl);
        mNavigationView.setItemIconTintList(csl);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mActivityTitle = getTitle().toString();
        setupDrawer();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.homeprofile) {
                    Intent intent = new Intent(ExistingEvent.this, UserProfile.class);
                    startActivity(intent);
                    finish();
                }

                else if(item.getItemId() == R.id.chatMenu) {
                    Intent intent = new Intent(ExistingEvent.this, ChatClass.class);
                    startActivity(intent);
                    finish();
                }

                else if(item.getItemId() == R.id.myFriends) {
                    Intent intent = new Intent(ExistingEvent.this, FriendsActivity.class);
                    startActivity(intent);
                    finish();
                }

                else if(item.getItemId() == R.id.users) {
                    Intent intent = new Intent(ExistingEvent.this, AllUsers.class);
                    startActivity(intent);
                    finish();
                }

                else if(item.getItemId() == R.id.interestedEvent) {
                    //Toast.makeText(ExistingEvent.this, "Clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ExistingEvent.this, InterestedEvent.class);
                    startActivity(intent);
                }

                else if(item.getItemId() == R.id.settings) {
                    Intent intent = new Intent(ExistingEvent.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

        View headerView = mNavigationView.getHeaderView(0);
        userProfileImage = (CircleImageView) headerView.findViewById(R.id.circleView);
        userName = (TextView) headerView.findViewById(R.id.profile_name);
        userEmailText = (TextView) headerView.findViewById(R.id.profile_email);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        storageReference = storage.getReference();

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

        mCurrentUser = auth.getCurrentUser();
        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        newStudent.child("email").setValue(mCurrentUser.getEmail());

        newStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                try {
                    if (userInfo.getName() != null)
                        userName.setText(userInfo.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (userInfo.getEmail() != null)
                        userEmailText.setText(userInfo.getEmail());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (userInfo.getUserImage() != null) {
                        //Iterator it = userInfo.getProfilephoto().entrySet().iterator();

                        //while(it.hasNext()) {
                        //Map.Entry pair = (Map.Entry)it.next();

                        imageDownloadUrl = Uri.parse(userInfo.getUserImage());

                        if (imageDownloadUrl != null) {
                            Glide
                                    .with(ExistingEvent.this)
                                    .load(imageDownloadUrl)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>(100, 100) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                            userProfileImage.setImageBitmap(resource);
                                        }
                                    });
                            //break;
                            //}
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(1);

        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        tabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                position = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new OneFragmentForEvent(), "Existing Events");
        adapter.addFrag(new TwoFragmentForEvent(), "My Events");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if(position == 1)
            inflater.inflate(R.menu.menu_fragment_two_event, menu);
        else
            inflater.inflate(R.menu.menu_search, menu);

        if(position == 0) {
            MenuItem item = menu.findItem(R.id.menuSearch);

            SearchView searchView = (SearchView) item.getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    OneFragmentForEvent.adapter.getFilter().filter(newText);
                    return false;
                }
            });

            MenuItem item2 = menu.findItem(R.id.notification);

            item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent intent = new Intent(ExistingEvent.this, NotificationActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }
        else {
            MenuItem item = menu.findItem(R.id.menuAdd);

            MenuItem item1 = item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent(ExistingEvent.this, AddEvent.class);
                    startActivity(intent);
                    return true;
                }
            });

            MenuItem item2 = menu.findItem(R.id.notification);
            item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent intent = new Intent(ExistingEvent.this, NotificationActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid())
                .child("available").setValue("true");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid())
                .child("available").setValue("false");
        FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid())
                .child("lastSeen").setValue(ServerValue.TIMESTAMP);
    }
}
