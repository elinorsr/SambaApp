/**
 * {@code LessonViewActivity} displays a list of dance lessons filtered by difficulty level.
 * <p>
 * The user can switch between "Beginners", "Advanced", and "Expert" tabs to view relevant lessons.
 * If the user is an instructor, a FloatingActionButton is shown to allow adding new lessons.
 * </p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Displays demo and real lessons based on selected tab</li>
 *     <li>Loads user profile image and greeting</li>
 *     <li>Instructor-only FAB for adding new lessons</li>
 *     <li>Integration with {@link LessonViewModel} for observing Firebase data</li>
 * </ul>
 *
 * This activity is intended to be launched after user login.
 *
 * @author Elinor
 */

package com.example.sambaapp.lessons.viewmodel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sambaapp.lessons.fragment.LessonAdapter;
import com.example.sambaapp.lessons.model.LessonModel;
import com.example.sambaapp.R;
import com.example.sambaapp.lessons.view.AddLessonActivity;
import com.example.sambaapp.lessons.view.LessonViewModel;
import com.example.sambaapp.user.UserManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class LessonViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LessonAdapter adapter;
    private FloatingActionButton fabAddLesson;
    private TextView tvHelloUser;
    private ImageView imgProfile;
    private String selectedTab = "Beginners";  // Default level

    private LessonViewModel lessonViewModel;

    /**
     * Initializes the activity, sets up UI components, loads user data and connects ViewModel observers.
     *
     * @param savedInstanceState the previously saved state, if any
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_view);

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recycler_lessons);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LessonAdapter(new ArrayList<>(), UserManager.isInstructor());
        recyclerView.setAdapter(adapter);

        // Display greeting with user name
        tvHelloUser = findViewById(R.id.tv_hello_user);
        imgProfile = findViewById(R.id.img_profile);
        String userName = getIntent().getStringExtra("userName");
        if (userName != null) {
            tvHelloUser.setText("Hello " + userName + "!");
        }

        // Load profile image from SharedPreferences or internal storage
        UserManager.loadProfileImage(this, imgProfile);


        // FAB to add lessons (only visible for instructors)
        fabAddLesson = findViewById(R.id.fab_add_lesson);
        fabAddLesson.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddLessonActivity.class);
            intent.putExtra("lessonLevel", selectedTab);
            startActivity(intent);
        });

        if (!UserManager.isInstructor()) {
            fabAddLesson.hide();
        }

        // Set up ViewModel and observe lesson data
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
        observeLessons();

        // Setup tab click listeners
        findViewById(R.id.tab_beginners).setOnClickListener(v -> {
            selectedTab = "Beginners";
            observeLessons();
        });

        findViewById(R.id.tab_advanced).setOnClickListener(v -> {
            selectedTab = "Advanced";
            observeLessons();
        });

        findViewById(R.id.tab_expert).setOnClickListener(v -> {
            selectedTab = "Expert";
            observeLessons();
        });
    }

    /**
     * Observes lessons for the currently selected tab and updates the adapter list.
     * Adds demo lessons in addition to real lessons retrieved from Firestore.
     */
    private void observeLessons() {
        lessonViewModel.getLessonsByLevel(selectedTab, true).observe(this, lessons -> {
            List<LessonModel> combinedList = new ArrayList<>();

            // Add demo lessons based on level
            if (selectedTab.equals("Beginners")) {
                combinedList.add(new LessonModel("08:00", "Basic Steps", "Forward & Back Basic", 5, 20, false, false, "basic_icon_image"));
                combinedList.add(new LessonModel("09:00", "Side Basic", "Side step flow", 8, 20, false, false, "basic_icon_image"));
            } else if (selectedTab.equals("Advanced")) {
                combinedList.add(new LessonModel("10:00", "Lead & Follow", "Partner connection", 10, 20, false, false, "advanced_icon_image"));
            } else if (selectedTab.equals("Expert")) {
                combinedList.add(new LessonModel("11:00", "Musicality", "Dance with rhythm", 6, 20, false, false, "expert_icon_image"));
            }

            // Append lessons from Firestore
            combinedList.addAll(lessons);
            // Update UI
            adapter.updateList(combinedList);
        });
    }


    /**
     * Refreshes data and reloads user preferences when the activity resumes.
     * Ensures profile image and instructor permissions are up-to-date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        lessonViewModel.refresh(selectedTab);

        // Reload user data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = prefs.getString("user_name", null);
        String age = prefs.getString("user_age", null);
        String email = prefs.getString("user_email", null);
        String role = prefs.getString("user_role", null);
        if (name != null && age != null && email != null && role != null) {
            UserManager.setUserInfo(name, age, email, role);
        }

        // Reload profile image
        UserManager.loadProfileImage(this, imgProfile);

        // Toggle FAB visibility based on instructor status
        if (UserManager.isInstructor()) {
            fabAddLesson.show();
        } else {
            fabAddLesson.hide();
        }
    }




}
