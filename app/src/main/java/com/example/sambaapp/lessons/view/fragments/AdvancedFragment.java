/**
 * Fragment responsible for displaying lessons categorized under the "Advanced" level.
 *
 * This fragment observes the LiveData from LessonViewModel and updates the RecyclerView
 * accordingly. It supports refreshing the list of lessons either when resumed or manually
 * through the {@link RefreshableFragment} interface.
 *
 * Architecture:
 * - MVVM (Model-View-ViewModel)
 * - ViewModel: {@link LessonViewModel}
 * - Adapter: {@link LessonAdapter}
 * - Data Source: Firebase Firestore
 * - User Context: {@link UserManager}
 *
 * Dependencies:
 * - Firestore (Cloud data source)
 * - LiveData (Reactive updates)
 * - ViewModelProvider (Lifecycle-aware ViewModel)
 * - SharedPreferences/UserManager for role-based UI
 */
package com.example.sambaapp.lessons.view.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sambaapp.lessons.fragment.LessonAdapter;
import com.example.sambaapp.R;
import com.example.sambaapp.core.RefreshableFragment;
import com.example.sambaapp.user.UserManager;
import com.example.sambaapp.lessons.view.LessonViewModel;

import java.util.ArrayList;

public class AdvancedFragment extends Fragment implements RefreshableFragment {
    /** Shared ViewModel that handles lesson logic */
    private LessonViewModel viewModel;
    /** RecyclerView for displaying lessons */
    private RecyclerView recyclerView;
    /** Adapter for binding lessons to RecyclerView */
    private LessonAdapter adapter;
    /** Dedicated ViewModel for this fragment */
    private LessonViewModel lessonViewModel;


    /**
     * Default constructor - loads layout from fragment_day.xml
     */
    public AdvancedFragment() {
        super(R.layout.fragment_day);
    }

    /**
     * Lifecycle method - initializes ViewModel
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LessonViewModel.class);
    }

    /**
     * Lifecycle method - called when the view is created.
     * Sets up RecyclerView and subscribes to lesson updates from ViewModel.
     *
     * @param view Root view of the fragment
     * @param savedInstanceState Saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_day_lessons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create adapter - pass whether the user is an instructor to allow editing
        adapter = new LessonAdapter(new ArrayList<>(), UserManager.isInstructor());
        recyclerView.setAdapter(adapter);

        // Create ViewModel instance
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Observe LiveData of advanced-level lessons and update adapter when changed
        lessonViewModel.getLessonsByLevel("Advanced",true).observe(getViewLifecycleOwner(), lessons -> {
            adapter.updateList(lessons);
        });
    }
    /**
     * Refreshes the lesson list manually.
     * Can be called externally through the RefreshableFragment interface.
     */
    public void refreshLessons() {
        if (lessonViewModel != null) {
            lessonViewModel.refresh("Advanced");
        }
    }
    /**
     * Lifecycle method - called when the fragment becomes visible again.
     * Refreshes the lessons automatically to ensure updated data.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (lessonViewModel != null) {
            lessonViewModel.refresh("Advanced");
        }
    }
    /**
     * Returns the ViewModel instance associated with this fragment.
     *
     * @return The lesson ViewModel
     */
    // בתוך BeginnerFragment
    public LessonViewModel getViewModel() {
        return lessonViewModel;
    }

}
