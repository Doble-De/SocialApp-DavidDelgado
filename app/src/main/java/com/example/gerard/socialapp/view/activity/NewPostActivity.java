package com.example.gerard.socialapp.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.gerard.socialapp.MediaFiles;
import com.example.gerard.socialapp.R;
import com.example.gerard.socialapp.model.Post;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewPostActivity extends AppCompatActivity {

    static final int RC_IMAGE_TAKE = 8000;
    static final int RC_VIDEO_TAKE = 8001;
    static final int RC_IMAGE_PICK = 9000;
    static final int RC_VIDEO_PICK = 9001;
    static final int RC_AUDIO_PICK = 9002;

    static final int REQUEST_RECORD_AUDIO_PERMISSION = 1212;
    private boolean permissionToRecordAccepted = false;

    EditText mPostTextField;
    ImageView imagePreview;
    Button mPublishButton;
    Button mCameraImageButton;
    Button mCameraVideoButton;
    Button mMicButton;
    Button mImageButton;
    Button mVideoButton;
    Button mAudioButton;
    MediaPlayer mp;

    Uri mFileUri;

    Uri mediaUri;
    String mediaType;
    @SuppressLint("NewApi")
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-YYYY HH:mm");

    DatabaseReference mReference;
    FirebaseUser mUser;

    boolean recording = false;
    private MediaRecorder mRecorder = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
       // mp = MediaPlayer.create(this, R.raw.tirar);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);

        mReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mPostTextField = findViewById(R.id.postText);
        imagePreview = findViewById(R.id.image);
        mPublishButton = findViewById(R.id.publish);
        mImageButton = findViewById(R.id.btnImage);
        mVideoButton = findViewById(R.id.btnVideo);
        mAudioButton = findViewById(R.id.btnAudio);
        mCameraImageButton = findViewById(R.id.btnCameraImage);
        mCameraVideoButton = findViewById(R.id.btnCameraVideo);
        mMicButton = findViewById(R.id.btnMic);

        mPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPost();
            }
        });

        mCameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mCameraVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });

        mMicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(recording){
                    stopRecording();
                } else {
                    startRecording();
                }
                recording = !recording;
            }
        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RC_IMAGE_PICK);
            }
        });

        mVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI), RC_VIDEO_PICK);
            }
        });

        mAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI), RC_AUDIO_PICK);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_IMAGE_TAKE && resultCode == RESULT_OK) {
            mediaUri = mFileUri;
            mediaType = "image";
            setPicture();
        } else if (requestCode == RC_VIDEO_TAKE && resultCode == RESULT_OK) {
            mediaUri = mFileUri;
            mediaType = "video";
            setPicture();
        }

        else if(data != null) {
             if (requestCode == RC_IMAGE_PICK) {
                mediaUri = data.getData();
                mediaType = "image";
                 setPicture();
             } else if (requestCode == RC_VIDEO_PICK) {
                mediaUri = data.getData();
                mediaType = "video";
                 setPicture();
             } else if (requestCode == RC_AUDIO_PICK) {
                mediaUri = data.getData();
                mediaType = "audio";
                 setPicture();
             }
        }
    }

    private void setPicture() {

        Glide.with(this).load(mediaUri).into(imagePreview);
    }

    void submitPost(){
        final String postText = mPostTextField.getText().toString();

        if(postText.isEmpty()){
            mPostTextField.setError("Required");
            return;
        }

        mPublishButton.setEnabled(false);

        if (mediaType == null) {
            writeNewPost(postText, null);
        } else {
            uploadAndWriteNewPost(postText);
        }

    }

    private void writeNewPost(String postText, String mediaUrl) {
        String postKey = mReference.push().getKey();

        Post post = new Post(mUser.getUid(), mUser.getDisplayName(), mUser.getPhotoUrl().toString(), postText, mediaUrl, mediaType, LocalDateTime.now().format(formatter));
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("posts/data/" + postKey, postValues);
        childUpdates.put("posts/all-posts/" + postKey, true);
        childUpdates.put("posts/user-posts/" + mUser.getUid() + "/" + postKey, true);

        mReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
            }
        });
    }

    private void uploadAndWriteNewPost(final String postText){
        if(mediaType != null) {
            FirebaseStorage.getInstance().getReference(mediaType + "/" + UUID.randomUUID().toString() + mediaUri.getLastPathSegment()).putFile(mediaUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return task.getResult().getStorage().getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String downloadUri = task.getResult().toString();
                        writeNewPost(postText, downloadUri);
                    }
                }
            });
        }
    }



    private void dispatchTakePictureIntent() {

        Uri fileUri = null;
        try {
            fileUri = MediaFiles.createFile(this, MediaFiles.Type.IMAGE).uri;
        } catch (IOException ex) {
            // No se pudo crear el fichero
        }

        if (fileUri != null) {
            mFileUri = fileUri;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, RC_IMAGE_TAKE);
        }
    }

    private void dispatchTakeVideoIntent() {

        Uri fileUri = null;
        try {
            fileUri = MediaFiles.createFile(this, MediaFiles.Type.VIDEO).uri;
        } catch (IOException ex) {
            // No se pudo crear el fichero
        }

        if (fileUri != null) {
            mFileUri = fileUri;

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, RC_VIDEO_TAKE);
        }
    }

    void startRecording(){
        MediaFiles.UriPathFile file = null;
        try {
            file = MediaFiles.createFile(this, MediaFiles.Type.AUDIO);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(file != null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(file.path);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {

            }

            mediaType = "audio";
            mediaUri = file.uri;
            mRecorder.start();
        }
    }

    void stopRecording(){
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if(mediaUri != null ) {
            outState.putString("PREVIEW", mediaUri.toString());
            outState.putString("TYPE",mediaType);
        }
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        mediaUri = Uri.parse(savedInstanceState.getString("PREVIEW", null));
        mediaType = savedInstanceState.getString("TYPE", null);
        setPicture();

    }
}
