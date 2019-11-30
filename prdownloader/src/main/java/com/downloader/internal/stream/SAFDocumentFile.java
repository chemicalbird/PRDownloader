package com.downloader.internal.stream;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileOutputStream;
import java.io.IOException;

public class SAFDocumentFile implements FileDownloadOutputStream {
    private final ParcelFileDescriptor parcelFileDescriptor;
    private FileOutputStream fileOutputStream;

    private SAFDocumentFile(Context context, String fileUri) throws IOException {
        Uri uri = Uri.parse(fileUri);

        parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "w");

        if (parcelFileDescriptor == null) {
            throw new IllegalStateException("file descriptor is null");
        }

        fileOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
        fileOutputStream.getChannel().position(0);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        fileOutputStream.write(b, off, len);
    }

    @Override
    public void flushAndSync() throws IOException {
        fileOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        fileOutputStream.close();
        parcelFileDescriptor.close();
    }

    @Override
    public void seek(long offset) throws IOException {
        fileOutputStream.getChannel().position(offset);
    }

    @Override
    public void setLength(long newLength) throws IOException {
        if (newLength < fileOutputStream.getChannel().size()) {
            fileOutputStream.getChannel().truncate(newLength);
        }
    }

    public static FileDownloadOutputStream create(Context context, String fileUri) throws IOException {
        return new SAFDocumentFile(context, fileUri);
    }
}
