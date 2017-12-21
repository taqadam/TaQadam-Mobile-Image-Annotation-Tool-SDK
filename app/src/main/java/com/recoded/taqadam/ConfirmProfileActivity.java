package com.recoded.taqadam;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.UploadTask;
import com.recoded.taqadam.databinding.ActivityConfirmProfileBinding;
import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.recoded.taqadam.models.db.UserDbHandler;
import com.recoded.taqadam.models.storage.UserStorageHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ConfirmProfileActivity extends AppCompatActivity {
    private static final String TAG = ConfirmProfileActivity.class.getSimpleName();
    private static final int ACTIVITY_REQUEST_CODE_FILES = 1990;
    private static final int ACTIVITY_REQUEST_CODE_CAMERA = 1991;
    private static final int ACTIVITY_REQUEST_CODE_GALLERY = 1992;
    private static final String EMAIL_REGEX = "^[a-zA-Z]+[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.{1}[a-zA-Z0-9-.]{2,}(?<!\\.)$";


    private ActivityConfirmProfileBinding binding;
    private User user;
    private OnCompleteListener<Void> mUserCreatedListener;
    private Calendar mCalendar;
    private DatePickerDialog.OnDateSetListener mDatePickedListener;
    private boolean isAnimating = false; // a work around for image animation

    private BottomSheetBehavior mIntentHandlerChooser;
    //Changing picture intents (camera, gallery, file explorer)
    private Intent cameraIntent, galleryIntent, filesIntent;
    private Uri cameraOutputFile; //Because there is no access to it;

    private boolean mEditMode; // This activity Will Be Used for editing profile as well
    private ProgressDialog mCreatingAccountProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthorized();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_confirm_profile);

        this.mEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        if (mEditMode) {
            binding.tvReady.setText("Edit profile");
            binding.tvAlmostThere.setVisibility(View.GONE);
        }
        binding.bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndPush();
            }
        });

        mCalendar = Calendar.getInstance();

        mDatePickedListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDobField();
            }
        };

        binding.etDob.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ConfirmProfileActivity.this,
                        mDatePickedListener,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        binding.spinnerCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    binding.tvSpinnerCitiesError.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mIntentHandlerChooser = BottomSheetBehavior.from(binding.intentsChooserContainer);
        mIntentHandlerChooser.setHideable(true);
        mIntentHandlerChooser.setState(BottomSheetBehavior.STATE_HIDDEN);
        setupPictureIntents();
        binding.bChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIntentHandlerChooser.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    mIntentHandlerChooser.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    mIntentHandlerChooser.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });
        setupUserDbCreationListener();
        setUser();

        mCreatingAccountProgressDialog = new ProgressDialog(this);
        mCreatingAccountProgressDialog.setCancelable(false);
        mCreatingAccountProgressDialog.setTitle("Updating Account");
        mCreatingAccountProgressDialog.setCanceledOnTouchOutside(false);
        mCreatingAccountProgressDialog.setMessage("Please wait");
    }

    private void checkAuthorized() {
        if (UserAuthHandler.getInstance().getCurrentUser() == null) {
            user = new User();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error!");
            builder.setIcon(R.drawable.ic_error);
            builder.setMessage("You are not logged in!");
            builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(ConfirmProfileActivity.this, SigninActivity.class));
                    finish();
                }
            });
            builder.setNegativeButton("Register", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(ConfirmProfileActivity.this, RegisterActivity.class));
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        } else {
            user = UserAuthHandler.getInstance().getCurrentUser();
        }
    }

    private void setUser() {
        binding.setUser(user);
        if (user.getGender() == User.Gender.MALE) {
            binding.spinnerGender.setSelection(1);
        } else if (user.getGender() == User.Gender.FEMALE) {
            binding.spinnerGender.setSelection(2);
        }
        ArrayAdapter adapter = (ArrayAdapter) binding.spinnerCities.getAdapter();
        binding.spinnerCities.setSelection(adapter.getPosition(user.getUserAddress()));
        binding.ivDisplayImage.setTag(user.getPicturePath());
        loadImage();
        if (user.getDateOfBirth() != null) {
            mCalendar.setTime(user.getDateOfBirth());
            updateDobField();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mIntentHandlerChooser.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                Rect outRect = new Rect();
                binding.intentsChooserContainer.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
                    mIntentHandlerChooser.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void updateDobField() {
        String format = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        binding.etDob.setError("");
        binding.etDob.getEditText().setText(sdf.format(mCalendar.getTime()));
    }

    private void validateAndPush() {
        final String EMPTY = "This field is required";
        final String INVALID = "Invalid input";
        final String INVALID_AGE = "You're younger than 13";
        final String INVALID_CITY = "Please select a valid city";

        boolean valid = true;

        //Display Name Validator
        if (binding.etDisplayName.getText().toString().isEmpty()) {
            binding.etDisplayName.setError(EMPTY);
            valid = false;
        } else if (binding.etDisplayName.getText().length() < 4) {
            binding.etDisplayName.setError(INVALID);
            valid = false;
        }

        //Email Validator
        if (binding.etEmail.getEditText().getText().toString().isEmpty()) {
            binding.etEmail.getEditText().setError(EMPTY);
            valid = false;
        } else if (!binding.etEmail.getEditText().getText().toString().matches(EMAIL_REGEX)) {
            binding.etEmail.getEditText().setError(INVALID);
            valid = false;
        }

        //First Name Validator
        if (binding.etFName.getEditText().getText().toString().isEmpty()) {
            binding.etFName.getEditText().setError(EMPTY);
            valid = false;
        } else if (binding.etFName.getEditText().getText().toString().length() < 2) {
            binding.etFName.getEditText().setError(INVALID);
            valid = false;
        }

        //Last Name Validator
        if (binding.etLName.getEditText().getText().toString().isEmpty()) {
            binding.etLName.getEditText().setError(EMPTY);
            valid = false;
        } else if (binding.etLName.getEditText().getText().toString().length() < 2) {
            binding.etLName.getEditText().setError(INVALID);
            valid = false;
        }

        //Phone Number Validator
        if (binding.etPhoneNumber.getEditText().getText().toString().isEmpty()) {
            binding.etPhoneNumber.getEditText().setError(EMPTY);
            valid = false;
        } else if (binding.etPhoneNumber.getEditText().getText().toString().length() < 5) {
            binding.etPhoneNumber.getEditText().setError(INVALID);
            valid = false;
        }

        //DOB Validator
        if (binding.etDob.getEditText().getText().toString().isEmpty()) {
            binding.etDob.setError(EMPTY);
            valid = false;
        } else if (isYoung()) {
            binding.etDob.setError(INVALID_AGE);
            valid = false;
        } else {
            binding.etDob.setError(""); //We need to clear only this field because set to the layout
        }

        //City Validator
        if (binding.spinnerCities.getSelectedItemPosition() == 0) {
            ((TextView) binding.spinnerCities.getSelectedView()).setError(INVALID_CITY);
            binding.tvSpinnerCitiesError.setText(INVALID_CITY);
            valid = false;
        } else {
            binding.tvSpinnerCitiesError.setText(""); //We need to clear only this field because set to the layout
        }

        //Image Validator
        if (binding.ivDisplayImage.getTag() == null) {
            if (!isAnimating) {
                isAnimating = true;
                binding.ivDisplayImage.animate().yBy(-50).setInterpolator(new DecelerateInterpolator()).setDuration(200).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.ivDisplayImage.animate().yBy(50).setInterpolator(new BounceInterpolator()).setDuration(400).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                isAnimating = false;
                            }
                        }).start();
                    }
                }).start();
            }
            valid = false;
        }

        //Push Data
        if (valid) {
            createUserDbEntry();
        }
    }

    private boolean isYoung() {
        Date today = Calendar.getInstance().getTime();
        final double acceptableDiff = 13 * 365.25d * 24 * 3600 * 1000; //13 years * 365 days * 24 hrs * 3600 secs * 1000 ms
        long diff = today.getTime() - mCalendar.getTimeInMillis();

        return diff < acceptableDiff;
    }

    private void setupUserDbCreationListener() {
        mUserCreatedListener = new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mCreatingAccountProgressDialog.dismiss();
                    Toast.makeText(ConfirmProfileActivity.this, "User Created Successfully!", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(ConfirmProfileActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                    finish();
                } else {
                    Log.d(TAG, "Error writing user data to Firebase db: " + task.getException());
                    Toast.makeText(ConfirmProfileActivity.this, "Error updating account", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void loadImage() {
        if (binding.ivDisplayImage.getTag() != null) {
            final Uri uri = (Uri) binding.ivDisplayImage.getTag();
            binding.pbDisplayImage.setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(uri)
                    .placeholder(getResources().getDrawable(R.drawable.no_image))
                    .into(binding.ivDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            binding.pbDisplayImage.setVisibility(View.GONE);
                            binding.ivDisplayImage.setBorderColor(getResources().getColor(R.color.colorPwStrong));
                        }

                        @Override
                        public void onError() {
                            Log.d(TAG, "Picasso couldn't load the image: " + uri);
                        }
                    });
        }
    }

    private void setupPictureIntents() {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        filesIntent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        PackageManager pm = getPackageManager();
        ComponentName cameraComponent = cameraIntent.resolveActivity(pm);
        ComponentName galleryComponent = galleryIntent.resolveActivity(pm);
        ComponentName filesComponent = filesIntent.resolveActivity(pm);

        //Instantiate camera intent
        if (cameraComponent != null) {
            //File creation is passed to the camera button click listener
            //Set the layout
            try {
                String pkgName = cameraComponent.getPackageName();
                String appName = pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 0)).toString();
                Drawable packageIcon = pm.getApplicationIcon(pkgName);
                binding.intentCamera.setText(appName);
                binding.intentCamera.setCompoundDrawablesWithIntrinsicBounds(null, packageIcon, null, null);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            binding.intentCamera.setVisibility(View.GONE);
        }

        //Instantiate gallery intent
        if (galleryComponent != null) {
            galleryIntent.setType("image/*");
            galleryIntent.putExtra(MediaStore.MEDIA_IGNORE_FILENAME, ".nomedia");

            //Set the layout
            try {
                String pkgName = galleryComponent.getPackageName();
                String appName = pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 0)).toString();
                Drawable packageIcon = pm.getApplicationIcon(pkgName);
                binding.intentGallery.setText(appName);
                binding.intentGallery.setCompoundDrawablesWithIntrinsicBounds(null, packageIcon, null, null);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            binding.intentGallery.setVisibility(View.GONE);
        }

        //Set the layout for files intent
        if (filesComponent != null) {
            try {
                String pkgName = filesComponent.getPackageName();
                String appName = pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 0)).toString();
                Drawable packageIcon = pm.getApplicationIcon(pkgName);
                binding.intentFiles.setText(appName);
                binding.intentFiles.setCompoundDrawablesWithIntrinsicBounds(null, packageIcon, null, null);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            binding.intentFiles.setVisibility(View.GONE);
        }

        binding.intentFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(filesIntent, ACTIVITY_REQUEST_CODE_FILES);
                mIntentHandlerChooser.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        binding.intentCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create the file to save the captured image
                File photoFile = createImageFile();

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    //Put Extras
                    cameraOutputFile = FileProvider.getUriForFile(ConfirmProfileActivity.this,
                            getPackageName().concat(".fileprovider"),
                            photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputFile);
                }

                startActivityForResult(cameraIntent, ACTIVITY_REQUEST_CODE_CAMERA);
                mIntentHandlerChooser.setState(BottomSheetBehavior.STATE_HIDDEN);

            }
        });

        binding.intentGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(galleryIntent, ACTIVITY_REQUEST_CODE_GALLERY);
                mIntentHandlerChooser.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "Img_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    fileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (image != null) {
                image.deleteOnExit();
            }
        }

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_REQUEST_CODE_CAMERA:
                    if (cameraOutputFile != null) { //Just to make sure file was created successfully
                        Uri uri = cameraOutputFile;
                        Log.d(TAG, "from camera: " + uri);
                        binding.ivDisplayImage.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
                        binding.pbDisplayImage.setVisibility(View.VISIBLE);
                        uploadImageFileToStorage(uri);
                    } else {
                        Log.d(TAG, "Image file is null");
                    }
                    break;

                case ACTIVITY_REQUEST_CODE_GALLERY:
                case ACTIVITY_REQUEST_CODE_FILES:
                    Uri uri = imageReturnedIntent.getData();
                    Log.d(TAG, "from gallery: " + uri);
                    binding.ivDisplayImage.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
                    binding.pbDisplayImage.setVisibility(View.VISIBLE);
                    uploadImageFileToStorage(uri);
                    break;
            }
        }
    }

    private void uploadImageFileToStorage(Uri uri) {
        //TODO-wisam: In the future make sure to resize the images for faster uploads
        Log.d(TAG, "Uploading display image " + uri);
        try {
            UserStorageHandler.getInstance().uploadDisplayImage(uri)
                    .addOnCompleteListener(ConfirmProfileActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                binding.ivDisplayImage.setTag(task.getResult().getDownloadUrl());
                                loadImage();
                            } else {
                                Log.d(TAG, "Display image upload failed. Reason:" + task.getException());
                                Toast.makeText(ConfirmProfileActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createUserDbEntry() {
        //TODO: CHECK IF THE USER HAS A CONFIRMED COMPLETE PROFILE
        user.setDisplayName(binding.etDisplayName.getText().toString());
        user.setEmailAddress(binding.etEmail.getEditText().getText().toString());
        user.setFirstName(binding.etFName.getEditText().getText().toString());
        user.setLastName(binding.etLName.getEditText().getText().toString());
        user.setPhoneNumber(binding.etPhoneNumber.getEditText().getText().toString());
        user.setDateOfBirth(mCalendar.getTime());
        user.setUserAddress(binding.spinnerCities.getSelectedItem().toString());
        user.setPicturePath((Uri) binding.ivDisplayImage.getTag());
        switch (binding.spinnerGender.getSelectedItemPosition()) {
            case 0:
                user.setGender(User.Gender.NOT_SPECIFIED);
                break;
            case 1:
                user.setGender(User.Gender.MALE);
                break;
            case 2:
                user.setGender(User.Gender.FEMALE);
                break;
        }
        if (!user.isCompleteProfile()) {
            Log.d(TAG, "Creating user");
            UserDbHandler.getInstance().writeNewUser(user).addOnCompleteListener(ConfirmProfileActivity.this, mUserCreatedListener);
            mCreatingAccountProgressDialog.show();
        } else {
            Log.d(TAG, "Updating user");
            UserDbHandler.getInstance().updateUser(user).addOnCompleteListener(ConfirmProfileActivity.this, mUserCreatedListener);
            mCreatingAccountProgressDialog.show();
        }


    }
}

