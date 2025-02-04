package com.example.planit;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private static final int CAMERA_REQUEST = 1888;
    private static final int CAPTURE_PERMISSIONS_CODE = 100;
    private static final int MY_CAL_REQ = 101;
    private static final int READ_GALLERY_PERMISSION = 500;
    private static final int REQUEST_CHOOSE_IMAGE = 1002;

    private FloatingActionButton fabCreate;
    private FloatingActionButton fabCamera;
    private FloatingActionButton fabGallery;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Uri imageUri;

    private Integer imageMaxWidth;
    private Integer imageMaxHeight;

    private boolean isFABOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabCreate = findViewById(R.id.fab);
        fabCamera = findViewById(R.id.fab1);
        fabGallery = findViewById(R.id.fab2);

        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAPTURE_PERMISSIONS_CODE);
                } else {
                    startCameraIntentForResult();
                }
            }
        });

        fabGallery.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_GALLERY_PERMISSION);
                } else {
                    pickImageFromGallery();
                }

            }
        });

        recyclerView = findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(getDataFromEventTable());
        recyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAPTURE_PERMISSIONS_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_LONG).show();
                startCameraIntentForResult();
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_CAL_REQ){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }else{
                Toast.makeText(this, "Calendar Permission Denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == READ_GALLERY_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            startTextDetectionAndPreview();
        }else if(requestCode == REQUEST_CHOOSE_IMAGE && resultCode == AppCompatActivity.RESULT_OK){
            imageUri = data.getData();
            startTextDetectionAndPreview();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_upcomingevent) {

        } else if (id == R.id.nav_maps) {
            // TODO : get a location
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=1600 Amphitheatre Parkway, Mountain+View, California");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

            //Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            //startActivity(intent);
        } else if (id == R.id.nav_pastevents) {
//            long eventID = 208;
//            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
//            Intent intent = new Intent(Intent.ACTION_VIEW)
//                    .setData(uri);
//            startActivity(intent);

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(2019, 11, 19, 7, 30);
            Calendar endTime = Calendar.getInstance();
            endTime.set(2019, 11, 19, 8, 30);
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, "Yoga")
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Group class")
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym");

            startActivity(intent);

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startCameraIntentForResult() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            closeFABMenu();
        }
    }

    private void startTextDetectionAndPreview() {
        try {

            if (imageUri == null) {
                return;
            }

            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Get the dimensions of the View
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) imageBitmap.getWidth() / (float) targetWidth,
                            (float) imageBitmap.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            imageBitmap,
                            (int) (imageBitmap.getWidth() / scaleFactor),
                            (int) (imageBitmap.getHeight() / scaleFactor),
                            true);

            //Write file
            String filename = "event_capture.png";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);

            //Cleanup
            stream.close();
            imageBitmap.recycle();

            //Pop intent
            Intent previewIntent = new Intent(this, TextPreviewActivity.class);
            previewIntent.putExtra("image", filename);
            startActivity(previewIntent);

        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image");
        }
    }

    private void showFABMenu() {
        isFABOpen = true;
        fabCamera.animate().translationY(-getResources().getDimension(R.dimen.standard_65)).setDuration(150);
        fabGallery.animate().translationY(-getResources().getDimension(R.dimen.standard_125)).setDuration(150);
        fabCreate.animate().rotationBy(225).setDuration(150);
    }

    private void closeFABMenu() {
        isFABOpen = false;
        fabCamera.animate().translationY(0).setDuration(150);
        fabGallery.animate().translationY(0).setDuration(150);
        fabCreate.animate().rotationBy(-225).setDuration(150);
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        int targetWidth = maxWidthForPortraitMode;
        int targetHeight = maxHeightForPortraitMode;


        return new Pair<>(targetWidth, targetHeight);
    }

    private Integer getImageMaxWidth() {
        if (imageMaxWidth == null) {
            imageMaxWidth = ((View) fabCreate.getParent()).getWidth();
        }
        return imageMaxWidth;
    }

    private Integer getImageMaxHeight() {
        if (imageMaxHeight == null) {
            imageMaxHeight = ((View) fabCreate.getParent()).getHeight();
        }
        return imageMaxHeight;
    }

    public ArrayList<String> getDataFromEventTable() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, MY_CAL_REQ);
        }

        Cursor cur = null;
        ContentResolver cr = getContentResolver();

        String[] mProjection =
                {
                        "_id",
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND,
                        CalendarContract.Events.ACCOUNT_NAME
                };

        Uri uri = CalendarContract.Events.CONTENT_URI;
        String selection = CalendarContract.Events.ACCOUNT_NAME + " = ? ";

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        String[] selectionArgs = new String[]{pref.getString("user", "")};

        cur = cr.query(uri, mProjection, selection, selectionArgs, CalendarContract.Events.DTSTART + " desc");

        ArrayList<String> result = new ArrayList<>();
        while (cur.moveToNext()) {
            String title = cur.getString(cur.getColumnIndex(CalendarContract.Events.TITLE));
            result.add(title);
        }
        return result;
    }

    private void pickImageFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }
}
