package com.recoded.taqadam.models.storage;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.recoded.taqadam.models.auth.UserAuthHandler;

/**
 * Created by wisam on Dec 16 17.
 */

public class UserStorageHandler {
    private static final String TAG = UserStorageHandler.class.getSimpleName();
    private static final String USERS_FILES_ROOT = "users_files";

    private String mUid;

    private static UserStorageHandler storageHandler;

    public static UserStorageHandler getInstance() {
        if (storageHandler == null) {
            storageHandler = new UserStorageHandler();
        }

        return storageHandler;
    }

    private UserStorageHandler() {
        this.mUid = UserAuthHandler.getInstance().getUid();
    }

    public Task<UploadTask.TaskSnapshot> uploadFile(String fileName, Uri filePath) {
        StorageReference userFilesRef = FirebaseStorage.getInstance().getReference().child(USERS_FILES_ROOT);
        StorageReference imageRef = userFilesRef.child(mUid).child(fileName);
        return imageRef.putFile(filePath);
    }

    public Task<UploadTask.TaskSnapshot> uploadDisplayImage(Uri filePath) throws Exception {
        return uploadFile("display_img.jpg", filePath);
    }
}
