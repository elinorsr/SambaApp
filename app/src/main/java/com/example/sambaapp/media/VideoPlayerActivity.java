/**
 * VideoPlayerActivity is responsible for playing a lesson video
 * in full screen using {@link VideoView}.
 *
 * <p>The video URI is retrieved from a {@link LessonModel} object
 * passed via an Intent with the key "lesson".</p>
 *
 * <p>The activity automatically starts playback and attaches a
 * {@link MediaController} for playback controls (play, pause, seek, etc.).</p>
 *
 * <p><strong>Note:</strong> In order for {@code getSerializableExtra("lesson")}
 * to work, {@link LessonModel} must implement {@link java.io.Serializable}.
 * Alternatively (and recommended for Android), {@code Parcelable} can be used.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *     Intent intent = new Intent(context, VideoPlayerActivity.class);
 *     intent.putExtra("lesson", lessonModel);
 *     startActivity(intent);
 * </pre>
 *
 * @author Elinor
 */
package com.example.sambaapp.media;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sambaapp.R;
import com.example.sambaapp.lessons.model.LessonModel;

public class VideoPlayerActivity extends AppCompatActivity {

    /** UI component used for video playback */
    private VideoView videoView;

    /**
     * Activity entry point.
     * <ul>
     *     <li>Loads the activity layout</li>
     *     <li>Finds the {@link VideoView} component</li>
     *     <li>Retrieves the {@link LessonModel} from the Intent</li>
     *     <li>Parses the video path string into a {@link Uri}</li>
     *     <li>Sets the URI on the {@link VideoView}</li>
     *     <li>Attaches a {@link MediaController}</li>
     *     <li>Automatically starts video playback</li>
     * </ul>
     *
     * @param savedInstanceState Saved instance state (not used here)
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the layout dedicated to video playback
        setContentView(R.layout.activity_video_player);

        // Bind the VideoView from the XML layout
        videoView = findViewById(R.id.video_view);

        // Retrieve the LessonModel object passed via Intent
        // NOTE: LessonModel must implement Serializable (or Parcelable if refactored)
        LessonModel lesson =
                (LessonModel) getIntent().getSerializableExtra("lesson");

        // Verify that the lesson and its video path exist
        if (lesson != null && lesson.getVideoPath() != null) {

            // Convert the video path string into a Uri
            // (may be content:// or file:// depending on storage method)
            Uri videoUri = Uri.parse(lesson.getVideoPath());

            // Set the video source
            videoView.setVideoURI(videoUri);

            // Attach media playback controls (play, pause, seek, etc.)
            videoView.setMediaController(new MediaController(this));

            // Request focus so media buttons work properly
            videoView.requestFocus();

            // Start video playback automatically
            videoView.start();
        }
    }
}
