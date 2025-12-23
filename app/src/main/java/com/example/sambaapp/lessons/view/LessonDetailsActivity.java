/**
 * LessonDetailsActivity displays the full details of a selected {@link LessonModel},
 * including its title, subtitle, description, and video content.
 *
 * <p>In addition, the screen presents the currently logged-in user's name and profile image
 * at the top of the screen using {@link UserManager} utilities.
 *
 * <p>This activity is typically launched from the lesson list and expects a LessonModel
 * object via Intent extras.
 *
 * <p><strong>Layout:</strong> {@code activity_lesson_details.xml}
 *
 * <p><strong>Features:</strong>
 * <ul>
 *     <li>Displays lesson information (title, subtitle, description)</li>
 *     <li>Plays lesson video from device storage using {@link VideoView}</li>
 *     <li>Loads user name and profile image from SharedPreferences</li>
 *     <li>Auto-starts video playback and loops on completion</li>
 *     <li>Back button to return to the previous screen</li>
 * </ul>
 *
 * @see LessonModel
 * @see UserManager
 */
package com.example.sambaapp.lessons.view;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sambaapp.lessons.model.LessonModel;
import com.example.sambaapp.R;
import com.example.sambaapp.user.UserManager;

public class LessonDetailsActivity extends AppCompatActivity {

    // UI Components
    TextView title, subtitle, description;
    VideoView videoView;
    Button btnBack;
    TextView tvHeader;
    ImageView imgProfile;



    /**
     * Initializes the screen, loads lesson data from intent extras,
     * displays user info, and starts video playback if available.
     *
     * @param savedInstanceState previous instance state (not used)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_details);

        // View bindings
        title = findViewById(R.id.tv_lesson_title);
        subtitle = findViewById(R.id.tv_lesson_subtitle);
        description = findViewById(R.id.tv_description);
        videoView = findViewById(R.id.video_view);
        btnBack = findViewById(R.id.btn_back);
        tvHeader = findViewById(R.id.tv_header);
        imgProfile = findViewById(R.id.profile_image_view);

        // Load user name (from UserManager or fallback to SharedPreferences)
        String name = UserManager.getName();
        if (name == null || name.equals("Unknown")) {
            name = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getString("user_name", "User");
        }
        tvHeader.setText("Hello " + name);

        // Load user profile image
        UserManager.loadProfileImage(this, imgProfile);


        // Log user info for debugging
        String imagePath = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("profile_image_path", null);
        Log.d("PROFILE_DEBUG", "🔍 Name from prefs: " + name);
        Log.d("PROFILE_DEBUG", "🔍 Image path from prefs: " + imagePath);


        // Get lesson object from intent
        Intent intent = getIntent();
        // קבלת הנתונים מה-Intent
        LessonModel lesson = (LessonModel) getIntent().getSerializableExtra("lesson");

        if (lesson != null) {
            Log.d("LessonDetails", "✔ lesson received: " + lesson.getTitle());
            Log.d("LessonDetails", "🆔 ID: " + lesson.getId()); // <-- בדקי שהוא לא null
            // Display lesson content
            title.setText(lesson.getTitle());
            subtitle.setText(lesson.getSubtitle());
            description.setText(lesson.getDescription());

            // Setup and play video (if exists)
            if (lesson.getVideoPath() != null && !lesson.getVideoPath().isEmpty()) {
                videoView.setVideoURI(Uri.parse(lesson.getVideoPath()));
                videoView.setOnPreparedListener(MediaPlayer::start);
                videoView.setOnCompletionListener(mp -> videoView.start()); // לולאה אם את רוצה
            }
        }


        // Handle back button press
        btnBack.setOnClickListener(v -> finish());
    }
}
