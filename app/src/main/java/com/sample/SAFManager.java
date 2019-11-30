package com.sample;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.text.Html;
import android.text.Spanned;
import android.util.ArrayMap;
import android.widget.Toast;


import java.io.File;
import java.util.HashMap;
import java.util.List;


import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;


public class SAFManager {

    private static final String TAG = "SAFManager";
    public static final String DOCUMENT_AUTHORITY = "com.android.externalstorage.documents";
    public static final int ADD_STORAGE_REQUEST_CODE = 4010;
    public static HashMap<String, Uri> secondaryRoots = new HashMap<>();
    private final Context mContext;

    private SharedPreferences prefs;
    private String savedUri;

    public SAFManager(Context context) {
        mContext = context;

        prefs = context.getSharedPreferences("SAFStorage", Context.MODE_PRIVATE);
        savedUri = prefs.getString("sd_uri", "");
    }

    public String getUri() {
        return savedUri;
    }

    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean hasNougatMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }

    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    @SuppressLint("NewApi")
    public void takeCardUriPermission(final Activity activity, String path) {
        boolean useStorageAccess = hasOreo();
        if (useStorageAccess && null != path) {
            StorageManager storageManager = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume storageVolume = storageManager.getStorageVolume(new File(path));
            Intent intent = storageVolume.createAccessIntent(null);
            try {
                activity.startActivityForResult(intent, ADD_STORAGE_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
            }
        } else if (hasLollipop()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            Spanned message = Html.fromHtml("Select root (outermost) folder of storage "
                    + "<b>" + "root" + "</b>"
                    + " to grant access from next screen");
            builder.setTitle("Grant accesss to External Storage")
                    .setMessage(message.toString())
                    .setPositiveButton("Give Access", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterfaceParam, int code) {
                            Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                            intent.addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                            | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                            );
                            intent.setPackage("com.android.documentsui");
                            try {
                                activity.startActivityForResult(intent, ADD_STORAGE_REQUEST_CODE);
                            } catch (ActivityNotFoundException e) {
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null);
            builder.show();
        }
    }

    public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_STORAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    if (hasKitKat()) {
                        activity.getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        boolean isPrimaryStorage = hasLollipop() && DocumentsContract.getTreeDocumentId(uri).startsWith("primary");
                        if (isPrimaryStorage) {
                            Toast.makeText(activity, "Please choose SD Card", Toast.LENGTH_SHORT).show();
                        } else {
                            savedUri = uri.toString();
                            prefs.edit().putString("sd_uri", savedUri).apply();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


}