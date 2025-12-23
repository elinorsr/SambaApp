/**
 * {@code LessonPagerAdapter} is a {@link FragmentStateAdapter} used to manage lesson-level fragments
 * (Beginners, Advanced, Expert) within a {@link androidx.viewpager2.widget.ViewPager2} component.
 * <p>
 * Each fragment represents a category of lessons based on difficulty level, and is reused to optimize performance.
 * </p>
 *
 * <p>Fragment positions:</p>
 * <ul>
 *   <li>Position 0: {@link BeginnerFragment}</li>
 *   <li>Position 1: {@link AdvancedFragment}</li>
 *   <li>Position 2: {@link ExpertFragment}</li>
 * </ul>
 *
 * Example usage:
 * <pre>{@code
 * ViewPager2 viewPager = findViewById(R.id.view_pager);
 * viewPager.setAdapter(new LessonPagerAdapter(this));
 * }</pre>
 *
 * @author Elinor
 */
package com.example.sambaapp.lessons.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.sambaapp.lessons.fragment.ExpertFragment;
import com.example.sambaapp.lessons.view.fragments.AdvancedFragment;
import com.example.sambaapp.lessons.view.fragments.BeginnerFragment;

/**
 * Constructs a new {@code LessonPagerAdapter}.
 *
 * @param fa The hosting {@link FragmentActivity} that owns the ViewPager2.
 */
public class LessonPagerAdapter extends FragmentStateAdapter {

    private final BeginnerFragment beginnerFragment = new BeginnerFragment();
    private final AdvancedFragment advancedFragment = new AdvancedFragment();
    private final ExpertFragment expertFragment = new ExpertFragment();

    public LessonPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    /**
     * Returns the fragment associated with the specified position.
     *
     * @param position The index of the page to instantiate.
     * @return The corresponding {@link Fragment} for the given position.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return beginnerFragment;
            case 1: return advancedFragment;
            case 2: return expertFragment;
            default: return beginnerFragment;
        }
    }

    /**
     * @return The total number of tabs/fragments managed by the adapter.
     */
    @Override
    public int getItemCount() {
        return 3;
    }

    /**
     * @return The instance of {@link BeginnerFragment} managed by this adapter.
     */
    public BeginnerFragment getBeginnerFragment() { return beginnerFragment; }
    /**
     * @return The instance of {@link AdvancedFragment} managed by this adapter.
     */
    public AdvancedFragment getAdvancedFragment() { return advancedFragment; }
    /**
     * @return The instance of {@link ExpertFragment} managed by this adapter.
     */
    public ExpertFragment getExpertFragment() { return expertFragment; }
}

