
/**
 * {@code EditLessonActivity} allows instructors to update, delete, or upload a video for an existing lesson.
 * This screen is launched when the user selects to edit a lesson's details from the lesson list.
 *
 * <p>Main functionalities include:
 * <ul>
 *     <li>Prefilling fields based on current lesson data</li>
 *     <li>Allowing instructors to update lesson title, subtitle, description, and video</li>
 *     <li>Saving updates to Firestore</li>
 *     <li>Deleting the lesson from Firestore and local video file from internal storage</li>
 *     <li>Previewing selected video before saving</li>
 * </ul>
 *
 * <p><strong>Permissions required:</strong> Read access to external storage (for video selection).
 */
package com.example.sambaapp.lessons.view;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ImageView;

import com.example.sambaapp.lessons.model.LessonModel;
import com.example.sambaapp.R;
import com.example.sambaapp.user.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EditLessonActivity extends AppCompatActivity {
    /** UI preview component for the selected or existing video */
    private VideoView videoPreview;
    /** Request code used for video picker intent */
    private static final int PICK_VIDEO_REQUEST = 1;
    /** URI of the newly selected video (optional) */
    private Uri selectedVideoUri = null;
    // UI Elements
    EditText etTitle, etSubtitle, etDescription;

    ImageButton btnSave;
    /** The lesson being edited (received from Intent) */
    LessonModel lesson;

    /**
     * Initializes the activity and binds lesson data to the UI.
     * Sets up listeners for Save, Delete, Upload, and Back buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lesson);

        // Load profile image (optional, for better UX)
        ImageView profileImage = findViewById(R.id.profile_image);  // ודאי שיש ID כזה ב־activity_edit_lesson.xml



        UserManager.loadProfileImage(this, profileImage);
        if (profileImage.getDrawable() != null) {
            Log.d("PROFILE_DEBUG", "תמונה נטענה ל־ImageView");
        } else {
            Log.d("PROFILE_DEBUG", "ImageView ריק");
        }

        // UI fields binding
        etTitle = findViewById(R.id.et_title);
        etSubtitle = findViewById(R.id.et_subtitle);
        etDescription = findViewById(R.id.et_description);

        TextView tvLessonName = findViewById(R.id.tv_lesson_name);

        // Return to previous screen
        btnSave = findViewById(R.id.btn_save);
        videoPreview = findViewById(R.id.video_preview);
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        Button btnUpload = findViewById(R.id.btn_upload);

        // Get lesson from Intent
        lesson = (LessonModel) getIntent().getSerializableExtra("lesson");

        if (lesson != null) {
            etTitle.setText(lesson.getTitle());
            etSubtitle.setText(lesson.getSubtitle());
            etDescription.setText(lesson.getDescription());

            tvLessonName.setText(lesson.getTitle());
        }

        if (lesson != null && lesson.getVideoUri() != null) {
            videoPreview.setVideoPath(lesson.getVideoUri());
            videoPreview.seekTo(1); // Show thumbnail preview
        }
        btnSave.setOnClickListener(v -> {
            if (lesson != null) {
                String newTitle = etTitle.getText().toString().trim();
                String newSubtitle = etSubtitle.getText().toString().trim();
                String newDesc = etDescription.getText().toString().trim();


                Map<String, Object> data = new HashMap<>();
                data.put("title", newTitle);
                data.put("subtitle", newSubtitle);
                data.put("description", newDesc);
                // ✅ שמירת URI של סרטון חדש אם נבחר
                if (selectedVideoUri != null) {
                    data.put("videoUri", selectedVideoUri.toString());
                }

                FirebaseFirestore.getInstance()
                        .collection("lessons")
                        .document(lesson.getId())  // ודאי שיש Id ב־LessonModel
                        .update(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "שיעור עודכן!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "עדכון נכשל: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
            }
        });
        ImageButton btnDelete = findViewById(R.id.btn_delete);
        // Delete lesson
        btnDelete.setOnClickListener(v -> {
            if (!UserManager.isInstructor()) {
                Toast.makeText(this, "רק מדריך יכול למחוק שיעור", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lesson != null) {
                FirebaseFirestore.getInstance()
                        .collection("lessons")
                        .document(lesson.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // מחיקת סרטון מהאחסון
                            if (lesson.getVideoPath() != null) {
                                File file = new File(lesson.getVideoPath());
                                if (file.exists() && file.delete()) {
                                    Log.d("DELETE", "📹 הסרטון נמחק מהטלפון");
                                } else {
                                    Log.w("DELETE", "⚠️ לא הצליח למחוק את הסרטון");
                                }
                            }

                            Toast.makeText(this, "השיעור נמחק", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
        // Upload new video
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
            startActivityForResult(intent, PICK_VIDEO_REQUEST);
        });



    }

    /**
     * Converts URI to absolute file system path.
     *
     * @param uri The content URI
     * @return Absolute file path, or null if failed
     */
    private String getPathFromUri(Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    /**
     * Handles result from video picker intent. Previews selected video.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedVideoUri = data.getData();
            videoPreview.setVideoURI(selectedVideoUri);
            videoPreview.start();

            videoPreview.seekTo(1); // Preview frame
            Toast.makeText(this, "סרטון נבחר", Toast.LENGTH_SHORT).show();
        }
    }


}
