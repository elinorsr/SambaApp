/**
 * {@code ExpertFragment} is responsible for displaying a list of lessons categorized under the "Expert" level.
 * This fragment observes changes in the lesson list via {@link LessonViewModel} and updates the UI accordingly.
 *
 * <p>Main functionalities include:
 * <ul>
 *     <li>Displaying a RecyclerView of expert-level lessons</li>
 *     <li>Observing LiveData for real-time updates from Firestore</li>
 *     <li>Refreshing lessons on resume and manual trigger</li>
 * </ul>
 *
 * <p>This class implements {@link RefreshableFragment} interface, allowing external triggers for data refresh.
 *
 * <p><strong>Used in:</strong> LessonTabbedActivity (Tab: "Expert")<br>
 * <strong>Layout:</strong> {@code fragment_day.xml}
 */
package com.example.sambaapp.lessons.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sambaapp.R;
import com.example.sambaapp.core.RefreshableFragment;
import com.example.sambaapp.lessons.view.LessonViewModel;
import com.example.sambaapp.user.UserManager;

import java.util.ArrayList;

public class ExpertFragment extends Fragment implements RefreshableFragment {
    /** RecyclerView displaying the list of expert lessons */
    private RecyclerView recyclerView;
    /** Adapter used to bind lesson data to the RecyclerView */
    private LessonAdapter adapter;
    /** ViewModel for managing lesson data */
    private LessonViewModel viewModel; // (Unused, can be removed)
    /** The active ViewModel instance used for observing and refreshing lessons */
    private LessonViewModel lessonViewModel;

    /**
     * Required empty constructor which sets the layout of the fragment.
     */
    public ExpertFragment() {
        super(R.layout.fragment_day);
    }
    /**
     * Initializes the ViewModel instance.
     * This method is called before the UI is created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LessonViewModel.class);
    }


     /**
     * Called when the fragment's view has been created.
     * Responsible for setting up the RecyclerView and observing the lesson list.
     *
     * @param view               The root view of the fragment
     * @param savedInstanceState If the fragment is being re-created, this contains the previous state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recycler_day_lessons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LessonAdapter(new ArrayList<>(), UserManager.isInstructor());
        recyclerView.setAdapter(adapter);

        // Obtain ViewModel and observe expert lessons
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // צפייה ברשימת שיעורים לפי רמה
        lessonViewModel.getLessonsByLevel("Expert",true).observe(getViewLifecycleOwner(), lessons -> {
            adapter.updateList(lessons);
        });
    }

    /**
     * Triggers manual refresh of the expert lessons from Firestore.
     * Part of {@link RefreshableFragment} contract.
     */
    public void refreshLessons() {
        if (lessonViewModel != null) {
            lessonViewModel.refresh("Expert");
        }
    }

    /**
     * Automatically refreshes expert lessons every time the fragment resumes.
     * Useful for keeping the data up-to-date when navigating back.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (lessonViewModel != null) {
            lessonViewModel.refresh("Expert");
        }
    }
    /**
     * Provides access to the ViewModel for external use (e.g., tab manager).
     *
     * @return the ViewModel associated with this fragment
     */
    // בתוך BeginnerFragment
    public LessonViewModel getViewModel() {
        return lessonViewModel;
    }

}
