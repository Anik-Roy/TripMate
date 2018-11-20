package com.example.anik.tripmate;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.database.FirebaseDatabase;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;
import java.util.List;


public class AllUsers extends AppCompatActivity {

    boolean isPaused;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    Toolbar toolbar;
    static boolean male = false, female = false;
    static String country = "";

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

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    Button maleButton, femaleButton, countryButton;
    static Button searchButton;
    //SeekBar ageBar;
    RangeSeekBar ageBar;
    TextView ageText;

    static int min = 18, max = 60;
    int position;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        FirebaseDatabase.getInstance().getReference().keepSynced(true);
        toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("User's List");

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        ColorStateList csl = new ColorStateList(state, color);
        mNavigationView.setItemTextColor(csl);

        setupDrawer();

        View headerView = mNavigationView.getHeaderView(0);
        maleButton = (Button) headerView.findViewById(R.id.male);
        femaleButton = (Button) headerView.findViewById(R.id.female);
        countryButton = (Button) headerView.findViewById(R.id.country);
        searchButton = (Button) headerView.findViewById(R.id.search);
        ageBar = (RangeSeekBar) headerView.findViewById(R.id.age);
        ageText = (TextView) headerView.findViewById(R.id.ageText);

        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(male) {
                    male = false;
                    //Drawable img = getApplicationContext().getResources().getDrawable( R.drawable.checked );
                    //img.setBounds( 0, 0, 60, 60 );
                    maleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.notchecked, 0);
                }
                else {
                    male = true;
                    Drawable img = getApplicationContext().getResources().getDrawable( R.drawable.notchecked);
                    img.setBounds( 0, 0, 60, 60 );
                    maleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checked, 0);
                }
            }
        });

        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(female) {
                    female = false;
                    //Drawable img = getApplicationContext().getResources().getDrawable( R.drawable.checked );
                    //img.setBounds( 0, 0, 60, 60 );
                    femaleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.notchecked, 0);
                }
                else {
                    female = true;
                    //Drawable img = getApplicationContext().getResources().getDrawable( R.drawable.notchecked);
                    //img.setBounds( 0, 0, 60, 60 );
                    femaleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checked, 0);
                }
            }
        });

        countryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(AllUsers.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        ageBar.setRangeValues(18, 60);
        ageBar.setNotifyWhileDragging(true);

        ageBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                ageText.setText("Age: "+minValue+"-"+maxValue);
                min = (int)minValue;
                max = (int)maxValue;
                ageBar.setRangeValues(18, 60);
                //Toast.makeText(getApplicationContext(), minValue + "-" + maxValue, Toast.LENGTH_LONG).show();
            }
        });

        Toolbar toolbar2 = (Toolbar) headerView.findViewById(R.id.userTool);
        Button reset = (Button) toolbar2.findViewById(R.id.reset);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                male = false;
                female = false;
                min = 18;
                max = 60;
                country = "";
                countryButton.setText("Country:");
                ageText.setText("Age:18-60");
                maleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.notchecked, 0);
                femaleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.notchecked, 0);
                countryButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.notchecked, 0);
                ageBar.resetSelectedValues();
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

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position == 0)
                    OneFragmentNearestUser.setAllUser();
                else
                    TwoFragmentAllUser.setAllUser();
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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                }
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                //Log.i(TAG, "Place: " + place.getName());
                //newStudent.child("hometown").setValue(place.getName().toString());
                countryButton.setText(place.getName().toString());
                countryButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checked, 0);
                country = place.getName().toString();
                Toast.makeText(AllUsers.this, country, Toast.LENGTH_LONG).show();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("Place", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new OneFragmentNearestUser(), "Nearest Users");
        adapter.addFrag(new TwoFragmentAllUser(), "All Users");
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
    protected void onStart() {
        super.onStart();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AllUsers.this, ExistingEvent.class);
        startActivity(intent);
        finish();
    }
}
