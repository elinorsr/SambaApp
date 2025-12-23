/**
 * DayFragment is a reusable Fragment class that displays lessons based on a specified level
 * (e.g., Beginners, Advanced, Expert). The level is passed as an argument using {@link #newInstance(String)}.
 *
 * Responsibilities:
 * - Display a list of lessons for a given level using RecyclerView
 * - React to lesson selection events (open detail screen)
 * - Integrate with {@link LessonViewModel} and observe real-time updates from Firestore
 * - Supports instructors (edit access) and trainees (read-only view)
 *
 * Part of MVVM architecture: View (Fragment), ViewModel (LessonViewModel), Model (Firestore)
 */

package com.example.sambaapp.lessons.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sambaapp.lessons.model.LessonModel;
import com.example.sambaapp.R;
import com.example.sambaapp.lessons.view.LessonViewModel;
import com.example.sambaapp.lessons.view.LessonDetailsActivity;
import com.example.sambaapp.user.UserManager;

import java.util.ArrayList;

public class DayFragment extends Fragment {
    /** Argument key used to identify lesson level in the fragment arguments bundle */
    private static final String ARG_LEVEL = "level";
    /** The lesson level to filter by (e.g., "Beginners", "Advanced") */
    private String level;
    /** RecyclerView used to display the list of lessons */
    private RecyclerView recyclerView;
    /** Adapter for managing and binding lesson items */
    private LessonAdapter adapter;
    /** ViewModel used to fetch and observe lessons from Firestore */
    private LessonViewModel lessonViewModel;

    /**
     * Factory method to create a new instance of DayFragment with a specified level.
     *
     * @param level The lesson level to filter by
     * @return A new DayFragment instance with the level passed as an argument
     */
    public static DayFragment newInstance(String level) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LEVEL, level);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Lifecycle method called to inflate the view hierarchy and initialize logic.
     * Sets up the RecyclerView, adapter, ViewModel, and LiveData observation.
     *
     * @param inflater LayoutInflater to inflate the layout
     * @param container Parent container
     * @param savedInstanceState Saved state if available
     * @return The root view of the inflated layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_day_lessons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Retrieve the lesson level from arguments
        if (getArguments() != null) {
            level = getArguments().getString(ARG_LEVEL);
        }

        // Determine if user is instructor (used by adapter to control edit access)
        boolean isInstructor = UserManager.isInstructor();

        // Initialize adapter with empty list and role awareness
        adapter = new LessonAdapter(new ArrayList<>(), isInstructor);
        recyclerView.setAdapter(adapter);

        // Set click listeners for lesson items
        adapter.setOnLessonClickListener(new LessonAdapter.OnLessonClickListener() {
            @Override
            public void onPlayClick(LessonModel lesson) {
                // Placeholder for playing a lesson video (optional)
            }

            @Override
            public void onHeartClick(LessonModel lesson) {
                // Placeholder for "favorite" action (optional)
            }

            @Override
            public void onLessonClick(LessonModel lesson) {
                // Open the detailed lesson view
                Intent intent = new Intent(getContext(), LessonDetailsActivity.class);
                intent.putExtra("lesson", lesson);
                startActivity(intent);
            }
        });

        // Initialize ViewModel
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Observe lessons filtered by level and update the UI
        lessonViewModel.getLessonsByLevel(level, true).observe(getViewLifecycleOwner(), lessons -> {
            adapter.updateList(lessons);
        });

        return view;
    }

    /**
     * Lifecycle method called when the fragment resumes visibility.
     * Refreshes the lesson list to ensure data is up to date.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (lessonViewModel != null && level != null) {
            lessonViewModel.refresh(level);  // Refresh lesson list on resume
        }
    }

}
