package com.prakriti.sdcardaccess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.prakriti.sdcardaccess.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewSwitcher.ViewFactory {
// making & accessing folders in diff directories on device
// find the results of these ops in Files app on device
// view switcher is necessary for image switcher + animation effects

    private EditText edtFileName;
    private TextView txtSDCardData;
    private ImageSwitcher imageSwitcher;
    private LinearLayout llHorizontal;

    private static final int REQ_CODE = 333;

    private ArrayList<String> filePaths;
    private File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // call SD card state checker
        SDCardChecker.checkExternalStorageAvailable(this);
        // check if permission is granted when app runs - will prompt user for permission
        isStorageAccessGranted();

        // initialise UI
        findViewById(R.id.btnDownload).setOnClickListener(this);
        findViewById(R.id.btnMusic).setOnClickListener(this);
        findViewById(R.id.btnDocuments).setOnClickListener(this);
        findViewById(R.id.btnPictures).setOnClickListener(this);
        findViewById(R.id.btnPodcasts).setOnClickListener(this);
        findViewById(R.id.btnRingtones).setOnClickListener(this);
        findViewById(R.id.btnMovies).setOnClickListener(this);
        findViewById(R.id.btnAlarms).setOnClickListener(this);
        findViewById(R.id.btnGetDataFromFile).setOnClickListener(this);
        findViewById(R.id.btnAccessPicture).setOnClickListener(this);
        findViewById(R.id.btnSaveDataToFile).setOnClickListener(this);

        txtSDCardData = findViewById(R.id.txtSDCardData);
        edtFileName = findViewById(R.id.edtFileName);
        ImageView image = findViewById(R.id.image);
        image.setOnClickListener(this);

        imageSwitcher = findViewById(R.id.imageSwitcher);
        imageSwitcher.setFactory(this); // View Factory override
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));

        llHorizontal = findViewById(R.id.llHorizontal);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDownload:
                returnStorageDirectoryForFolder(Environment.DIRECTORY_DOWNLOADS, "My Downloads");;
                break;
            case R.id.btnMusic:
                returnStorageDirectoryForFolder(Environment.DIRECTORY_MUSIC, "My Songs");;
                break;
            case R.id.btnDocuments:
                returnStorageDirectoryForFolder(Environment.DIRECTORY_DOCUMENTS, "My Documents");;
                break;
            case R.id.btnPictures:
                returnStorageDirectoryForFolder(Environment.DIRECTORY_PICTURES, "My Pictures");;
                break;
            case R.id.btnPodcasts:
                returnStorageDirectoryForFolder(Environment.DIRECTORY_PODCASTS, "My Podcasts");;
                break;
            case R.id.btnRingtones:
                returnStorageDirectoryForFolder(Environment.DIRECTORY_RINGTONES, "My Ringtones");;
                break;
            case R.id.btnMovies:
                returnStorageDirectoryForFolder(Environment.DIRECTORY_MOVIES, "My Movies");;
                break;
            case R.id.btnAlarms:
                returnStorageDirectoryForFolder(Environment.DIRECTORY_ALARMS, "My Alarms");;
                break;
            case R.id.btnSaveDataToFile: // save txt file in docs
                saveFileToDocuments();
                break;
            case R.id.btnGetDataFromFile:
                retrieveDataFromFile();
                break;
            case R.id.btnAccessPicture:
                accessPicturesFromDevice();
                break;
            case R.id.image: // save image by clicking on it
                saveImageToPictures();
                break;
        }
    }

    @Override
    public View makeView() {
        // creating image views inside the image switcher
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(800, 800));
        return imageView;
    }

    private boolean isStorageAccessGranted() {
        // SDK 23+, we need to specify permissions
        if(Build.VERSION.SDK_INT >= 23) {
            if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i("PERMISSION", "Permission is granted");
                return true;
            }
            else {
                Log.i("PERMISSION", "Permission is not granted");
                // override on req permission result
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE);
                return false;
            }
        }
        else {
            // SDK VER < 23 - auto granted
            Log.i("PERMISSION", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("PERMISSION", "Permission: " + permissions[0] + " is " + grantResults[0]);
            // resume tasks with permission
        }
    }

    private File returnStorageDirectoryForFolder(String directoryName, String folderName) {
        // create a folder in directory in device storage folder
        // does not create same folder twice
        File filePath = new File(Environment.getExternalStoragePublicDirectory(directoryName), folderName); // gives access to storage dir
        if(!filePath.mkdirs()) { // cannot create
            createToast("Cannot create folder in SD Card");
        } else {
            createToast("Folder: " + folderName + " created!");
        }
        return filePath;
    }

    private void saveFileToDocuments() {
        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "File1.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath); // output a file
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            // check for empty edit text
            outputStreamWriter.append(edtFileName.getText().toString());
            outputStreamWriter.close();
            fileOutputStream.close();
            createToast("File saved to Documents");
        }
        catch (Exception e) {
            Log.e("SAVE_FILE", e.toString());
            e.printStackTrace();
            createToast("Error occured");
        }
    }

    private void retrieveDataFromFile() {
        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "File1.txt"); // same file
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String fileData = "";
            String bufferData = "";
            while ((fileData = bufferedReader.readLine()) != null) {
                bufferData = bufferData + fileData + "\n";
            }
            txtSDCardData.setText(bufferData);
            bufferedReader.close();
            }
        catch (Exception e) {
            Log.e("RETRIEVE_FILE", e.toString());
            e.printStackTrace();
            createToast("Error occured");
        }
    }

    private void saveImageToPictures() {
        try {
            // loop here to save all image files ???
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cat); // res id of image in resources
            File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "cat.png"); // specify format
            OutputStream outputStream = new FileOutputStream(filePath); // creates output stream to write to file
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            createToast("Image is saved to Pictures!");
        }
        catch (Exception e) {
            Log.e("IMAGE_ACCESS", e.toString());
            e.printStackTrace();
            createToast("Error occured");
        }
    }

    private void accessPicturesFromDevice() {
        if(isStorageAccessGranted()) {
            // if storage permission is allowed
            filePaths = new ArrayList<>();
            File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyImages");
                // creating / accessing new folder

            if(filePath.isDirectory() && filePath != null) { // if file address specified is a directory
                files = filePath.listFiles(); // get images inside Animal Images
                for(int i = 0; i < files.length; i++) {
                    filePaths.add(files[i].getAbsolutePath()); // add absolute string path of list of files inside Animal Images
                }
            }
        }
        // next
        for(int index = 0; index < filePaths.size(); index++) {
            final ImageView imageView = new ImageView(MainActivity.this); // create image view for each image in Pictures
            imageView.setImageURI(Uri.parse(filePaths.get(index)));
            imageView.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
            imageView.setPadding(50, 50, 50, 50);

            final int i = index;
            imageView.setOnClickListener(v -> {
                // listens to clicks and puts the clicked image in image switcher
                imageSwitcher.setImageURI(Uri.parse(filePaths.get(i)));
//                imageSwitcher.addView(imageView);
            });
            llHorizontal.addView(imageView);
        }
    }

    private void createToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}