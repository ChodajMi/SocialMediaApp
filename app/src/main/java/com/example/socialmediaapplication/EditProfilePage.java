package com.example.socialmediaapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditProfilePage extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog pd;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST = 400;
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri imageUri;
    private String profileOrCoverPhoto;

    private ImageView set;
    private TextView profilePic, editName, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_page);

        initializeComponents();
        setupFirebase();

        loadProfileData();

        setupClickListeners();
    }

    private void initializeComponents() {
        profilePic = findViewById(R.id.profilepic);
        editName = findViewById(R.id.editname);
        set = findViewById(R.id.setting_profile_image);
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        editPassword = findViewById(R.id.changepassword);
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void loadProfileData() {
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String image = "" + data.child("image").getValue();
                    Glide.with(EditProfilePage.this).load(image).into(set);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditProfilePage.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        editPassword.setOnClickListener(v -> {
            pd.setMessage("Changing Password");
            showPasswordChangeDialog();
        });

        profilePic.setOnClickListener(v -> {
            pd.setMessage("Updating Profile Picture");
            profileOrCoverPhoto = "image";
            showImagePicDialog();
        });

        editName.setOnClickListener(v -> {
            pd.setMessage("Updating Name");
            showNamePhoneUpdate("name");
        });
    }

    private void showPasswordChangeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null);
        EditText oldPass = view.findViewById(R.id.oldpasslog);
        EditText newPass = view.findViewById(R.id.newpasslog);
        Button editPass = view.findViewById(R.id.updatepass);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        editPass.setOnClickListener(v -> {
            String oldPassword = oldPass.getText().toString().trim();
            String newPassword = newPass.getText().toString().trim();

            if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
                Toast.makeText(EditProfilePage.this, "Both fields must be filled", Toast.LENGTH_LONG).show();
                return;
            }

            dialog.dismiss();
            updatePassword(oldPassword, newPassword);
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        pd.show();
        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), oldPassword);
        firebaseUser.reauthenticate(authCredential).addOnSuccessListener(aVoid -> {
            firebaseUser.updatePassword(newPassword).addOnSuccessListener(aVoid1 -> {
                pd.dismiss();
                Toast.makeText(EditProfilePage.this, "Password Changed", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(EditProfilePage.this, "Error changing password", Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(EditProfilePage.this, "Old password is incorrect", Toast.LENGTH_LONG).show();
        });
    }

    private void showNamePhoneUpdate(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update " + key);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);
        EditText editText = new EditText(this);
        editText.setHint("Enter " + key);
        layout.addView(editText);
        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String value = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(value)) {
                pd.show();
                HashMap<String, Object> result = new HashMap<>();
                result.put(key, value);
                databaseReference.child(firebaseUser.getUid()).updateChildren(result).addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(EditProfilePage.this, "Updated", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(EditProfilePage.this, "Update failed", Toast.LENGTH_LONG).show();
                });
            } else {
                Toast.makeText(EditProfilePage.this, "Please enter a value", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showImagePicDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Camera
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
            } else if (which == 1) { // Gallery
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST) {
                imageUri = data.getData();
                uploadProfileCoverPhoto(imageUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_REQUEST) {
                uploadProfileCoverPhoto(imageUri);
            }
        }
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.setMessage("Uploading...");
        pd.show();

        String filePathAndName = "ProfileImages/" + firebaseUser.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isComplete());
            Uri downloadUri = uriTask.getResult();

            HashMap<String, Object> results = new HashMap<>();
            results.put("image", downloadUri.toString());

            databaseReference.child(firebaseUser.getUid()).updateChildren(results).addOnSuccessListener(aVoid -> {
                pd.dismiss();
                Toast.makeText(EditProfilePage.this, "Image Uploaded", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(EditProfilePage.this, "Error uploading image", Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(EditProfilePage.this, "Error uploading image", Toast.LENGTH_LONG).show();
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery();
            } else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // No need to check if user is logged in here as the activity lifecycle will handle that
    }

    @Override
    protected void onStart() {
        super.onStart();
        // No need to check if user is logged in here as the activity lifecycle will handle that
    }
}
