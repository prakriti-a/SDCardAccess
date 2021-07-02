package com.prakriti.sdcardaccess;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class SDCardChecker {


    public static void checkExternalStorageAvailable(Context context) {
        boolean isExternalStorageAvailable = false;
        boolean isExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState(); // checks write & read permissions

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            // we can read & write media
            isExternalStorageAvailable = isExternalStorageWriteable = true;
            Toast.makeText(context, "Read and Write Allowed", Toast.LENGTH_SHORT).show();
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // we can only read media
            isExternalStorageAvailable = true;
            isExternalStorageWriteable = false;
            Toast.makeText(context, "Read Only Allowed", Toast.LENGTH_SHORT).show();
        }
        else {
            // neither read nor write
            isExternalStorageAvailable = isExternalStorageWriteable = false;
            Toast.makeText(context, "Neither Read nor Write Allowed", Toast.LENGTH_SHORT).show();
        }
    }

}
