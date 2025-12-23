/**
 * Activity responsible for adding a new lesson to the system.
 * Allows user to input time, description and pick a video.
 * The lesson is saved to Firestore and the video is saved locally.
 *
 * Dependencies:
 * - Firebase Firestore (cloud storage for metadata)
 * - SharedPreferences (local user data)
 * - Internal Storage (video saving)
 * - UserManager (user context)
 */

package com.example.sambaapp.lessons.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sambaapp.core.LocalStorageManager;
import com.example.sambaapp.R;
import com.example.sambaapp.user.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import android.content.pm.PackageManager;

public class AddLessonActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_PICK = 100;
    /** Uri of the selected video */
    private Uri selectedVideoUri;
    /** User input fields */
    private EditText editTime, editDescription;
    /** VideoView for previewing selected video */
    private VideoView videoPreview;
    /** Buttons for user interaction */
    private Button btnPickVideo, btnSave, btnCancel;
    /** Lesson level passed via Intent */
    private String lessonLevel; // משתנה לשמירת הרמה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);
        // Load user name for header
        TextView tvHeader = findViewById(R.id.tv_header);
        ImageView imgProfile = findViewById(R.id.profile_image_view);

        // טען שם
        String name = UserManager.getName();
        if (name == null || name.equals("Unknown")) {
            name = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getString("user_name", "User");
        }
        tvHeader.setText("Hello " + name);
        // Load user profile image from local storage
        // טען תמונה
        UserManager.loadProfileImage(this, imgProfile);

        // Debug
        String imagePath = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("profile_image_path", null);
        Log.d("PROFILE_DEBUG", "🔍 AddLesson - name: " + name);
        Log.d("PROFILE_DEBUG", "🔍 AddLesson - image path: " + imagePath);

        // Retrieve lesson level from Intent
        // קבלת הרמה מה-Intent
        lessonLevel = getIntent().getStringExtra("lessonLevel");
        if (lessonLevel == null) {
            lessonLevel = "Beginners"; // ברירת מחדל
        }

        Log.d("ADD_LESSON", "Creating lesson for level: " + lessonLevel);
        // Initialize views
        editTime = findViewById(R.id.edit_time);
        editDescription = findViewById(R.id.edit_description);
        videoPreview = findViewById(R.id.video_preview);
        btnPickVideo = findViewById(R.id.btn_pick_video);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // Enable looping on preview video
        videoPreview.setOnPreparedListener(mp -> mp.setLooping(true));

        // Handle navigation back
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> navigateBack());

        // Handle pick video
        btnPickVideo.setOnClickListener(v -> openVideoPicker());
        btnCancel.setOnClickListener(v -> navigateBack());

        // Handle save
        btnSave.setOnClickListener(v -> {
            String time = editTime.getText().toString().trim();
            if (TextUtils.isEmpty(time)) {
                Toast.makeText(this, "Please enter time", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedVideoUri != null) {
                saveLessonToFirestore(selectedVideoUri.toString());
            } else {
                Toast.makeText(this, "Please choose a video first", Toast.LENGTH_SHORT).show();
            }
        });

        requestPermissionsIfNeeded();
    }
    /**
     * Opens the video picker intent to select video from gallery.
     */
    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    /**
     * Handles the result of video picker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_VIDEO_PICK && data != null) {
            Uri sourceUri = data.getData();
            Uri localUri = saveVideoLocally(sourceUri);
            if (localUri != null) {
                selectedVideoUri = localUri;
                videoPreview.setVideoURI(localUri);
                videoPreview.setOnPreparedListener(mp -> {
                    mp.setLooping(true); // אם את רוצה שהווידאו ימשיך בלולאה
                    videoPreview.seekTo(1); // תצוגה ראשונית של פריים ראשון
                    videoPreview.start();   // התחלת ניגון
                });

            } else {
                Toast.makeText(this, "Failed to save video locally", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Saves lesson metadata to Firestore.
     *
     * @param videoPath the local path to the video file
     */
    private void saveLessonToFirestore(String videoPath) {
        String description = editDescription.getText().toString();
        String time = editTime.getText().toString().trim();
        String title = description.length() > 15 ? description.substring(0, 15) + "..." : description;

        String name = UserManager.getName();
        if (name == null) name = "unknown user";
        String subtitle = "Added by " + name;

        Map<String, Object> lessonData = new HashMap<>();
        lessonData.put("time", time);
        lessonData.put("title", title);
        lessonData.put("subtitle", subtitle);
        lessonData.put("description", description);
        lessonData.put("videoPath", videoPath);
        lessonData.put("likes", 0);
        lessonData.put("maxParticipants", 20);
        lessonData.put("iconId", "icon_image_dance");
        lessonData.put("level", lessonLevel); // שימוש במשתנה הרמה
        lessonData.put("createdBy", UserManager.getUid());

        Log.d("ADD_LESSON", "Saving lesson with level: " + lessonLevel);

        FirebaseFirestore.getInstance().collection("lessons")
                .add(lessonData)
                .addOnSuccessListener(docRef -> {
                    Log.d("ADD_LESSON", "Lesson saved successfully with ID: " + docRef.getId());

                    // Save locally
                    new LocalStorageManager(this).addCreated(docRef.getId());

                    // Notify success
                    Toast.makeText(this, "Lesson saved!", Toast.LENGTH_SHORT).show();

                    // Return result
                    // החזרת תוצאה עם הרמה כדי שהמסך הקודם יוכל להתעדכן
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("lessonLevel", lessonLevel);
                    resultIntent.putExtra("lessonAdded", true);
                    resultIntent.putExtra("time", time);
                    resultIntent.putExtra("title", title);
                    resultIntent.putExtra("subtitle", subtitle);
                    resultIntent.putExtra("description", description);
                    resultIntent.putExtra("videoPath", videoPath);
                    resultIntent.putExtra("iconId", "icon_image_dance"); // או מה שיש בפועל
                    resultIntent.putExtra("lessonId", docRef.getId());
                    resultIntent.putExtra("createdBy", UserManager.getUid());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("ADD_LESSON", "Failed to save lesson", e);
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Saves the selected video to internal app storage.
     *
     * @param sourceUri the URI of the selected video
     * @return Uri of the saved local file, or null if failed
     */
    private Uri saveVideoLocally(Uri sourceUri) {
        try {
            InputStream in = getContentResolver().openInputStream(sourceUri);
            File dir = new File(getFilesDir(), "videos");
            if (!dir.exists()) dir.mkdirs();

            String fileName = "lesson_" + System.currentTimeMillis() + ".mp4";
            File outFile = new File(dir, fileName);
            OutputStream out = new FileOutputStream(outFile);

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            return Uri.fromFile(outFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * Requests the appropriate permissions for accessing media,
     * depending on Android version.
     */
    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_VIDEO}, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    /**
     * Navigates back to previous activity with canceled result.
     */
    private void navigateBack() {
        setResult(RESULT_CANCELED);
        finish();
    }
}