package com.example.planit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ListEventsActivity extends AppCompatActivity {
    private static final int MY_CALENDAR_PERMISSION_CODE = 200;
    Button listEvents;
    Cursor cursor = null;
    Uri uri = CalendarContract.Calendars.CONTENT_URI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_events);
        listEvents = (Button)findViewById(R.id.button_list_events);
        listEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ListEventsActivity.this, "List event button pressed", Toast.LENGTH_SHORT).show();
                switch (view.getId()){
                    case R.id.button_list_events:
                        Toast.makeText(ListEventsActivity.this, "Checking for permission", Toast.LENGTH_SHORT).show();
                        if(ContextCompat.checkSelfPermission(ListEventsActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(ListEventsActivity.this, new String[]{Manifest.permission.READ_CALENDAR}, MY_CALENDAR_PERMISSION_CODE);
                        }
                        else{
                            getEvents();
                            break;
                        }
                }
            }
        });
    }

    private void getEvents() {
        cursor = getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            if(cursor != null){
                int id_1 = cursor.getColumnIndex(CalendarContract.Events._ID);
                int id_2 = cursor.getColumnIndex(CalendarContract.Events.ORGANIZER);
                int id_3 = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION);
                int id_4 = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);

//                String idValue = cursor.getColumnName(id_1);
////                String idVal = cursor.getString(id_1);
//                String titleValue = cursor.getString(id_2);
//                String descriptionValue = cursor.getString(id_3);
//                String locationValue = cursor.getString(id_4);
                Toast.makeText(ListEventsActivity.this, id_1 + ", " + id_2 + ", " + id_3 + ", " + id_4, Toast.LENGTH_SHORT).show();
//                Toast.makeText(ListEventsActivity.this, idValue + ", " + titleValue + ", " + descriptionValue + ", " + locationValue, Toast.LENGTH_SHORT).show();
//                Toast.makeText(ListEventsActivity.this, idValue + ", " + idVal, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ListEventsActivity.this, "Event is not present", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_CALENDAR_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    System.out.println("Permission granted");
                    Toast.makeText(ListEventsActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                    getEvents();
                } else {
                    return;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
