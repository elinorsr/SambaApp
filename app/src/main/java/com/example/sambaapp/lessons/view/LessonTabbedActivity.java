/**
 * {@code LessonTabbedActivity} is the main activity that displays lessons organized by difficulty levels:
 * Beginners, Advanced, and Expert.
 * <p>
 * The activity uses {@link ViewPager2} and {@link TabLayout} to provide a tabbed navigation interface
 * between fragments representing each level. Users with instructor permissions can add new lessons via
 * a FloatingActionButton (FAB).
 * </p>
 *
 * <p>Main features include:</p>
 * <ul>
 *   <li>Tabbed navigation using {@link LessonPagerAdapter}</li>
 *   <li>Launching {@link AddLessonActivity} to create new lessons</li>
 *   <li>Auto-refreshing the corresponding fragment when a new lesson is added</li>
 * </ul>
 *
 * Example usage:
 * <pre>{@code
 * Intent intent = new Intent(context, LessonTabbedActivity.class);
 * startActivity(intent);
 * }</pre>
 *
 * @author Elinor
 */
    package com.example.sambaapp.lessons.view;

    import android.content.Intent;
    import android.os.Bundle;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.viewpager2.widget.ViewPager2;

    import com.example.sambaapp.lessons.model.LessonModel;
    import com.example.sambaapp.lessons.adapter.LessonPagerAdapter;
    import com.example.sambaapp.R;
    import com.example.sambaapp.core.RefreshableFragment;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import com.google.android.material.tabs.TabLayout;
    import com.google.android.material.tabs.TabLayoutMediator;

    public class LessonTabbedActivity extends AppCompatActivity {

        private ViewPager2 viewPager;
        private TabLayout tabLayout;
        private FloatingActionButton fabAdd;
        private LessonPagerAdapter adapter;

        /**
         * Called when the activity is first created. Sets up the tab layout, view pager,
         * and click listener for the add lesson FAB.
         *
         * @param savedInstanceState The previously saved instance state, if any.
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_lesson_tabbed);

            tabLayout = findViewById(R.id.tab_layout);
            viewPager = findViewById(R.id.view_pager);
            fabAdd = findViewById(R.id.fab_add);

            adapter = new LessonPagerAdapter(this);
            viewPager.setAdapter(adapter);

            // Attach tab titles to ViewPager2
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("Beginners");
                        break;
                    case 1:
                        tab.setText("Advanced");
                        break;
                    case 2:
                        tab.setText("Expert");
                        break;
                }
            }).attach();

            // Launch AddLessonActivity when FAB is clicked
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddLessonActivity.class);
                intent.putExtra("lessonLevel", getCurrentLevel());  // שליחת הרמה הנוכחית
                addLessonLauncher.launch(intent);
            });
        }

        /**
         * Handles the result returned from {@link AddLessonActivity}.
         * If a lesson was added, refreshes the relevant fragment to show the new lesson.
         */
        private final ActivityResultLauncher<Intent> addLessonLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String level = data.getStringExtra("lessonLevel");
                        boolean added = data.getBooleanExtra("lessonAdded", false);
                        if (added && level != null) {
                            // Construct new LessonModel from returned data
                            LessonModel newLesson = new LessonModel(
                                    data.getStringExtra("time"),
                                    data.getStringExtra("title"),
                                    data.getStringExtra("subtitle"),
                                    data.getStringExtra("description"),
                                    data.getStringExtra("videoPath"),
                                    0,
                                    20,
                                    false,
                                    false,
                                    data.getStringExtra("iconId"),
                                    level // ✅
                            );

                            newLesson.setId(data.getStringExtra("lessonId"));
                            newLesson.setCreatedBy(data.getStringExtra("createdBy"));


                            refreshFragmentByLevel(level);

                        }
                    }
                });


        /**
         * Returns the current lesson level based on the selected tab in {@link ViewPager2}.
         *
         * @return "Beginners", "Advanced", or "Expert" depending on current tab.
         */
        private String getCurrentLevel() {
            switch (viewPager.getCurrentItem()) {
                case 0:
                    return "Beginners";
                case 1:
                    return "Advanced";
                case 2:
                    return "Expert";
                default:
                    return "Beginners";
            }
        }

        /**
         * Refreshes the corresponding fragment's lesson list based on the provided level.
         * This triggers a data reload via {@link RefreshableFragment#refreshLessons()}.
         *
         * @param level The lesson level of the fragment to be refreshed.
         */
        private void refreshFragmentByLevel(String level) {
            if (adapter == null) return;

            if ("Beginners".equals(level) && adapter.getBeginnerFragment() instanceof RefreshableFragment) {
                ((RefreshableFragment) adapter.getBeginnerFragment()).refreshLessons();
            } else if ("Advanced".equals(level) && adapter.getAdvancedFragment() instanceof RefreshableFragment) {
                ((RefreshableFragment) adapter.getAdvancedFragment()).refreshLessons();
            } else if ("Expert".equals(level) && adapter.getExpertFragment() instanceof RefreshableFragment) {
                ((RefreshableFragment) adapter.getExpertFragment()).refreshLessons();
            }
        }




    }

