/**
 * {@code LocalStorageManager} is a utility class responsible for managing local user-specific data
 * using {@link SharedPreferences}. It stores and retrieves:
 * <ul>
 *     <li>Favorite lessons (liked by the user)</li>
 *     <li>Created lessons (lessons added by the user)</li>
 *     <li>Watched lessons (lessons the user has viewed)</li>
 * </ul>
 *
 * <p>This class ensures data persistence across app launches and is used
 * to provide a personalized experience for each user.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * LocalStorageManager storage = new LocalStorageManager(context);
 * storage.addFavorite("lesson123");
 * boolean isFav = storage.isFavorite("lesson123");
 * }</pre>
 *
 * @author Elinor
 */
package com.example.sambaapp.core;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;


public class LocalStorageManager {
    private static final String PREF_NAME = "SambaAppPrefs";
    private static final String KEY_FAVORITES = "favorite_lessons";
    private static final String KEY_CREATED = "created_lessons";
    private static final String KEY_WATCHED = "watched_lessons";

    private final SharedPreferences prefs;

    public LocalStorageManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Favorite lessons
    public void addFavorite(String lessonId) {
        Set<String> favs = getFavorites();
        favs.add(lessonId);
        prefs.edit().putStringSet(KEY_FAVORITES, favs).apply();
    }

    /**
     * Removes a lesson from the favorites list.
     *
     * @param lessonId The ID of the lesson to remove
     */
    public void removeFavorite(String lessonId) {
        Set<String> favs = getFavorites();
        favs.remove(lessonId);
        prefs.edit().putStringSet(KEY_FAVORITES, favs).apply();
    }

    /**
     * Retrieves all lesson IDs marked as favorites.
     *
     * @return A set of favorite lesson IDs
     */
    public Set<String> getFavorites() {
        return new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    /**
     * Checks if a specific lesson is marked as favorite.
     *
     * @param lessonId The ID of the lesson to check
     * @return {@code true} if the lesson is a favorite, {@code false} otherwise
     */
    public boolean isFavorite(String lessonId) {
        return getFavorites().contains(lessonId);
    }

    // --------------------------
    // Created Lessons
    // --------------------------

    /**
     * Adds a lesson ID to the locally stored list of created lessons.
     *
     * @param lessonId The ID of the lesson the user created
     */
    public void addCreated(String lessonId) {
        Set<String> created = getCreatedLessons();
        created.add(lessonId);
        prefs.edit().putStringSet(KEY_CREATED, created).apply();
    }

    /**
     * Retrieves all lesson IDs created by the user.
     *
     * @return A set of lesson IDs created by the user
     */
    public Set<String> getCreatedLessons() {
        return new HashSet<>(prefs.getStringSet(KEY_CREATED, new HashSet<>()));
    }
    // Watched lessons
    public void setWatched(String lessonId, boolean watched) {
        Set<String> watchedSet = getWatchedLessons();
        if (watched) {
            watchedSet.add(lessonId);
        } else {
            watchedSet.remove(lessonId);
        }
        prefs.edit().putStringSet(KEY_WATCHED, watchedSet).apply();
    }


    public boolean isWatched(String lessonId) {
        return getWatchedLessons().contains(lessonId);
    }

    public Set<String> getWatchedLessons() {
        return new HashSet<>(prefs.getStringSet(KEY_WATCHED, new HashSet<>()));
    }

    /**
     * Checks if the user created a specific lesson.
     *
     * @param lessonId The ID of the lesson
     * @return {@code true} if the lesson was created by the user, {@code false} otherwise
     */
    public boolean isCreated(String lessonId) {
        return getCreatedLessons().contains(lessonId);
    }
}
