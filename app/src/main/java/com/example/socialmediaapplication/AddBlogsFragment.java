package com.example.socialmediaapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddBlogsFragment extends Fragment {

    public AddBlogsFragment() {
        // Required empty public constructor
    }

    FirebaseAuth firebaseAuth;
    EditText title, des;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;

    String cameraPermission[];
    String storagePermission[];
    ProgressDialog pd;
    ImageView image;
    Uri imageuri = null;
    String name, email, uid, dp;
    DatabaseReference databaseReference;
    Button upload;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseAuth = FirebaseAuth.getInstance();
        View view = inflater.inflate(R.layout.fragment_add_blogs, container, false);

        title = view.findViewById(R.id.ptitle);
        des = view.findViewById(R.id.pdes);
        image = view.findViewById(R.id.imagep);
        upload = view.findViewById(R.id.pupload);
        pd = new ProgressDialog(getContext());
        pd.setCanceledOnTouchOutside(false);

        // Initialize user data retrieval
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        uid = firebaseAuth.getCurrentUser().getUid();
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    name = dataSnapshot1.child("name").getValue(String.class);
                    email = dataSnapshot1.child("email").getValue(String.class);
                    dp = dataSnapshot1.child("image").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error retrieving user data", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize camera and storage permissions
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Set image click listener
        image.setOnClickListener(v -> showImagePicDialog());

        // Set upload button click listener
        upload.setOnClickListener(v -> {
            String titl = title.getText().toString().trim();
            String description = des.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(titl)) {
                title.setError("Title can't be empty");
                return;
            }
            if (TextUtils.isEmpty(description)) {
                des.setError("Description can't be empty");
                return;
            }
            if (imageuri == null) {
                Toast.makeText(getContext(), "Select an image", Toast.LENGTH_LONG).show();
                return;
            } else {
                uploadData(titl, description);
            }
        });

        return view;
    }

    private void showImagePicDialog() {
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pick Image From");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Camera option
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
            } else if (which == 1) { // Gallery option
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    // Check storage permission
    private Boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Check camera permission
    private Boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Request storage permission
    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    // Request camera permission
    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
                break;
            case STORAGE_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else {
                    Toast.makeText(getContext(), "Storage permission is required", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // Pick image from camera
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageuri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent camerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(camerIntent, IMAGE_PICKCAMERA_REQUEST);
    }

    // Pick image from gallery
    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST);
    }

    // Upload blog data to Firebase
    private void uploadData(final String titl, final String description) {
        pd.setMessage("Publishing Post");
        pd.show();
        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filepathname = "Posts/" + "post" + timestamp;
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child(filepathname);
        storageReference1.putBytes(data).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            uriTask.addOnSuccessListener(downloadUri -> {
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("uid", uid);
                hashMap.put("uname", name);
                hashMap.put("uemail", email);
                hashMap.put("udp", dp);
                hashMap.put("title", titl);
                hashMap.put("description", description);
                hashMap.put("uimage", downloadUri.toString());
                hashMap.put("ptime", timestamp);
                hashMap.put("plike", "0");
                hashMap.put("pcomments", "0");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                databaseReference.child(timestamp).setValue(hashMap)
                        .addOnSuccessListener(aVoid -> {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Published", Toast.LENGTH_LONG).show();
                            title.setText("");
                            des.setText("");
                            image.setImageURI(null);
                            imageuri = null;
                            startActivity(new Intent(getContext(), DashboardActivity.class));
                            getActivity().finish();
                        }).addOnFailureListener(e -> {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Failed to publish", Toast.LENGTH_LONG).show();
                        });
            });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == IMAGEPICK_GALLERY_REQUEST) {
                imageuri = data.getData();
                image.setImageURI(imageuri);
            } else if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                image.setImageURI(imageuri);
            }
        }
    }
}
