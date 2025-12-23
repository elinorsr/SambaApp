/**
 * LessonListActivity displays the main screen for browsing and managing lessons,
 * organized by level tabs ("Beginners", "Advanced", "Expert").
 *
 * <p>This activity is the central hub for both trainees and instructors:
 * <ul>
 *   <li>Trainees can view categorized lessons and navigate between levels</li>
 *   <li>Instructors can add new lessons via the floating action button</li>
 * </ul>
 *
 * <p>Additional features include user greeting, profile picture loading,
 * tab navigation with {@link ViewPager2} and {@link TabLayout}, and access to settings.
 *
 * <p><strong>Data Sources:</strong>
 * <ul>
 *   <li>User information and image path are loaded from {@link SharedPreferences}</li>
 *   <li>Authentication state is checked using {@link FirebaseAuth}</li>
 * </ul>
 *
 * <p><strong>Layout:</strong> {@code activity_lesson_list.xml}
 *
 * @see AddLessonActivity
 * @see DayFragment
 * @see SettingsActivity
 * @see UserManager
 */
package com.example.sambaapp.lessons.view;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.sambaapp.lessons.fragment.DayFragment;
import com.example.sambaapp.R;
import com.example.sambaapp.user.SettingsActivity;
import com.example.sambaapp.user.UserManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

public class LessonListActivity extends AppCompatActivity {

    // UI components
    private TextView tvHelloUser;
    private ImageView ivProfile;
    private ImageButton btnSettings;
    private FloatingActionButton fabAdd;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    // Lesson level categories
    private final String[] levels = {"Beginners", "Advanced", "Expert"};


    /**
     * Initializes the lesson list screen: binds UI elements, sets up tabs,
     * loads user data and profile picture, and handles navigation to
     * add new lessons and settings.
     *
     * @param savedInstanceState previous state, if available
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        // Load user name
        String userName = UserManager.getName();

        // Bind views
        tvHelloUser = findViewById(R.id.tv_hello_user);
        ivProfile = findViewById(R.id.iv_profile);
        btnSettings = findViewById(R.id.btn_settings);
        fabAdd = findViewById(R.id.fab_add);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);


        // Load profile image from SharedPreferences
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String imagePath = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("profile_image_path_" + uid, null);

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.profile_placeholder);
        }

        // Greet user by name
        if (userName != null) {
            tvHelloUser.setText("Hello " + userName + "!");
        }

        // Set up ViewPager with tabs
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return DayFragment.newInstance(levels[position]);
            }

            @Override
            public int getItemCount() {
                return levels.length;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(levels[position]);
        }).attach();

        // Navigate to settings screen
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Floating button to add lesson (Instructor only)
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(LessonListActivity.this, AddLessonActivity.class);

            // הוספת שליחת הרמה לפי הטאב שנבחר
            int currentTabIndex = viewPager.getCurrentItem();
            String level = levels[currentTabIndex];  // levels = {"Beginners", "Advanced", "Expert"}

            intent.putExtra("lessonLevel", level); // Pass current tab level
            startActivity(intent);
        });


    }
    /**
     * Reloads user data and profile image when the activity resumes.
     * Also verifies the user's role to determine visibility of the Add button.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user info from SharedPreferences into UserManager
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = prefs.getString("user_name", null);
        String age = prefs.getString("user_age", null);
        String email = prefs.getString("user_email", null);
        String role = prefs.getString("user_role", null);
        if (name != null && age != null && email != null && role != null) {
            UserManager.setUserInfo(name, age, email, role);
        }
        // Reload profile image
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "";
        String imagePath = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("profile_image_path_" + uid, null);

        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                Glide.with(this)
                        .load(file)
                        .placeholder(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(ivProfile);
            } else {
                // קובץ לא קיים - מחזיר לברירת מחדל
                ivProfile.setImageResource(R.drawable.profile_placeholder);
            }
        } else {
            ivProfile.setImageResource(R.drawable.profile_placeholder);
        }
        // Check instructor role to control Add button visibility
        boolean isInstructor = UserManager.isInstructor();
        fabAdd.setVisibility(isInstructor ? View.VISIBLE : View.GONE);
    }
    }




