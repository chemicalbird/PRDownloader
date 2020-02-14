package com.downloader.internal.stream;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import junit.framework.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OutputStreamWrapper {

    private static OutputStreamWrapper INSTANCE;

    public static void init(Context context) {
        INSTANCE = new OutputStreamWrapper(context);
    }

    public static OutputStreamWrapper getInstance() {
        return INSTANCE;
    }

    private Context context;

    public OutputStreamWrapper(Context context) {
        this.context = context;
    }

    public FileDownloadOutputStream getOutputStream(Uri storageUri, String fileName) throws IOException {
        Assert.assertNotNull(storageUri.getPath());

        if ("file".equals(storageUri.getScheme())) {
            File file = new File(storageUri.getPath(), fileName);
            if (file.exists() && file.canWrite()) {
                return FileDownloadRandomAccessFile.create(file);
            }
        } else if ("content".equals(storageUri.getScheme())) {
            Uri fileUri = DocumentFile.fromTreeUri(context, storageUri).findFile(fileName).getUri();
            return SAFDocumentFile.create(context, fileUri.toString());
        }

        return null;
    }

    public boolean deleteFile(Uri storageUri, String fileName) {
        Assert.assertNotNull(storageUri.getPath());

        if ("file".equals(storageUri.getScheme())) {
            File file = new File(storageUri.getPath(), fileName);
            if (file.exists() && file.canWrite()) {
                return file.delete();
            }
        } else if ("content".equals(storageUri.getScheme())) {
            DocumentFile documentFile = DocumentFile.fromTreeUri(context, storageUri).findFile(fileName);
            if (documentFile != null && documentFile.exists()) {
                return documentFile.delete();
            }
        }

        return false;
    }

    public boolean renameFile(Uri storageUri, String oldName, String newName) {
        Assert.assertNotNull(storageUri.getPath());

        if ("file".equals(storageUri.getScheme())) {
            File file = new File(storageUri.getPath(), oldName);
            if (file.exists() && file.canWrite()) {
                return file.renameTo(new File(storageUri.getPath(), newName));
            }
        } else if ("content".equals(storageUri.getScheme())) {
            return DocumentFile.fromTreeUri(context, storageUri).findFile(oldName).renameTo(newName);

        }

        return false;
    }

    public String createFile(Uri storageUri, String fileName) throws IOException {
        Assert.assertNotNull(storageUri.getPath());

        if ("file".equals(storageUri.getScheme())) {
            createFile(new File(storageUri.getPath(), fileName));
            return storageUri.getPath();
        } else if ("content".equals(storageUri.getScheme())) {
            DocumentFile exists = DocumentFile.fromTreeUri(context, storageUri).findFile(fileName);
            if (exists == null) {
                try {
                    return DocumentFile.fromTreeUri(context, storageUri).createFile("", fileName).getUri().toString();
                } catch (NullPointerException e) {
                    exists = DocumentFile.fromTreeUri(context, storageUri).findFile(fileName);
                    return exists != null ? exists.getUri().toString() : null;
                }
            } else {
                return exists.getUri().toString();
            }
        }

        throw new IOException("didn't understand " + storageUri.getPath());
    }

    public boolean exists(Uri storageUri, String file) {
        Assert.assertNotNull(storageUri.getPath());

        if ("file".equals(storageUri.getScheme())) {
            return new File(storageUri.getPath(), file).exists();
        } else if ("content".equals(storageUri.getScheme())) {
            DocumentFile doc = DocumentFile.fromTreeUri(context, storageUri).findFile(file);
            return doc != null && doc.exists();
        }

        return false;
    }

    private void createFile(File file) throws IOException {
        if (!file.exists()) {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                if (file.getParentFile().mkdirs()) {
                    if (!file.createNewFile()) throw new FileNotFoundException("$file $FILE_NOT_FOUND");
                } else {
                    throw new FileNotFoundException("$file $FILE_NOT_FOUND");
                }
            } else {
                if (!file.createNewFile()) throw new FileNotFoundException("$file $FILE_NOT_FOUND");
            }
        }
    }
}
