/**
 * SettingsActivity handles user profile configuration including
 * name, phone, age, gender, level (e.g., Instructor or Participant), and profile image.
 * It supports loading and saving user data to Firestore, selecting images from the camera or gallery,
 * and storing image paths locally using SharedPreferences.
 *
 * This activity is shown after the health declaration and is required before accessing lessons.
 *
 * Author: Elinor
 */
/**
 * SettingsActivity מטפלת בהגדרות פרופיל משתמש:
 * שם, טלפון, גיל, מגדר, רמה (Instructor/Participant/...), ותמונת פרופיל.
 *
 * הפעילות:
 * - טוענת נתוני משתמש קיימים מ-Firestore (users/{uid})
 * - מאפשרת בחירת תמונה (מצלמה/גלריה) ושמירתה באחסון פנימי
 * - שומרת נתיבים לוקאליים ב-SharedPreferences
 * - מעדכנת את מסמך המשתמש ומנווטת לשיעורים
 *
 * NOTE: הזרימה הזו מוצגת אחרי הצהרת בריאות (health) ולפני גישה לשיעורים.
 */
package com.example.sambaapp.user;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sambaapp.R;
import com.example.sambaapp.lessons.view.LessonListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    /** קוד בקשה לצילום תמונה מהמצלמה (startActivityForResult ישן) */
    private static final int REQUEST_CAMERA = 1;
    /** קוד בקשה לבחירת תמונה מהגלריה (startActivityForResult ישן) */
    private static final int REQUEST_GALLERY = 2;
    /** ה-Uri של תמונת הפרופיל שנבחרה/צולמה (משמש לזיכרון בלבד) */
    private Uri imageUri;
    /** Spinner for selecting user level (e.g., Instructor, Beginner, etc.) */
    /** בוררים (Spinner) לרמה ולמגדר */
    Spinner spLevel, spGender;
    /** שדות קלט: טלפון, גיל, שם */
    EditText editPhone, editAge, editName;
    /** כפתור המשך (Enabled רק כשהשדות תקינים) */
    Button btnContinue;
    /** דגל האם נבחרה תמונה חדשה (משפיע על השמירה ל-Firestore) */
    private boolean isNewImageSelected = false;

    /** יוצר שם קובץ לתמונת פרופיל באחסון פנימי לפי UID */
    private String getProfileImageFilename(String uid) {
        return "profile_" + uid + ".png";
    }

    /**
     * onCreate – הכנת המסך:
     * - קישור רכיבי UI
     * - מילוי Spinners
     * - טעינת נתונים קיימים מ-Firestore
     * - רישום מאזינים לכפתורים ולשדות
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // ברכת שלום לפי שם שמור ב-UserManager (מגיע מהתחברות/טעינת פרופיל מוקדמת)
        TextView greetingText = findViewById(R.id.text_greeting);
        String userName = UserManager.getName();  // נשלף מהמחלקה ששומרת מידע
        if (userName != null && !userName.isEmpty()) {
            greetingText.setText("Hello " + userName);
        }

        // קישור רכיבים
        spLevel = findViewById(R.id.spinner_level);
        spGender = findViewById(R.id.spinner_gender);
        editPhone = findViewById(R.id.edit_phone);
        editAge = findViewById(R.id.edit_age);
        editName = findViewById(R.id.edit_name);
        btnContinue = findViewById(R.id.btn_continue_settings);
        btnContinue.setEnabled(false);// יופעל רק אחרי ולידציה

        // --- מילוי רשימות נגללות ---
        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(
                this, R.array.levels_full, android.R.layout.simple_spinner_item);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLevel.setAdapter(levelAdapter);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        // כפתור חזרה – סיום הפעילות
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // --- טעינת נתוני משתמש קיימים מ-Firestore (users/{uid}) ---

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();


            db.collection("users").document(uid).get()
                    .addOnSuccessListener(document -> {
                        String imageUriStr = document.getString("imageUri");// נתיב קובץ לוקאלי ששמרנו בעבר
                        if (document.exists()) {
                            String name = document.getString("name");
                            String age = document.getString("age");
                            String phone = document.getString("phone");
                            String gender = document.getString("gender");
                            String level = document.getString("level");

                            // שמירה ל-UserManager
                            UserManager.setUserInfo(name, age, user.getEmail(), level);
                            // טעינת תמונת פרופיל אם נשמר נתיב לוקאלי
                            if (imageUriStr != null) {
                                File imgFile = new File(imageUriStr);
                                if (imgFile.exists()) {

                                    imageUri = Uri.fromFile(imgFile);

                                    Glide.with(this)
                                            .load(imgFile)
                                            .placeholder(R.drawable.profile_placeholder)
                                            .circleCrop()
                                            .into((ImageButton) findViewById(R.id.btn_upload));
                                }
                                // NOTE: אם imageUriStr הוא content:// במקום קובץ,
                                //       אפשר לטעון גם אותו ישירות עם Glide (Glide תומכת).
                            }

                            // מילוי שדות UI בהתאם לנתונים
                            if (name != null) editName.setText(name);
                            if (age != null) editAge.setText(age);
                            if (phone != null) editPhone.setText(phone);

                            if (gender != null) {
                                int genderIndex = ((ArrayAdapter) spGender.getAdapter()).getPosition(gender);
                                spGender.setSelection(genderIndex);
                            }

                            if (level != null) {
                                int levelIndex = ((ArrayAdapter) spLevel.getAdapter()).getPosition(level);
                                spLevel.setSelection(levelIndex);
                            }
                        }

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // הפעלת ה-Continue רק כששדות חיוניים מולאו (כאן בודקים בעיקר טלפון וגיל)
        editPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                validateFields();
            }
        });

        editAge.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                validateFields();
            }
        });
        // כפתורי העלאת תמונה: צילום או גלריה
        ImageButton btnUpload = findViewById(R.id.btn_upload);
        TextView textUpload = findViewById(R.id.text_upload);
        loadProfileImageFromPrefs();  // ניסיון לטעון תמונה מה-Prefs (אם נשמרה קודם)


        btnUpload.setOnClickListener(v -> openCamera());// צילום תמונה חדשה
        textUpload.setOnClickListener(v -> openGallery());// בחירת תמונה קיימת

        // כפתור המשך – שמירת נתונים ב-Firestore, עדכון Prefs, ניווט לשיעורים
        btnContinue.setOnClickListener(v -> {
            String enteredName = editName.getText().toString();
            String age = editAge.getText().toString();
            String phone = editPhone.getText().toString();
            String gender = spGender.getSelectedItem().toString();
            String level = spLevel.getSelectedItem().toString();
            boolean isInstructor = level.equalsIgnoreCase("Instructor");

            // עדכון ב-Firestore
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {

                // שמירה גם כ־Uri בזיכרון
                String uid = currentUser.getUid();
                File imageFile = new File(getFilesDir(), getProfileImageFilename(uid));

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // אוסף עדכונים לשמירה במסמך המשתמש
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("name", enteredName);
                updateData.put("age", age);
                updateData.put("phone", phone);
                updateData.put("gender", gender);
                updateData.put("level", level);
                updateData.put("settingsDone", true);// מסמן שהגדרות הושלמו

                // אם נבחרה תמונה חדשה ונשמר קובץ – שמירת הנתיב במסמך וב-Prefs
                String imagePath = imageFile.getAbsolutePath();
                if (isNewImageSelected && imageFile.exists()) {
                    updateData.put("imageUri", imagePath);
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("profile_image_path_" + uid, imagePath)
                            .apply();
                    UserManager.setImageUri(imagePath);  // עדכון זיכרון
                }



                // כתיבת העדכונים ל-Firestore (update לא מוחק שדות שאינם קיימים במפה)
                db.collection("users").document(uid).update(updateData);
                // עדכון UserManager לוקאלי לטעינה מהירה
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                UserManager.setUserInfo(enteredName, age, email, level);

                // ניווט למסך רשימת השיעורים (ניקוי הסטאק למניעת חזרה אחורה)
                Intent intent = new Intent(SettingsActivity.this, LessonListActivity.class);
                intent.putExtra("userName", enteredName);
                intent.putExtra("level", level);
                intent.putExtra("isInstructor", isInstructor);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

        });

        // בדיקה ראשונית להפעלת הכפתור
        validateFields();
    }
    /**
     * Saves the selected/captured image to the app's internal storage.
     *
     * @param bitmap   The image to save
     * @param fileName Name of the file to save
     * @return Full path of the saved file, or null if saving failed
     */
    private String saveImageToInternalStorage(Bitmap bitmap, String fileName) {
        File directory = getFilesDir();
        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            return file.getAbsolutePath(); // זה מה שצריך לשמור ב-SharedPreferences
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * טעינת תמונת הפרופיל מה-SharedPreferences והצגתה ב-UI (אם קיימת).
     * מחפש לפי מפתח פר-משתמש: "profile_image_path_{uid}".
     */
    private void loadProfileImageFromPrefs() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String imagePath = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("profile_image_path_" + uid, null);


        Log.d("PROFILE_DEBUG", "Image URI from prefs: " + imagePath);

        if (imagePath != null) {
            File imgFile = new File(imagePath);
            Log.d("PROFILE_DEBUG", "✔ Checking file exists at: " + imagePath + ", exists=" + imgFile.exists());
            if (imgFile.exists()) {
                Glide.with(this)
                        .load(imgFile)
                        .placeholder(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into((ImageButton) findViewById(R.id.btn_upload));
            } else {
                Log.d("PROFILE_DEBUG", "⚠ imagePath exists=false: " + imagePath);
            }
        } else {
            Log.d("PROFILE_DEBUG", "⚠ imagePath is null");
        }
    }



    /**
     * פתיחת אפליקציית המצלמה לצילום תמונת פרופיל.
     *
     * NOTE: ACTION_IMAGE_CAPTURE מחזיר בדרך כלל "thumbnail" ב-data.getExtras().get("data").
     *       לצילום באיכות מלאה יש צורך לספק Uri יעד עם FileProvider.
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }
    /**
     * פתיחת גלריה לבחירת תמונת פרופיל קיימת.
     *
     * NOTE: ACTION_PICK הוא API ותיק. בגישות מודרניות מומלץ Photo Picker או SAF (ACTION_OPEN_DOCUMENT) + הרשאה מתמשכת.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }
    /**
     /**
     * קליטת תוצאות מהמצלמה/גלריה:
     * - המרה ל-Bitmap
     * - שמירה לקובץ פנימי (profile_{uid}.png)
     * - שמירת נתיב ב-SharedPreferences
     * - עדכון UserManager והצגת התמונה עם Glide
     *
     * NOTE: כאן מתבצע IO על ה-UI Thread עבור קובץ קטן; לקבצים גדולים מומלץ להעביר ל-Background.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            isNewImageSelected = true;
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            File imageFile = new File(getFilesDir(), getProfileImageFilename(uid));

            if (requestCode == REQUEST_CAMERA) {
                // מצלמה מחזירה לרוב thumbnail קטן דרך extras
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                // תצוגה מיידית של התמונה
                Glide.with(this)
                        .load(photo)
                        .circleCrop()
                        .placeholder(R.drawable.profile_placeholder)
                        .into((ImageButton) findViewById(R.id.btn_upload));

                try {
                    if (imageFile.exists()) {
                        imageFile.delete(); // מנקה קובץ קודם
                    }

                    FileOutputStream fos = new FileOutputStream(imageFile);
                    photo.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();

                    String imagePath = imageFile.getAbsolutePath();
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("profile_image_path_" + uid, imagePath)
                            .apply();
                    UserManager.setImageUri(imagePath);
                    imageUri = Uri.fromFile(imageFile);

                    loadProfileImageFromPrefs();// טעינה חוזרת לוודא שהקובץ נשמר כראוי

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_GALLERY) {
                // גלריה מחזירה Uri; קוראים את התוכן ושומרים כקובץ פנימי
                Uri selectedImage = data.getData();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();

                    FileOutputStream fos = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();

                    String imagePath = imageFile.getAbsolutePath();
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("profile_image_path_" + uid, imagePath)
                            .apply();
                    UserManager.setImageUri(imagePath);
                    imageUri = Uri.fromFile(imageFile);

                    Glide.with(this)
                            .load(imageFile)
                            .placeholder(R.drawable.profile_placeholder)
                            .circleCrop()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into((ImageButton) findViewById(R.id.btn_upload));

                    loadProfileImageFromPrefs();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ולידציה להפעלת/כיבוי כפתור Continue.
     * כרגע בודקת רק שהטלפון לא ריק; ניתן להרחיב (שם/גיל/רמה).
     */
    private void validateFields() {
        boolean ok = !TextUtils.isEmpty(editPhone.getText());
        btnContinue.setEnabled(ok);
    }
    /**
     * onResume – נטען שוב את תמונת הפרופיל מה-Prefs למקרה שנעשו שינויים.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImageFromPrefs();  // טוען שוב את התמונה מה־SharedPreferences
    }

}
