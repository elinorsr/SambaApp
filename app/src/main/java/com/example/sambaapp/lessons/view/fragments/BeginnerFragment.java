/**
 * Fragment responsible for displaying all "Beginners" level lessons.
 *
 * This fragment uses a shared {@link LessonViewModel} to fetch data from Firestore,
 * observes LiveData for updates, and updates the UI (RecyclerView) in response.
 * Supports playback, details view, and local interaction via adapter callbacks.
 *
 * Architecture:
 * - MVVM: Fragment (View), ViewModel (Business logic), Firestore (Model/DB)
 * - Reactive with LiveData and ViewModelProvider
 *
 * Features:
 * - Displays lesson list filtered by "Beginners" level
 * - Responds to user interactions: play video, view details, toggle favorite
 * - Observes and reacts to data updates in real-time
 */
package com.example.sambaapp.lessons.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sambaapp.lessons.fragment.LessonAdapter;
import com.example.sambaapp.lessons.model.LessonModel;
import com.example.sambaapp.R;
import com.example.sambaapp.core.RefreshableFragment;
import com.example.sambaapp.user.UserManager;
import com.example.sambaapp.media.VideoPlayerActivity;
import com.example.sambaapp.lessons.view.LessonViewModel;
import com.example.sambaapp.lessons.view.LessonDetailsActivity;

import java.util.ArrayList;

public class BeginnerFragment extends Fragment implements RefreshableFragment {
    /** Shared ViewModel for accessing lesson data */
    private LessonViewModel lessonViewModel; // רק אובייקט אחד!

    /** RecyclerView to display lessons */
    private RecyclerView recyclerView;
    /** Adapter to bind lesson data to RecyclerView */
    private LessonAdapter adapter;

    /**
     * Default constructor - loads layout defined in fragment_day.xml
     */
    public BeginnerFragment() {
        super(R.layout.fragment_day);
    }

    /**
     * Lifecycle method - Initializes the ViewModel once when the fragment is created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel only once (recommended pattern)
        // יצירת ViewModel פעם אחת ב-onCreate
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
    }

    /**
     * Called when the view is created - initializes RecyclerView and binds adapter,
     * sets up click listeners for lesson items, and subscribes to LiveData.
     *
     * @param view The root view of the fragment
     * @param savedInstanceState Optional saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_day_lessons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create adapter with instructor role awareness
        adapter = new LessonAdapter(new ArrayList<>(), UserManager.isInstructor());
        recyclerView.setAdapter(adapter);
        Log.d("UI", "✅ Adapter set!");

        // Adapter click listeners
        adapter.setOnLessonClickListener(new LessonAdapter.OnLessonClickListener() {
            @Override
            public void onPlayClick(LessonModel lesson) {
                // Play lesson video
                Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
                intent.putExtra("lesson", lesson);
                startActivity(intent);
            }

            @Override
            public void onHeartClick(LessonModel lesson) {
                // User liked (favorited) the lesson
                Log.d("BeginnerFragment", "❤️ שיעור עודכן בלב: " + lesson.getTitle());
            }

            @Override
            public void onLessonClick(LessonModel lesson) {
                // Open lesson detail screen
                Intent intent = new Intent(getContext(), LessonDetailsActivity.class);
                intent.putExtra("lesson", lesson);
                startActivity(intent);
            }
        });




        // Observe LiveData from ViewModel for "Beginners" lessons
        // התחברות ל-Observer (פעם אחת בלבד)
        lessonViewModel.getLessonsByLevel("Beginners", true)
                .observe(getViewLifecycleOwner(), lessons -> {
                    Log.d("UI", "🔄 lesson list updated: size = " + lessons.size());
                    for (LessonModel lesson : lessons) {
                        Log.d("UI", "📄 " + lesson.getTitle());
                    }
                    adapter.updateList(lessons);
                    Log.d("UI", "🟨 Adapter item count = " + adapter.getItemCount());
                });
    }
    /**
     * Manual refresh trigger for lessons - can be used externally via RefreshableFragment.
     */
    public void refreshLessons() {
        if (lessonViewModel != null) {
            Log.d("BeginnerFragment", "🔄 רענון ידני של שיעורים");
            lessonViewModel.refresh("Beginners");
        }
    }

    /**
     * Called automatically when the fragment becomes visible again.
     * Refreshes the list to ensure it's always current.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (lessonViewModel != null) {
            Log.d("BeginnerFragment", "🔄 רענון ב-onResume");
            lessonViewModel.refresh("Beginners");
        }
    }
    /**
     * Exposes ViewModel to external classes that might need access to it.
     *
     * @return The ViewModel used by this fragment
     */
    // בתוך BeginnerFragment
    public LessonViewModel getViewModel() {
        return lessonViewModel;
    }

}