/**
 * {@code MainActivity} is the entry point of the SambaApp application.
 * It provides registration and login functionality using Firebase Authentication
 * and navigates the user based on their onboarding status (health declaration and settings).
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>User registration via email and password</li>
 *   <li>Login for existing users</li>
 *   <li>Storing and retrieving user data in Firebase Firestore</li>
 *   <li>Validation of required fields</li>
 *   <li>Conditional navigation to Health, Settings, or LessonList screens</li>
 * </ul>
 *
 * <p>All user data such as name, age, email, role, and onboarding progress
 * are stored and retrieved from Firestore under a document with the user's UID.</p>
 *
 * @author Elinor
 */
package com.example.sambaapp.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.sambaapp.R;
import com.example.sambaapp.lessons.view.LessonListActivity;
import com.example.sambaapp.user.HealthActivity;
import com.example.sambaapp.user.SettingsActivity;
import com.example.sambaapp.user.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText fullName, age, emailField, passwordField;
    RadioGroup roleGroup;
    CheckBox healthCheckbox;
    Button continueBtn;
    Button loginExistingBtn;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loads the screen layout from XML
        setContentView(R.layout.activity_main);

        // Initialize UserManager (SharedPreferences) for local user data storage
        UserManager.init();

        // Bind UI components from XML to Java variables
        fullName = findViewById(R.id.edit_full_name);
        age = findViewById(R.id.edit_age);
        emailField = findViewById(R.id.edit_email);
        passwordField = findViewById(R.id.edit_password);
        roleGroup = findViewById(R.id.role_group);
        healthCheckbox = findViewById(R.id.checkbox_health);
        continueBtn = findViewById(R.id.btn_continue);
        loginExistingBtn = findViewById(R.id.btn_login_existing);

        // Get FirebaseAuth instance for login/registration
        auth = FirebaseAuth.getInstance();

        // Enable or disable the "Continue" button based on health declaration checkbox
        healthCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            continueBtn.setEnabled(isChecked);
        });

        // "Continue" button logic:
        // Attempts to sign in; if user does not exist, creates a new user and saves profile to Firestore
        continueBtn.setOnClickListener(v -> {

            // Read values from the form
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String name = fullName.getText().toString().trim();
            String userAge = age.getText().toString().trim();
            boolean isHealthChecked = healthCheckbox.isChecked();
            int selectedRoleId = roleGroup.getCheckedRadioButtonId();

            // Basic validation for required fields
            if (name.isEmpty() || selectedRoleId == -1 || !isHealthChecked) {
                Toast.makeText(this,
                        "To continue you must fill the required fields",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine role based on selected RadioButton
            String role = (selectedRoleId == R.id.radio_guide)
                    ? "Instructor"
                    : "Participant";

            // First attempt to sign in
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Existing user → load profile and continue
                            loadExistingUserData();
                        } else {
                            // Sign-in failed → attempt to create a new user
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(createTask -> {
                                        if (createTask.isSuccessful()) {
                                            // Save new user profile to Firestore
                                            saveNewUserToFirestore(name, userAge, role, email);
                                        } else {
                                            Toast.makeText(this,
                                                    "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
        });

        // "Existing User" button – login only (no registration)
        loginExistingBtn.setOnClickListener(v -> {

            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // Basic validation
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt login
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Load profile and navigate based on onboarding status
                            loadExistingUserData();
                        } else {
                            Toast.makeText(this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    /**
     * Creates and stores a new user profile in Firestore after successful registration.
     * The profile is saved under users/{uid} and includes name, age, role, and email.
     * After saving, local UserManager is updated and the user is navigated
     * to the Health declaration screen.
     */
    private void saveNewUserToFirestore(String name, String age, String role, String email) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return; // Edge case: no authenticated user

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Build user profile map
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("age", age); // TIP: store as Integer if age calculations are needed
        userData.put("role", role);
        userData.put("email", email);

        // Save document users/{uid} in Firestore
        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Save user info locally for fast access
                    UserManager.setUserInfo(name, age, email, role);

                    // New users always start with health declaration
                    goToHealthScreen(name, role);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save user: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    /**
     * Loads the existing user's profile from Firestore (users/{uid}),
     * updates local UserManager, and navigates according to onboarding status:
     *
     * <ul>
     *   <li>If healthDone && settingsDone → Lesson list</li>
     *   <li>If !healthDone → Health screen</li>
     *   <li>Otherwise → Settings screen</li>
     * </ul>
     */
    private void loadExistingUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return; // No authenticated user

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        // Fetch users/{uid} from Firestore
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {

                        String name = document.getString("name");
                        String role = document.getString("role");
                        String age = document.getString("age");

                        Log.d("LOGIN_FLOW",
                                "name=" + name + ", role=" + role + ", age=" + age);

                        // Save locally (SharedPreferences)
                        UserManager.setUserInfo(name, age, user.getEmail(), role);

                        // Onboarding flags (Boolean.TRUE.equals handles null safely)
                        boolean healthDone =
                                Boolean.TRUE.equals(document.getBoolean("healthDone"));
                        boolean settingsDone =
                                Boolean.TRUE.equals(document.getBoolean("settingsDone"));

                        Log.d("LOGIN_FLOW",
                                "healthDone=" + healthDone + ", settingsDone=" + settingsDone);

                        // Navigation logic
                        if (healthDone && settingsDone) {
                            goToLessonsScreen();
                        } else if (!healthDone) {
                            goToHealthScreen(name, role);
                        } else {
                            goToSettingsScreen(name, role);
                        }

                    } else {
                        // User exists in Auth but profile document is missing
                        Toast.makeText(this,
                                "User data not found.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load user data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // --------------------------
    // Navigation helper methods
    // --------------------------

    /**
     * Unused method (identical to goToHealthScreen).
     * Can be safely removed for cleanup.
     */
    private void goToNextScreen(String name, String role) {
        Intent intent = new Intent(MainActivity.this, HealthActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to HealthActivity with required parameters.
     */
    private void goToHealthScreen(String name, String role) {
        Intent intent = new Intent(MainActivity.this, HealthActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to SettingsActivity with required parameters.
     */
    private void goToSettingsScreen(String name, String role) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to LessonListActivity – the main screen after onboarding completion.
     */
    private void goToLessonsScreen() {
        Intent intent = new Intent(MainActivity.this, LessonListActivity.class);
        startActivity(intent);
        finish();
    }
}
