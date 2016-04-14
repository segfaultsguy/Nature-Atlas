
        package com.example.brandan.natureatlas;

        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.database.Cursor;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Matrix;
        import android.graphics.Paint;
        import android.graphics.Point;
        import android.graphics.drawable.ColorDrawable;
        import android.media.ExifInterface;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.support.v7.app.ActionBarActivity;
        import android.util.Log;
        import android.view.*;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.AutoCompleteTextView;
        import android.widget.CheckBox;
        import android.widget.CompoundButton;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.MultiAutoCompleteTextView;
        import android.widget.Spinner;
        import android.widget.Toast;

        import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
        import org.json.JSONArray;
        import org.json.JSONException;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.ObjectInputStream;
        import java.io.ObjectOutputStream;
        import java.io.StreamCorruptedException;
        import java.text.DateFormat;
        import java.text.ParseException;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.Map;

public class SightingSubmit extends ActionBarActivity
{

    private static int GALLERY_PICTURE = 0;
    private static int CAMERA_PICTURE = 1;
    private static int GALLERY_PICTURE2 = 2;
    private static int CAMERA_PICTURE2= 3;
    private Bitmap getImageBitmapTwo = null;
    private Bitmap scaledBitmapTwo = null;
    private Bitmap thumbBitmapTwo = null;
    private Bitmap getImageBitmap = null;
    private Bitmap scaledBitmap = null;
    private Bitmap thumbBitmap = null;
    PairRetriever pr;
    ScientificRetriever sr;
    ArrayAdapter<String> arrayAdapterS;
    private EditText name, lat, lon, accuracyGPS, state, country;
    private Spinner species, wildStatusSpinner, phenologySpinner, abundanceSpinner;
    private MultiAutoCompleteTextView scientificName, commonName;
    private boolean comingFromSnapshot;
    private CheckBox editSwitch;
    private int editable;
    Thread t1, t2;
    private Map<String, String> pairs;
    private String  s_scientificName, s_commonName, imagePath, userChoice;
    private ImageView displayImage, displayImageTwo;
    private JSONArray pairJSON= null;
    private JSONArray scientificJSON = null;
    private JSONArray dateJSON = null;
    private JSONArray dateSJSON = null;
    private static String bucketName = "natureatlas";
    ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
    ArrayList<String> scientific = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sighting_submit);


        Bundle bundle = getIntent().getExtras();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#916d43"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        if(bundle != null)
        {
            comingFromSnapshot = true;
        }
        else
        {
            comingFromSnapshot = false;
        }



        editSwitch = (CheckBox)findViewById(R.id.editableSwitch);
        abundanceSpinner = (Spinner)findViewById(R.id.abundance);
        displayImage = (ImageView)findViewById(R.id.userImage);
        displayImageTwo = (ImageView)findViewById(R.id.userImageTwo);
        species = (Spinner)findViewById(R.id.organismType);
        wildStatusSpinner = (Spinner)findViewById(R.id.wildStatus);
        phenologySpinner = (Spinner)findViewById(R.id.phenology);
        scientificName = (MultiAutoCompleteTextView)findViewById(R.id.scientificName);
        state = (EditText)findViewById(R.id.state);
        country = (EditText)findViewById(R.id.country);

        accuracyGPS = (EditText)findViewById(R.id.gpsAcc);
        commonName = (MultiAutoCompleteTextView)findViewById(R.id.commonName);
        lat = (EditText)findViewById(R.id.Lat);
        lon = (EditText)findViewById(R.id.Lon);

        t1 = new Thread(runnable);
        t1.run();
        t2 = new Thread(runnableScientific);
        t2.run();

        StartScientificGet();
        StartDataGet();



        editSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    editable = 1;
                } else {
                    editable = 0;
                }
            }
        });


        scientificName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(pairs != null)
                {
                    if (!scientificName.getText().toString().equals("") && commonName.getText().toString().equals(""))
                    {
                        Log.d("Finding match", "True");
                        for (String s : pairs.keySet())
                        {
                            if (s.equals(scientificName.getText().toString()))
                            {
                                Log.d("Match Common", pairs.get(s));
                                commonName.setText(pairs.get(s));
                            }
                        }
                    }
                    Log.d("Focus changed", "scientific");
                }
            }
        });


        commonName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(pairs != null)
                {
                    if (!commonName.getText().toString().equals("") && scientificName.getText().toString().equals(""))
                    {
                        for (Map.Entry entry : pairs.entrySet())
                        {
                            if (commonName.getText().toString().equals(entry.getValue().toString()))
                            {
                                scientificName.setText(entry.getKey().toString());
                            }
                        }
                    }
                    Log.d("Focus changed", "common");
                }
            }
        });




        species.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object choice = species.getItemAtPosition(i);
                userChoice = choice.toString();
                switch (choice.toString()) {
                    case "Birds":


                        break;
                    case "Fungi":

                        ArrayAdapter<String> fPhen = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, getResources().getStringArray(R.array.phen));
                        phenologySpinner.setAdapter(fPhen);
                        break;
                    case "Fishes":

                        break;
                    case "Herps":

                        break;
                    case "Invertebrates":

                        break;
                    case "Mammals":

                        break;
                    case "Plants":

                        ArrayAdapter<String> plantsWild = new ArrayAdapter<String>(getApplicationContext(), R.layout.layoutblack, R.id.blacktext, getResources().getStringArray(R.array.wildStatusPlants));
                        wildStatusSpinner.setAdapter(plantsWild);
                        ArrayAdapter<String> pPhen = new ArrayAdapter<String>(getApplicationContext(), R.layout.layoutblack, R.id.blacktext, getResources().getStringArray(R.array.plantPhen));
                        phenologySpinner.setAdapter(pPhen);
                        break;
                    case "Zooplankton":

                        break;
                    default:
                        break;
                }
                if (!choice.equals("Plants")) {
                    ArrayAdapter<String> wildStatus = new ArrayAdapter<String>(getApplicationContext(), R.layout.layoutblack, R.id.blacktext, getResources().getStringArray(R.array.wildStatus));
                    wildStatusSpinner.setAdapter(wildStatus);
                }
                if (!choice.equals("Plants") && !choice.equals("Fungi")) {
                    ArrayAdapter<String> phen = new ArrayAdapter<String>(getApplicationContext(), R.layout.layoutblack, R.id.blacktext, getResources().getStringArray(R.array.phen));
                    phenologySpinner.setAdapter(phen);
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(comingFromSnapshot)
        {
            Globals globals = Globals.getInstance();
            Snapshot s = globals.GetSnapshotInstance();
            accuracyGPS.setText(s.ac);
            scientificName.setText(s.species);
            commonName.setText(s.comm);
            lat.setText(s.lat);
            lon.setText(s.lon);
            GPSTracking gpsTracking = new GPSTracking(this);
            gpsTracking.GetMyLocation();
            state.setText(gpsTracking.getState());
            country.setText(gpsTracking.getCountry());

        }
        else
        {
            GPSTracking gpsTracking = new GPSTracking(this);
            gpsTracking.GetMyLocation();
            float accuracy = gpsTracking.GetAccuracy();
            String accString = Float.toString(accuracy);
            accuracyGPS.setText(accString);


            String longitude = Double.toString(gpsTracking.GetLongitude());
            String latitude = Double.toString(gpsTracking.GetLatitude());
            GPSTracking gps = new GPSTracking(this);
            state.setText(gps.getState());
            country.setText(gps.getCountry());

            lat.setText(latitude);
            lon.setText(longitude);

            s_scientificName = scientificName.getText().toString();
            s_commonName = commonName.getText().toString();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_itemdetailother, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId())
        {
            case R.id.find:
                Intent intentF = new Intent(this, AtlasMap.class);
                startActivity(intentF);
                return true;
            case R.id.share:
                Intent intentSu = new Intent(this, SightingSubmit.class);
                startActivity(intentSu);
                return true;
            case R.id.about:
                Toast.makeText(this, "Programmed by Brandan Jablonski for Millersville University.",
                        Toast.LENGTH_LONG).show();
                return true;
            case R.id.account:
                Intent intent = new Intent(this, loginregister.class);
                startActivity(intent);
                return true;
            case R.id.drafts:
                Intent intentD = new Intent(this, Drafts.class);
                startActivity(intentD);
                return true;
            case R.id.action_settings:
                Intent intentS = new Intent(this, SettingsPage.class);
                startActivity(intentS);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void Submit(View view) throws IOException {
        ConnectionStates cs = new ConnectionStates(this);
        Globals g = Globals.getInstance();
        if(!cs.isMobileDataOn() && !cs.isWifiOn())
        {

            Snapshot capture = new Snapshot(scientificName.getText().toString(),species.getSelectedItem().toString(), commonName.getText().toString(), phenologySpinner.getSelectedItem().toString(), abundanceSpinner.getSelectedItem().toString(),  country.getText().toString(),
                    wildStatusSpinner.getSelectedItem().toString(), lat.getText().toString(),
                    lon.getText().toString(), accuracyGPS.getText().toString());
            snapshots.add(capture);
        }
        else
        {
            if(g.GetSignedIn())
            {
                SharedPreferences prefs = getSharedPreferences("userName", MODE_PRIVATE);
                DataSender sender = new DataSender();
                if (!snapshots.isEmpty()) {
                    // If snapshots aren't empty, we have to send them to the server.
                    for (Snapshot s : snapshots) {
                        // try
                        // {

                        // Update once we get more info.
                        //sender.SendData(s.name, s.lat, s.lon,s.wild, s.nation, s.species, s.organism ,this);
                        //}
                        //catch(JSONException js)
                        //{

                        //}
                        snapshots.remove(s);
                    }

                }
                try {
                    String organism = "";
                    switch (species.getSelectedItem().toString()) {
                        case "Invertebrates":
                            organism = "1";
                            break;
                        case "Birds":
                            organism = "2";
                            break;
                        case "Fishes":
                            organism = "3";
                            break;
                        case "Fungi":
                            organism = "4";
                            break;
                        case "Herps":
                            organism = "5";
                            break;
                        case "Mammals":
                            organism = "6";
                            break;
                        case "Plants":
                            organism = "8";
                            break;
                        case "Zooplankton":
                            organism = "9";
                            break;

                    }
                    Log.d("Organism", organism);
                    //Update once we get more info.
                    Log.d("NameSent", prefs.getString("usersName", ""));


                    SharedPreferences sp = getSharedPreferences("userName", MODE_PRIVATE);
                    if(getImageBitmap != null)
                    {

                        thumbBitmap = Bitmap.createScaledBitmap(getImageBitmap, 100,100, false);
                        scaledBitmap = Bitmap.createScaledBitmap(getImageBitmap, 500,500, false);
                        TransferUtility transferUtility = new TransferUtility(g.GetS3Instance(), getApplicationContext());
                        transferUtility.upload(bucketName, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-O", BitmapToFile(getImageBitmap, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-O"+ ".png"));
                        transferUtility.upload(bucketName, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Scaled", BitmapToFile(scaledBitmap, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Scaled"+ ".png"));
                        transferUtility.upload(bucketName, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Thumb", BitmapToFile(thumbBitmap, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Thumb"+ ".png"));

                    }

                    if(getImageBitmapTwo!= null)
                    {
                        thumbBitmapTwo = Bitmap.createScaledBitmap(getImageBitmapTwo, 100,100, false);
                        scaledBitmapTwo = Bitmap.createScaledBitmap(getImageBitmapTwo, 500,500, false);
                        TransferUtility transferUtility = new TransferUtility(g.GetS3Instance(), getApplicationContext());
                        transferUtility.upload(bucketName, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-O", BitmapToFile(getImageBitmapTwo, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-O"+ ".png"));
                        transferUtility.upload(bucketName, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Scaled", BitmapToFile(scaledBitmapTwo, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Scaled"+ ".png"));
                        transferUtility.upload(bucketName, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Thumb", BitmapToFile(thumbBitmapTwo, sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Thumb"+ ".png"));
                    }
                    sender.SendData(prefs.getString("email", ""), prefs.getString("usersName", ""), lat.getText().toString(), lon.getText().toString(),
                            wildStatusSpinner.getSelectedItem().toString(), country.getText().toString(), scientificName.getText().toString(), organism, commonName.getText().toString(),
                            phenologySpinner.getSelectedItem().toString(), abundanceSpinner.getSelectedItem().toString(), accuracyGPS.getText().toString(), state.getText().toString(),
                            Integer.toString(editable), sp.getString("email", "") + Calendar.getInstance().getTime().toString()+ "-O", sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Scaled", sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Thumb"
                            , sp.getString("email", "") + Calendar.getInstance().getTime().toString()+ "-O2",  sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Scaled2", sp.getString("email", "") + Calendar.getInstance().getTime().toString() + "-Thumb2", this);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(this, "You must be signed in to make a post. Please log in.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void SaveDraft(View view)
    {

        Snapshot capture = new Snapshot(scientificName.getText().toString(),species.getSelectedItem().toString(),  commonName.getText().toString(),  phenologySpinner.getSelectedItem().toString(), abundanceSpinner.getSelectedItem().toString(), country.getText().toString(),
                wildStatusSpinner.getSelectedItem().toString(), lat.getText().toString(),
                lon.getText().toString(), accuracyGPS.getText().toString());




        String fileName = this.getFilesDir().getPath() + "/snapshots.txt";
        try
        {
            FileInputStream inStream = new FileInputStream(fileName);
            ObjectInputStream objectInStream = new ObjectInputStream(inStream);
            int count = objectInStream.readInt();
            Log.d("count",Integer.toString(count));

            for(int c = 0; c < count; c++)
            {
                snapshots.add((Snapshot)objectInStream.readObject());
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }



        try
        {

            snapshots.add(capture);
            FileOutputStream outStream = new FileOutputStream(this.getFilesDir().getPath() + "/snapshots.txt");
            ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
            objectOutStream.writeInt(snapshots.size()); // Save size first
            Log.d("FilePath: ", this.getFilesDir().getPath() + "/snapshots.txt");


            Log.d("snapSize", Integer.toString(snapshots.size()));





            for(Snapshot s:snapshots)
                objectOutStream.writeObject(s);
            objectOutStream.close();



        }
        catch(FileNotFoundException f)
        {
            f.printStackTrace();
        }
        catch(StreamCorruptedException sc)
        {
            sc.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }


    public void GetImage(View view)
    {
        AlertDialog.Builder getPicture = new AlertDialog.Builder(this);
        getPicture.setTitle("Choose An Option");
        getPicture.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent galleryIntent;
                galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_PICTURE);

            }
        });
        getPicture.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(android.os.Environment.getExternalStorageDirectory(), "picture.png");
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                startActivityForResult(cameraIntent, CAMERA_PICTURE);
            }
        });
        getPicture.create();
        getPicture.show();
    }

    public void GetImageTwo(View view)
    {
        AlertDialog.Builder getPicture = new AlertDialog.Builder(this);
        getPicture.setTitle("Choose An Option");
        getPicture.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent galleryIntent;
                galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_PICTURE2);

            }
        });
        getPicture.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(android.os.Environment.getExternalStorageDirectory(), "picture.png");
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                startActivityForResult(cameraIntent, CAMERA_PICTURE2);
            }
        });
        getPicture.create();
        getPicture.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        Log.d("Request Code", Integer.toString(requestCode));
        imagePath = null;
        if(resultCode == RESULT_OK && requestCode == GALLERY_PICTURE)
        {
            if (data != null)
            {
                Uri image = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(image, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                imagePath = c.getString(columnIndex);
                c.close();

                getImageBitmap = BitmapFactory.decodeFile(imagePath);
                //getImageBitmap = Bitmap.createScaledBitmap(getImageBitmap, 500, 500, false);
                Point point = new Point(-10, -10);
                getImageBitmap = WaterMark(getImageBitmap, point, "NatureAtlas.org");
                displayImage.setImageBitmap(getImageBitmap);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == CAMERA_PICTURE)
        {
            File f = new File(Environment.getExternalStorageDirectory().toString());
            for(File temp : f.listFiles())
            {
                if(temp.getName().equals("picture.png"))
                {
                    f = temp;
                    break;
                }
            }
            if(!f.exists())
            {
                Toast.makeText(getBaseContext(), "Error getting image.", Toast.LENGTH_SHORT).show();

            }
            try
            {

                getImageBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

                getImageBitmap = Bitmap.createScaledBitmap(getImageBitmap, 400, 400, true);

                int rotate = 0;
                try {
                    ExifInterface exif = new ExifInterface(f.getAbsolutePath());
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                getImageBitmap = Bitmap.createBitmap(getImageBitmap, 0, 0, getImageBitmap.getWidth(),
                        getImageBitmap.getHeight(), matrix, true);



                Point point = new Point(200,400);

                getImageBitmap = WaterMark(getImageBitmap, point, "NatureAtlas.org");
                displayImage.setImageBitmap(getImageBitmap);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        if(resultCode == RESULT_OK && requestCode == GALLERY_PICTURE2)
        {
            if (data != null)
            {
                Uri image = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(image, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                imagePath = c.getString(columnIndex);
                c.close();

                getImageBitmapTwo = BitmapFactory.decodeFile(imagePath);
                //getImageBitmap = Bitmap.createScaledBitmap(getImageBitmap, 500, 500, false);
                Point point = new Point(-10, -10);
                getImageBitmapTwo = WaterMark(getImageBitmapTwo, point, "NatureAtlas.org");
                displayImageTwo.setImageBitmap(getImageBitmapTwo);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == CAMERA_PICTURE2)
        {
            File f = new File(Environment.getExternalStorageDirectory().toString());
            for(File temp : f.listFiles())
            {
                if(temp.getName().equals("picture.png"))
                {
                    f = temp;
                    break;
                }
            }
            if(!f.exists())
            {
                Toast.makeText(getBaseContext(), "Error getting image.", Toast.LENGTH_SHORT).show();

            }
            try
            {

                getImageBitmapTwo = BitmapFactory.decodeFile(f.getAbsolutePath());

                getImageBitmapTwo = Bitmap.createScaledBitmap(getImageBitmapTwo, 400, 400, true);

                int rotate = 0;
                try {
                    ExifInterface exif = new ExifInterface(f.getAbsolutePath());
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                getImageBitmapTwo = Bitmap.createBitmap(getImageBitmapTwo, 0, 0, getImageBitmapTwo.getWidth(),
                        getImageBitmapTwo.getHeight(), matrix, true);



                Point point = new Point(200,400);

                getImageBitmapTwo = WaterMark(getImageBitmapTwo, point, "NatureAtlas.org");
                displayImageTwo.setImageBitmap(getImageBitmapTwo);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }


    android.os.Handler handler = new android.os.Handler();
    android.os.Handler handlerS = new android.os.Handler();
    Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                SharedPreferences.Editor sharedPreferences = getSharedPreferences("pairsList", MODE_PRIVATE).edit();
                SharedPreferences sp = getSharedPreferences("pairsList", MODE_PRIVATE);
                String date = sp.getString("dateP", "");

                if (!date.equals(""))
                {
                    CheckDate();
                    Date checkedDate = new Date();
                    Date storedDate = new Date();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
                    DateFormat storedDateF = new SimpleDateFormat("EEE MMM DD HH:mm:ss zzz yyyy");
                    try
                    {
                        checkedDate = df.parse(dateJSON.getJSONObject(0).getString("updatedAt"));
                        Log.d("Downloaded Date:", dateJSON.getJSONObject(0).getString("updatedAt"));
                        storedDate = storedDateF.parse(date);
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                    if (checkedDate.after(storedDate))
                    {
                        if (pairJSON != null)
                        {
                            try
                            {
                                pairs = new HashMap();
                                for (int i = 0; i < pairJSON.length(); i++)
                                {
                                    try
                                    {
                                        pairs.put(pairJSON.getJSONObject(i).getString("scientific"), pairJSON.getJSONObject(i++).getString("vernacular"));
                                    }
                                    catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }


                                }
                                for (String s : pairs.keySet())
                                {

                                    sharedPreferences.putString(s, pairs.get(s));
                                }
                                Date newDate = new Date();
                                newDate = df.parse(pairJSON.getJSONObject(0).getString("updatedAt"));
                                sharedPreferences.putString("dateP", newDate.toString());
                                sharedPreferences.apply();

                                t1.interrupt();
                                pr.cancel(true);


                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }
                        else
                        {
                            handler.postDelayed(runnable, 20000);
                        }
                    }
                    else
                    {


                        pairs = (HashMap<String, String>) sp.getAll();

                        Log.d("Pair Size After Load : ", Integer.toString(pairs.size()));
                        handler.removeCallbacks(runnable);
                        throw new InterruptedException();
                    }

                }

                // If we have never retrieved the date before.
                if (pairJSON != null && date.equals(""))
                {
                    try
                    {
                        pairs = new HashMap();
                        for (int i = 0; i < pairJSON.length(); i++)
                        {
                            try
                            {
                                pairs.put(pairJSON.getJSONObject(i).getString("scientific"), pairJSON.getJSONObject(i++).getString("vernacular"));
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        Log.d("Pairs Size: ", Integer.toString(pairs.size()));
                        for (String s : pairs.keySet()) {
                            sharedPreferences.putString(s, pairs.get(s));
                        }
                        Date newDate = new Date();
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        newDate = df.parse(pairJSON.getJSONObject(0).getString("updatedAt"));
                        sharedPreferences.putString("dateP", newDate.toString());
                        sharedPreferences.apply();
                        throw new InterruptedException();

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    t1.interrupt();
                    handler.removeCallbacks(runnable);
                }
                else
                {
                    handler.postDelayed(runnable, 20000);
                }


                Log.d("Runnable", "Running");
            }
            catch (InterruptedException e)
            {
                t1.interrupt();
            }
        }
    };

    Runnable runnableScientific = new Runnable()
    {
        @Override
        public void run() {


            try
            {
                SharedPreferences.Editor sPE = getSharedPreferences("scientificList", MODE_PRIVATE).edit();
                SharedPreferences sharedPreferences = getSharedPreferences("scientificList", MODE_PRIVATE);
                String date = sharedPreferences.getString("dateS", "");
                if (!date.equals(""))
                {
                    CheckDateS();
                    Date checkedDate = new Date();
                    Date storedDate = new Date();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
                    DateFormat storedDateF = new SimpleDateFormat("EEE MMM DD HH:mm:ss zzz yyyy");
                    try
                    {
                        checkedDate = df.parse(dateJSON.getJSONObject(0).getString("updatedAt"));
                        Log.d("Downloaded Date:", dateJSON.getJSONObject(0).getString("updatedAt"));
                        storedDate = storedDateF.parse(date);
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                    if (scientificJSON != null && checkedDate.after(storedDate))
                    {

                        try
                        {

                            for (int i = 0; i < scientificJSON.length(); i++)
                            {
                                try
                                {
                                    scientific.add(scientificJSON.getJSONObject(i).getString("word"));
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            Date newDate = new Date();
                            DateFormat dF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            newDate = dF.parse(scientificJSON.getJSONObject(0).getString("updatedAt"));
                            sPE.putString("dateS", newDate.toString());
                            sPE.putString("scientificList", ConvertToString(scientific));
                            sPE.apply();
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, scientific);
                            scientificName.setAdapter(arrayAdapter);
                            scientificName.setTokenizer(new SpaceTokenizer());


                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }




                    }
                    else
                    {
                        handlerS.postDelayed(runnableScientific, 20000);
                    }

                }
                else
                {
                    scientific = ConvertToArray(sharedPreferences.getString("scientificList", ""));
                    handlerS.removeCallbacks(runnableScientific);
                    Log.d("scientific after load", Integer.toString(scientific.size()));
                    throw new InterruptedException();

                }


                if (scientificJSON != null && date.equals(""))
                {
                    try
                    {

                        for (int i = 0; i < scientificJSON.length(); i++)
                        {
                            try
                            {
                                scientific.add(scientificJSON.getJSONObject(i).getString("word"));
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        Date newDate = new Date();
                        DateFormat dF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        newDate = dF.parse(scientificJSON.getJSONObject(0).getString("updatedAt"));
                        sPE.putString("dateS", newDate.toString());
                        sPE.putString("scientificList", ConvertToString(scientific));
                        sPE.apply();
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, scientific);
                        scientificName.setAdapter(arrayAdapter);
                        scientificName.setTokenizer(new SpaceTokenizer());



                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }



                }
                else
                {
                    handlerS.postDelayed(runnableScientific, 20000);
                }

                Log.d("Runnable", "Scientific Running");
                throw new InterruptedException();
            }
            catch (InterruptedException e)
            {
                t2.interrupt();

            }
        }
    };
    private class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {

        private final char delimiter = ' ';

        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != delimiter) {
                i--;
            }
            while (i < cursor && text.charAt(i) == delimiter) {
                i++;
            }

            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == delimiter) {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();
            while (i > 0 && text.charAt(i - 1) == delimiter) {
                i--;
            }

            return text;

        }

    }
    private static Bitmap WaterMark(Bitmap source, Point point, String text)
    {
        int w = source.getWidth();
        int h = source.getHeight();
        Bitmap newImage = Bitmap.createBitmap(w, h, source.getConfig());

        Canvas canvas = new Canvas(newImage);
        canvas.drawBitmap(source, 0, 0, null);

        Paint paint = new Paint();
        Color color = new Color();

        paint.setColor(color.WHITE);

        paint.setTextSize(24.0f);

        canvas.drawText(text, point.x, point.y, paint);

        return newImage;
    }

    void StartDataGet()
    {
       pr = new PairRetriever(new PairRetriever.CallBack()
        {
            public void GetJSON(JSONArray array)
            {


                SetData(array);

            }
        });

        pr.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void StartScientificGet()
    {

        sr = new ScientificRetriever(new ScientificRetriever.CallBack()
        {
            public void GetJSON(JSONArray array)
            {


                SetScientificData(array);

            }
        });
        sr.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void SetScientificData(JSONArray array)
    {

        scientificJSON = array;
        Log.d("scientificRetrieved", Integer.toString(scientificJSON.length()));
    }

    public void SetData(JSONArray array)
    {
        pairJSON = array;
        Log.d("dataRetrieved", Integer.toString(pairJSON.length()));

    }

    void CheckDate()
    {
        CheckPairsRetriever cr = new CheckPairsRetriever(this);
        dateJSON = cr.GetJSON();
    }

    void CheckDateS()
    {
        CheckRetriever cr = new CheckRetriever(this);
        dateSJSON = cr.GetJSON();
    }
    private String ConvertToString(ArrayList<String> list) {

        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : list)
        {
            sb.append(delim);
            sb.append(s);;
            delim = ",";
        }
        return sb.toString();
    }

    private ArrayList<String> ConvertToArray(String string) {

        ArrayList<String> list = new ArrayList<String>(Arrays.asList(string.split(",")));
        return list;
    }

    private File BitmapToFile(Bitmap bm, String filename) throws IOException
    {
        File f = new File(this.getCacheDir(), filename);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Convert bitmap to byte array

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitMapData = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(f);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        try
        {
            fos.write(bitMapData);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        fos.flush();
        fos.close();

        return f;
    }
}