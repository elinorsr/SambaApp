/**
 * LessonAdapter manages the binding of {@link LessonModel} data to views in a RecyclerView.
 *
 * <p>This adapter supports full interactivity for each lesson item, including:
 * <ul>
 *     <li>Viewing details</li>
 *     <li>Marking lessons as watched</li>
 *     <li>Adding/removing from favorites</li>
 *     <li>Editing (for instructors)</li>
 *     <li>Deleting (long press, instructors only)</li>
 * </ul>
 *
 * <p>Favorites and watched statuses are managed via:
 * <ul>
 *     <li>SharedPreferences (local)</li>
 *     <li>Firebase Firestore (remote)</li>
 * </ul>
 *
 * <p>Visual elements such as icons are dynamically selected based on metadata.
 *
 * <p><strong>Layout:</strong> {@code item_lesson.xml}
 *
 * @see LessonModel
 * @see UserManager
 */
package com.example.sambaapp.lessons.fragment;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sambaapp.lessons.model.LessonModel;
import com.example.sambaapp.R;
import com.example.sambaapp.core.LocalStorageManager;
import com.example.sambaapp.lessons.view.EditLessonActivity;
import com.example.sambaapp.lessons.view.LessonDetailsActivity;
import com.example.sambaapp.user.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<LessonModel> lessonList;
    private boolean isInstructor;
    private OnLessonClickListener listener;
    public void updateList(List<LessonModel> newList) {
        Log.d("ADAPTER", "🔁 Received " + newList.size() + " lessons");
        for (LessonModel lesson : newList) {
            Log.d("ADAPTER", "📄 Lesson in adapter: " + lesson.getTitle());
        }
        this.lessonList = newList;
        notifyDataSetChanged();
    }

    /**
     * Interface for click events on lesson actions (play, favorite, general click).
     */
    public interface OnLessonClickListener {
        void onPlayClick(LessonModel lesson);
        void onHeartClick(LessonModel lesson);
        void onLessonClick(LessonModel lesson);
    }

    /**
     * Constructs a new LessonAdapter.
     *
     * @param lessonList   the initial list of lessons to display
     * @param isInstructor flag indicating if current user has instructor permissions
     */
    public LessonAdapter(List<LessonModel> lessonList, boolean isInstructor) {
        this.lessonList = lessonList;
        this.isInstructor = isInstructor;
    }

    /**
     * Assigns a listener to handle lesson-level click events.
     *
     * @param listener {@link OnLessonClickListener}
     */
    public void setOnLessonClickListener(OnLessonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        LessonModel lesson = lessonList.get(position);
        Log.d("ADAPTER", "📌 Binding lesson: " + lesson.getTitle() + ", isPast=" + lesson.isPast());



        LocalStorageManager storage = new LocalStorageManager(holder.itemView.getContext());

        // Set lesson details
        holder.tvLessonTitle.setText(lesson.getTitle());
        holder.tvLessonSubtitle.setText(lesson.getSubtitle());
        // תיבת צפייה - סטטוס מהאחסון המקומי
        holder.cbWatched.setChecked(storage.isWatched(lesson.getId()));

        // Toggle watched state in local storage
        holder.cbWatched.setOnCheckedChangeListener((buttonView, isChecked) -> {
            storage.setWatched(lesson.getId(), isChecked);
        });
        // Long press for deletion (only for instructors)
        holder.itemView.setOnLongClickListener(v -> {
            if (!isInstructor) return true; // רק מדריך יכול למחוק

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("מחיקת שיעור")
                    .setMessage("האם את/ה בטוח/ה שברצונך למחוק את השיעור?")
                    .setPositiveButton("מחק", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("lessons")
                                .document(lesson.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // מחיקת הסרטון
                                    if (lesson.getVideoPath() != null) {
                                        File file = new File(lesson.getVideoPath());
                                        if (file.exists()) file.delete();
                                    }

                                    // הסרת השיעור מהרשימה
                                    lessonList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(holder.itemView.getContext(), "השיעור נמחק", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(holder.itemView.getContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("בטל", null)
                    .show();

            return true;
        });

        // Set icon dynamically
        setLessonIcon(holder.ivLessonIcon, lesson.getIconId());


        // לב: אם הזמן עבר
        if (lesson.isPast()) {
            holder.ivHeartButton.setImageResource(R.drawable.ic_heart_filled_black);
            holder.ivHeartButton.setEnabled(false);
        } else {
            holder.ivHeartButton.setImageResource(
                    lesson.isFavorite() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline
            );
            holder.ivHeartButton.setEnabled(true);
        }

        String userId = UserManager.getUid();

        if (UserManager.isFavorite(holder.itemView.getContext(), userId, lesson.getId())) {
            holder.ivHeartButton.setImageResource(R.drawable.ic_heart_filled);
            lesson.setFavorite(true);
        } else {
            holder.ivHeartButton.setImageResource(R.drawable.ic_heart_outline);
            lesson.setFavorite(false);
        }

        // Play button
        holder.ivPlayButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), LessonDetailsActivity.class);
            intent.putExtra("lesson", lesson); // lesson מממש Serializable
            holder.itemView.getContext().startActivity(intent);
            if (listener != null) {
                listener.onPlayClick(lesson);
            }
        });

        // Favorite (heart) toggle
        holder.ivHeartButton.setOnClickListener(v -> {
            if (!lesson.isPast()) {
                boolean isNowFav = !lesson.isFavorite();
                lesson.setFavorite(isNowFav);

                if (isNowFav) {
                    UserManager.saveFavorite(holder.itemView.getContext(), userId, lesson.getId());
                } else {
                    UserManager.removeFavorite(holder.itemView.getContext(), userId, lesson.getId());
                }

                notifyItemChanged(position);
                if (listener != null) {
                    listener.onHeartClick(lesson);

                    // הוספה או הסרה לפי סטטוס
                    if (lesson.isFavorite()) {
                        saveFavoriteLessonToUser(lesson.getId());
                    } else {
                        removeFavoriteLessonFromUser(lesson.getId());
                    }
                }
            }
        });


        // Click on item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLessonClick(lesson);
                Log.d("LessonAdapter", "✅ Clicked lesson: " + lesson.getTitle());

            }
        });

        // Edit icon (visible only to instructors)
        holder.editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EditLessonActivity.class);
            intent.putExtra("lesson", lesson); // נשלח את השיעור
            holder.itemView.getContext().startActivity(intent);
        });


        // כפתור עריכה רק למדריך
        // הצגת כפתור עריכה רק אם המשתמש הוא מדריך
        if (isInstructor) {
            holder.editIcon.setVisibility(View.VISIBLE);

        } else {
            holder.editIcon.setVisibility(View.GONE);
        }


    }

    private void setLessonIcon(ImageView iconView, String iconIdOrTitle) {
        if (iconIdOrTitle == null || iconIdOrTitle.isEmpty()) {
            iconView.setImageResource(R.drawable.person_icon); // ברירת מחדל
            return;
        }

        // ננסה קודם להשתמש בשם האייקון (iconId)
        int resId = iconView.getContext().getResources()
                .getIdentifier(iconIdOrTitle, "drawable", iconView.getContext().getPackageName());

        if (resId != 0) {
            iconView.setImageResource(resId); // הצליח לפי iconId
        } else {
            // fallback חכם לפי תוכן המחרוזת (כלומר כותרת השיעור)
            String lower = iconIdOrTitle.toLowerCase();

            if (lower.contains("basic")) {
                iconView.setImageResource(R.drawable.basic_icon_image);
            } else if (lower.contains("lead")) {
                iconView.setImageResource(R.drawable.advanced_icon_image);
            } else if (lower.contains("turn")) {
                iconView.setImageResource(R.drawable.expert_icon_image);
            } else {
                iconView.setImageResource(R.drawable.icon_image_dance);
            }
        }
    }


    @Override
    public int getItemCount() {
        return lessonList != null ? lessonList.size() : 0;
    }

    /**
     * ViewHolder pattern for lesson item layout.
     */
    public static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvLessonTitle, tvLessonSubtitle;
        ImageView ivLessonIcon;
        ImageButton ivPlayButton, ivHeartButton, editIcon;
        CheckBox cbWatched;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvLessonSubtitle = itemView.findViewById(R.id.tv_lesson_subtitle);
            ivLessonIcon = itemView.findViewById(R.id.iv_lesson_icon);
            ivPlayButton = itemView.findViewById(R.id.iv_play_button);
            ivHeartButton = itemView.findViewById(R.id.iv_heart_button);

            editIcon = itemView.findViewById(R.id.btn_edit);
            cbWatched = itemView.findViewById(R.id.cb_watched); // ✅ הוספנו כאן
        }
    }
    /**
     * Saves a lesson as a favorite for the current user in Firestore.
     *
     * @param lessonId lesson ID to save
     */
    public static void saveFavoriteLessonToUser(String lessonId) {
        String userId = UserManager.getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("favorites")
                .document(lessonId)
                .set(new HashMap<String, Object>()); // אפשר לשים גם timestamp
    }

    /**
     * Removes a lesson from the user's favorite list in Firestore.
     *
     * @param lessonId lesson ID to remove
     */
    public static void removeFavoriteLessonFromUser(String lessonId) {
        String userId = UserManager.getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("favorites")
                .document(lessonId)
                .delete();
    }

}
