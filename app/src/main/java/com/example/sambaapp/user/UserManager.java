/**
 * UserManager is a utility class responsible for managing user-related data
 * such as name, age, role (Instructor or not), email, profile image URI,
 * and favorite lessons. It interacts with FirebaseAuth for user identity
 * and SharedPreferences for local persistence.
 *
 * This class is used across the application to provide consistent access
 * to user information.
 *
 * Author: Elinor
 */
/**
 * UserManager – מחלקת שירות סטטית שמרכזת את ניהול נתוני המשתמש:
 * שם, גיל, תפקיד (Instructor/לא), אימייל, URI של תמונת פרופיל, ושיעורים מועדפים.
 *
 * מקורות נתונים:
 * - FirebaseAuth: לזהות את המשתמש המחובר (UID/אובייקט משתמש).
 * - SharedPreferences: לשמירה מקומית מתמשכת של נתוני משתמש (טעינה מהירה ו-offline).
 *
 * שימוש נפוץ:
 * - setUserInfo(...) כשמתחברים/טוענים פרופיל – שומר בזיכרון וגם ב-SharedPreferences.
 * - getUid() כדי לקבל UID עבור פעולות Firestore/מפתחות Prefs.
 * - loadProfileImage(...) כדי להטעין תמונת פרופיל (עם Glide) לאווטאר.
 *
 * NOTE: המחלקה סטטית; אין יצירת מופעים. הסתמכות על מצב סטטי (name/role וכו') נוחה אך דורשת
 *       הקפדה לעדכן דרך setUserInfo אחרי התחברות/טעינה מ-Firestore כדי למנוע "מידע ישן".
 */
