/**
 * {@code RefreshableFragment} is an interface used to define a contract for
 * fragments that support refreshing their list of lessons.
 * <p>
 * Classes implementing this interface must provide an implementation
 * for {@link #refreshLessons()}, which is typically used to reload or update
 * the fragment's displayed data—e.g., after adding or removing a lesson.
 * <p>
 * This pattern promotes decoupling by allowing the activity or ViewPager
 * to notify individual fragments when a refresh is required.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * public class BeginnersFragment extends Fragment implements RefreshableFragment {
 *     @Override
 *     public void refreshLessons() {
 *         // Reload lesson list here
 *     }
 * }
 * }</pre>
 *
 * @author Elinor
 */
package com.example.sambaapp.core;

public interface RefreshableFragment {
    /**
     * Trigger a refresh of the lesson data shown in the fragment.
     */
    void refreshLessons();
}
