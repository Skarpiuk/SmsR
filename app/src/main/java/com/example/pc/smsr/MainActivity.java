package com.example.pc.smsr;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;




public class MainActivity extends AppCompatActivity implements LocationListener {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    EditText nrEdit;
    EditText tekstEdit;
    EditText ilosc;
    EditText opoznienie;
    int iloscwiadomosci;
    String wiadomosc;
    String nr;
    int delay;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    String lat;
    String provider;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;

    private static final String TAG = "MainActivity";

    private AdView mAdView;
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;
    String Lokal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        this.getSupportActionBar().hide();
        setLayout();


        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544/6300978111");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mAdView.loadAd(adRequest);


    }


    public void setLayout() {
        setContentView(R.layout.activity_master);
    }

    public void wyslij(View view) {
        nrEdit = (EditText) findViewById(R.id.nrTel);
        tekstEdit = (EditText) findViewById(R.id.wiadomosc);
        ilosc = (EditText) findViewById(R.id.iloscWiadmosci);
        opoznienie = (EditText) findViewById(R.id.opoznienie);

        nr = nrEdit.getText().toString();

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        if (ilosc.getText().toString().length() > 0) {
            iloscwiadomosci = Integer.parseInt(ilosc.getText().toString());
            iloscwiadomosci++;
        } else {
            iloscwiadomosci = 1;
            Toast.makeText(getApplicationContext(),
                    "Wpisz prawidłową ilość wiadomości", Toast.LENGTH_LONG).show();
        }

        if (nr.length() < 9) {
            Toast.makeText(getApplicationContext(),
                    "Wpisz prawidłowy numer telefonu", Toast.LENGTH_LONG).show();
        }
        if ((opoznienie.getText().toString()).length() > 0) {
            delay = Integer.parseInt(opoznienie.getText().toString());
        } else {
            delay = 2000;
            Toast.makeText(getApplicationContext(),
                    "Nie podałeś opóznienia, zostanie ono ustawione na 2000ms", Toast.LENGTH_LONG).show();
        }
        if (tekstEdit.getText().toString().length() < 1) {
            wiadomosc = " ";
        } else {
            wiadomosc = tekstEdit.getText().toString();
        }
        if (nr.length() < 9) {
            Toast.makeText(getApplicationContext(),
                    "Wpisz prawidłowy numer telefonu", Toast.LENGTH_LONG).show();
        }
        if (iloscwiadomosci > 1 && nr.length() >= 9) {
            Handler handler1 = new Handler();
            for (int i = 1; i < iloscwiadomosci; i++) {
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(nr, null, wiadomosc, null, null);
                    }
                }, delay);
            }
        }


    }

    public void onClickSelectContact(View btnSelectContact) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            uriContact = data.getData();
            nrEdit = (EditText) findViewById(R.id.nrTel);
            nrEdit.setText(retrieveContactNumber() + " (" + retrieveContactName() + ")");
        }
    }

    private String retrieveContactNumber() {

        String contactNumber = null;
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        return contactNumber;
    }

    private String retrieveContactName() {

        String contactName = null;
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        return contactName;

    }


    public void getLocalization(View view) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        tekstEdit = (EditText) findViewById(R.id.wiadomosc);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = 0; i < providers.size(); i++) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null)
                break;
        }
        double[] gps = new double[2];

        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        Lokal = "http://maps.google.com/?q=" + Double.toString(gps[0]) + "," + Double.toString(gps[1]);
        tekstEdit.setText(Lokal);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }


    public void shareonG(View view) {
        EditText wiad = (EditText) findViewById(R.id.wiadomosc);
        try {
            File testFile = new File(this.getExternalFilesDir(null), "message.txt");
            if (!testFile.exists()) {
                testFile.createNewFile();
            } else if (testFile.exists()) {
                testFile.delete();
                testFile.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, true /*append*/));
            writer.newLine();
            writer.write(wiad.getText().toString());
            writer.close();
            MediaScannerConnection.scanFile(this,
                    new String[]{testFile.toString()},
                    null,
                    null);
        } catch (IOException e) {
            Log.e("ReadWriteFile", "Unable to write to the TestFile.txt file.");
        }


    }


    public void Logowanie(View view) {

    }

    public void Rejestruj(View view) {

    }
}