package com.example.sambaapp.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.sambaapp.core.MyApp;
import com.example.sambaapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class UserManager {
    // --- שדות מצב (in-memory cache) לשימוש מהיר לאורך חיי האפליקציה ---
    /** שם המשתמש המלא */
    private static String name;
    /** גיל המשתמש (כמחרוזת) */
    private static String age;
    /** האם המשתמש מדריך (נגזר מ-role) */
    private static boolean isInstructor;
    /** כתובת אימייל של המשתמש */
    private static String email;
    /** תפקיד המשתמש (למשל "Instructor", "Participant"...) */
    private static String role;
    /** נתיב/URI של תמונת הפרופיל */
    private static String imageUri;
    /** אובייקט המשתמש המחובר מ-FirebaseAuth (עשוי להיות null אם לא מחובר) */
    private static FirebaseUser user;

    /**
     * קובע מידע משתמש ומ Persist אותו ל-SharedPreferences.
     * כולל "נורמליזציה" של role: אם מגיע "Guide" – ממירים ל-"Instructor".
     *
     * @param nameInput  שם מלא
     * @param ageInput   גיל
     * @param emailInput אימייל
     * @param roleInput  תפקיד (למשל Guide/Instructor, Participant...)
     *
     * NOTE: ודאי תאימות שמות התפקידים לכללי ה-Firestore Rules. אם ה-Rules בודקים "Guide",
     *       אפשר לשקול לשמור גם ערך "מקורי" וגם "מנורמל" או ליישר מונחים בכל האפליקציה.
     */
    public static void setUserInfo(String nameInput, String ageInput, String emailInput, String roleInput) {
        name = nameInput;
        age = ageInput;
        email = emailInput;

        // ממפה "Guide" ל-"Instructor" לצורך אחידות בצד הלקוח
        if ("Guide".equalsIgnoreCase(roleInput)) {
            roleInput = "Instructor";
        }

        role = roleInput;
        isInstructor = roleInput != null && roleInput.equalsIgnoreCase("Instructor");

        // שמירה מתמשכת ב-SharedPreferences (טעינה מהירה בפתיחות הבאות)
        SharedPreferences prefs = MyApp.getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", nameInput);
        editor.putString("user_age", ageInput);
        editor.putString("user_email", emailInput);
        editor.putString("user_role", roleInput);
        editor.putBoolean("user_is_instructor", isInstructor);
        editor.apply();
    }


    /** @return שם המשתמש או "Unknown" אם לא נקבע */
    public static String getName() {
        return name != null ? name : "Unknown";
    }

    /** @return גיל המשתמש (עשוי להיות null אם לא נטען) */
    public static String getAge() {
        return age;
    }

    /** @return אימייל המשתמש */
    public static String getEmail() {
        return email;
    }

    /** @return תפקיד המשתמש */
    public static String getRole() {
        return role;
    }

    /** @return האם המשתמש מדריך (מבוסס role) */
    public static boolean isInstructor() {
        return role != null && role.equalsIgnoreCase("Instructor");
    }


    /**
     * קובע URI/נתיב של תמונת פרופיל בזיכרון (לא שומר אוטומטית ב-Prefs).
     * @param uri נתיב מקומי או URI רשת (Glide יתמודד עם שניהם)
     *
     * TIP: אם רוצים התמדה, אפשר להרחיב לשמירה ב-SharedPreferences כאן.
     */
    public static void setImageUri(String uri) {
        imageUri = uri;
    }

    /** @return URI/נתיב תמונת פרופיל אם קיים, אחרת מחרוזת ריקה */
    public static String getImageUri() {
        return imageUri != null ? imageUri : "";
    }

    /**
     * מחזיר את ה-UID של המשתמש המחובר כעת.
     * @return UID אם קיים משתמש מחובר, אחרת "unknown_uid"
     */
    public static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : "unknown_uid";
    }
    /**
     * אתחול "פנימי" של מצביע המשתמש (user) מתוך FirebaseAuth.
     * רצוי לקרוא בתחילת מסך/אפליקציה כדי לסנכרן את ה-state.
     */
    public static void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
    }
    /** @return אובייקט FirebaseUser אחרון שהאותחל ב-init() (עשוי להיות null) */
    public static FirebaseUser getUser() {
        return user;
    }

    /**
     * טוען את תמונת הפרופיל אל ה-ImageView שסיפקת.
     *
     * סדר נפילה (fallback):
     * 1) מנסה מיקום קובץ שנשמר ב-SharedPreferences תחת מפתח פר־משתמש.
     * 2) אם לא קיים/נפל – מנסה את imageUri ששמור ב-UserManager (אולי הוחזר מ-Firestore).
     * 3) אם עדיין אין – מציב placeholder ברירת מחדל.
     *
     * @param context   קונטקסט לגישה ל-SharedPreferences ול-Glide
     * @param imageView רכיב ה-ImageView שאליו נטעין את התמונה
     וודאי שבמקום בחירת הקובץ בוצע takePersistableUriPermission.
     */
    public static void loadProfileImage(Context context, ImageView imageView) {
        // ⚠️ נדרש UID כדי לטעון נתיב תמונה פר-משתמש (מפתח per user)
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  // ✅ השורה החסרה

        // קוראים מה-Prefs את נתיב הקובץ המקומי (אם נשמר), ואת יתר נתוני הפרופיל ל-cache בזיכרון
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String imagePath = prefs.getString("profile_image_path_" + uid, null);
        name = prefs.getString("user_name", null);
        age = prefs.getString("user_age", null);
        email = prefs.getString("user_email", null);
        role = prefs.getString("user_role", null);

        // "נורמליזציה" של role גם בקריאה (Guide→Instructor)
        if ("Guide".equalsIgnoreCase(role)) {
            role = "Instructor";
        }
        isInstructor = role != null && role.equalsIgnoreCase("Instructor");

        // 1) ניסיון לטעון מקובץ לוקאלי ששמור ב-SharedPreferences
        if (imagePath != null) {
            Log.d("PROFILE_DEBUG", "✔ Trying to load image from: " + imagePath);
            Log.d("PROFILE_DEBUG", "✔ File exists: " + new File(imagePath).exists());

            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Log.d("PROFILE_DEBUG", "Image file exists ✅");
                Glide.with(context)
                        .load(imageFile)// טעינה מקומית – יעיל וללא רשת
                        .placeholder(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(imageView);
                return;
            } else {
                // אם הקובץ כבר לא קיים – מנקים את המפתח כדי למנוע ניסיונות כושלים בעתיד
                Log.d("PROFILE_DEBUG", "Image file does not exist ❌");
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("profile_image_path_" + uid);
                editor.apply();
            }
        }


        // נסיון נוסף לפי imageUri מה־UserManager (למקרה שזה שמור מ-Firestore)
        String fallbackUri = getImageUri();
        if (fallbackUri != null && !fallbackUri.isEmpty()) {
            File fallbackFile = new File(fallbackUri);
            if (fallbackFile.exists()) {
                Log.d("PROFILE_DEBUG", "Using fallback local file ✅");
                Glide.with(context)
                        .load(fallbackFile)
                        .placeholder(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(imageView);
                return;
            } else {
                // לא קובץ מקומי? ננסה כ-URI/URL (Glide תומך גם ב-content:// וגם ב-http(s)://)
                Log.d("PROFILE_DEBUG", "Fallback path is not a file or doesn't exist, try as URL 📡");
                Glide.with(context)
                        .load(fallbackUri)
                        .placeholder(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(imageView);
                return;
            }
        }

        // 3) אין נתיב תקף – מציגים תמונת ברירת מחדל
        imageView.setImageResource(R.drawable.profile_placeholder);
        // TIP: ניתן לשקול שמירת timestamp לעדכון/רענון Cache, או האזנה לשינויים והטענה מחדש.
    }


    // שם קבוע לקובץ ההעדפות
        private static final String PREFS_NAME = "user_prefs";

    // ------------------------------
    // Favorites API – ניהול שיעורים מועדפים פר משתמש (SharedPreferences בלבד)
    // ------------------------------

    /**
     * מסמן שיעור כמועדף עבור משתמש נתון.
     * נשמר ב-SharedPreferences כ-Set<String> תחת מפתח "favorites_{userId}".
     *
     * @param context  קונטקסט לגישה ל-Prefs
     * @param userId   UID של המשתמש
     * @param lessonId מזהה השיעור
     *
     * NOTE: getStringSet מחזיר אובייקט "חי" – לכן יוצרים עותק חדש (HashSet) לפני שינוי (best practice).
     */
        public static void saveFavorite(Context context, String userId, String lessonId) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Set<String> favorites = new HashSet<>(prefs.getStringSet("favorites_" + userId, new HashSet<>()));
            favorites.add(lessonId);
            prefs.edit().putStringSet("favorites_" + userId, favorites).apply();
        }


    /**
     * מסיר שיעור מרשימת המועדפים של המשתמש.
     */
        public static void removeFavorite(Context context, String userId, String lessonId) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Set<String> favorites = new HashSet<>(prefs.getStringSet("favorites_" + userId, new HashSet<>()));
            favorites.remove(lessonId);
            prefs.edit().putStringSet("favorites_" + userId, favorites).apply();
        }

    /**
     * בודק אם שיעור מסומן כמועדף ע"י המשתמש.
     * @return true אם קיים בסט, אחרת false
     */
        public static boolean isFavorite(Context context, String userId, String lessonId) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Set<String> favorites = prefs.getStringSet("favorites_" + userId, new HashSet<>());
            return favorites.contains(lessonId);
        }

    /**
     * מחזיר את כל מזהי השיעורים המועדפים של המשתמש.
     * @return סט של lessonIds (לא מובטח סדר)
     *
     * NOTE: הסט מוחזר ע"י ה-Prefs ויכול להיות "חי"; אם מתכננים לשנותו – מומלץ לעטוף ב-HashSet חדש.
     */
        public static Set<String> getFavorites(Context context, String userId) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getStringSet("favorites_" + userId, new HashSet<>());
        }



}
