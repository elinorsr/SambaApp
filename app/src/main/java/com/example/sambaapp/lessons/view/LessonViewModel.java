/**
 * {@code LessonViewModel} is a ViewModel responsible for retrieving, caching,
 * and exposing lesson data grouped by difficulty level using LiveData.
 *
 * <p>It interacts with Firebase Firestore to fetch lesson documents and
 * maintains a map of {@link LiveData} for each level (e.g., "Beginners", "Advanced", "Expert").</p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Fetch lessons from Firestore filtered by level</li>
 *     <li>Expose reactive {@link LiveData} to observe lessons from the UI</li>
 *     <li>Manually refresh lessons per level</li>
 *     <li>Support adding new lessons directly to LiveData</li>
 * </ul>
 *
 * <p>This ViewModel is used in activities like {@link LessonViewActivity}
 * to observe lesson updates and refresh the UI accordingly.</p>
 *
 * @author Elinor
 */
package com.example.sambaapp.lessons.view;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sambaapp.lessons.model.LessonModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class LessonViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Map<String, MutableLiveData<List<LessonModel>>> lessonsMap = new HashMap<>();
    private String currentLevel = ""; // Tracks the current level being loaded

    /**
     * Returns LiveData containing a list of lessons for the given level.
     * Optionally forces refresh from Firestore.
     *
     * @param level        The lesson level (e.g., "Beginners", "Advanced", "Expert")
     * @param forceRefresh If true, forces a new fetch from Firestore
     * @return LiveData containing list of lessons
     */
    public LiveData<List<LessonModel>> getLessonsByLevel(String level, boolean forceRefresh) {
        if (!lessonsMap.containsKey(level)) {
            lessonsMap.put(level, new MutableLiveData<>());
        }
        if (forceRefresh) {
            loadLessons(level);
        }
        return lessonsMap.get(level);
    }



    /**
     * Fetches lessons from Firebase Firestore filtered by the specified level
     * and updates the corresponding LiveData.
     *
     * @param level The lesson level to fetch
     */
    private void loadLessons(String level) {
        Log.d("LESSON_VIEW_MODEL", "🔄 Loading lessons for level: " + level);
        db.collection("lessons")
                .whereEqualTo("level", level)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<LessonModel> lessons = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        LessonModel lesson = new LessonModel(
                                doc.getString("time"),
                                doc.getString("title"),
                                doc.getString("subtitle"),
                                doc.getString("description"),
                                doc.getString("videoPath"),
                                doc.getLong("likes") != null ? doc.getLong("likes").intValue() : 0,
                                doc.getLong("maxParticipants") != null ? doc.getLong("maxParticipants").intValue() : 0,
                                false, false,
                                doc.getString("iconId"),
                                doc.getString("level")
                        );
                        lesson.setLevel(doc.getString("level"));
                        lesson.setId(doc.getId());
                        lesson.setVideoPath(doc.getString("videoPath"));
                        lesson.setCreatedBy(doc.getString("createdBy"));
                        Log.d("LESSON_VIEW_MODEL", "👀 isPast=" + lesson.isPast() + ", title=" + lesson.getTitle());
                        Log.d("LESSON_VIEW_MODEL", "✅ Lesson loaded: " + doc.getString("title") + ", level: " + doc.getString("level"));

                        lessons.add(lesson);
                    }
                    if (lessonsMap.containsKey(level)) {
                        lessonsMap.get(level).setValue(lessons);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LESSON_VIEW_MODEL", "❌ Error loading lessons: " + e.getMessage());
                    if (lessonsMap.containsKey(level)) {
                        lessonsMap.get(level).setValue(new ArrayList<>());
                    }
                });
    }

    /**
     * Adds a single lesson to the existing LiveData list for the given level.
     * This is useful when adding a lesson locally without refetching from Firestore.
     *
     * @param level  The level to which the lesson belongs
     * @param lesson The lesson to add
     */
    public void addLessonDirectly(String level, LessonModel lesson) {
        MutableLiveData<List<LessonModel>> liveData = lessonsMap.get(level);
        if (liveData == null) return;

        List<LessonModel> currentList = liveData.getValue();
        if (currentList == null) currentList = new ArrayList<>();

        List<LessonModel> newList = new ArrayList<>(currentList);
        newList.add(lesson);
        liveData.setValue(newList);
    }


    /**
     * Manually refreshes lesson data from Firestore for the specified level.
     *
     * @param level The level to refresh
     */
    public void refresh(String level) {
        Log.d("LESSON_VIEW_MODEL", "🔄 Manual refresh for level: " + level);
        currentLevel = level; // עדכון הרמה הנוכחית
        loadLessons(level);
    }

}