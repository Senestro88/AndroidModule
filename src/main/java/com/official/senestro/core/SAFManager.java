package com.official.senestro.core;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;

import com.official.senestro.core.utils.AdvanceUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SAFManager {
    private final Context context;
    private final Activity activity;

    public SAFManager(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void selectTree(int requestCode) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
        activity.startActivityForResult(intent, requestCode);
    }

    public ArrayList<HashMap<String, Object>> listTree(Uri treeUri) {
        ArrayList<HashMap<String, Object>> lists = new ArrayList<>();
        if (validUri(treeUri) && isDir(treeUri)) {
            ContentResolver contentResolver = context.getContentResolver();
            String treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri); // Get the document ID of the picked directory
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId); // Build the Uri for the children of the picked directory
            // Query the documents provider to get the children
            String[] columns = new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_SIZE, DocumentsContract.Document.COLUMN_MIME_TYPE};
            Cursor childCursor = contentResolver.query(childrenUri, columns, null, null, null);
            if (childCursor != null) {
                while (childCursor.moveToNext()) {
                    try {
                        // Get the document ID and mime type of each child
                        String id = childCursor.getString(0);
                        String name = childCursor.getString(1);
                        String size = childCursor.getString(2);
                        String mime = childCursor.getString(3);
                        Uri uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("uri", uri);
                        map.put("id", id);
                        map.put("name", name);
                        map.put("size", size);
                        map.put("mime", mime);
                        map.put("realPath", getPathFromUri(uri));
                        lists.add(map);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                AdvanceUtils.closeQuietly(childCursor);
            }
        }
        return lists;
    }

    public ArrayList<HashMap<String, Object>> listChildTree(Uri treeUri, Uri childUri) {
        ArrayList<HashMap<String, Object>> lists = new ArrayList<>();
        if (validUri(treeUri) && validUri(childUri) && isDir(treeUri) && isDir(childUri)) {
            ContentResolver contentResolver = context.getContentResolver();
            String childId = DocumentsContract.getDocumentId(childUri); // Get the child directory's document ID
            Uri childTreeUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, childId); // Build the Uri for listing the child directory's contents
            String[] columns = new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_SIZE, DocumentsContract.Document.COLUMN_MIME_TYPE};
            // Query the documents provider to get the children
            Cursor childCursor = contentResolver.query(childTreeUri, columns, null, null, null); // Query the child directory's contents and update your ListView
            if (childCursor != null) {
                while (childCursor.moveToNext()) {
                    try {
                        // Get the document ID and mime type of each child
                        String id = childCursor.getString(0);
                        String name = childCursor.getString(1);
                        String size = childCursor.getString(2);
                        String mime = childCursor.getString(3);
                        Uri uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("uri", uri);
                        map.put("id", id);
                        map.put("name", name);
                        map.put("size", size);
                        map.put("mime", mime);
                        map.put("realPath", getPathFromUri(uri));
                        lists.add(map);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                AdvanceUtils.closeQuietly(childCursor);
            }
        }
        return lists;
    }

    public boolean validUri(Uri uri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uriDecoded = Uri.parse(URLDecoder.decode(String.valueOf(uri), "UTF-8"));
            String documentId = DocumentsContract.getTreeDocumentId(uriDecoded);
            Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(uriDecoded, documentId);
            Objects.requireNonNull(contentResolver.openInputStream(documentUri)).close();
            return true;
        } catch (Throwable e) {
            // Handle invalid URI
            e.printStackTrace();
        }
        return false;
    }

    public boolean isDir(Uri uri) {
        try {
            String mime = getMimeFromUri(uri);
            return mime != null && mime.equalsIgnoreCase(DocumentsContract.Document.MIME_TYPE_DIR);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isFile(Uri uri) {
        try {
            String mime = getMimeFromUri(uri);
            return mime != null && !mime.equalsIgnoreCase(DocumentsContract.Document.MIME_TYPE_DIR);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getMimeFromUri(Uri uri) {
        String mime = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uriDecoded = Uri.parse(URLDecoder.decode(String.valueOf(uri), "UTF-8"));
            String documentId = DocumentsContract.getTreeDocumentId(uriDecoded);
            Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(uriDecoded, documentId);
            mime = contentResolver.getType(documentUri);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return mime;
    }

    public String getPathFromUri(Uri uri) {
        return AdvanceUtils.convertUriToFilePath(context, uri);
    }

    public void deleteChildUri(Uri treeUri, Uri childUri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            // Check if the child Uri represents a directory
            boolean isDirectory = DocumentsContract.Document.MIME_TYPE_DIR.equals(contentResolver.getType(childUri));
            // Delete the child Uri recursively if it's a directory
            if (isDirectory) {
                deleteUriDirRecursively(contentResolver, treeUri, childUri);
            } else {
                // If it's a file, simply delete the file
                contentResolver.delete(childUri, null, null);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // ================================================= //
    private void deleteUriDirRecursively(ContentResolver contentResolver, Uri treeUri, Uri directoryUri) {
        // Get the list of child documents in the directory
        try {
            Cursor cursor = contentResolver.query(directoryUri, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // Get the document ID of the child document
                    String childDocumentId = cursor.getString(0);
                    // Build the child Uri using the tree Uri and child document ID
                    Uri childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocumentId);
                    // Recursively delete child directories and files
                    if (DocumentsContract.Document.MIME_TYPE_DIR.equals(contentResolver.getType(childUri))) {
                        deleteUriDirRecursively(contentResolver, treeUri, childUri);
                    } else {
                        // Delete the file
                        contentResolver.delete(childUri, null, null);
                    }
                }
                cursor.close();
            }
            // Delete the empty directory after deleting its contents
            contentResolver.delete(directoryUri, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}