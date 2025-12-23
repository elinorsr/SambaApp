/**
 * {@code HealthActivity} is responsible for collecting a user's health declaration before proceeding to app usage.
 *
 * <p>Users are required to check all three health-related checkboxes in order to continue:
 * <ul>
 *     <li>Heart condition declaration</li>
 *     <li>Pregnancy status</li>
 *     <li>Terms of use agreement</li>
 * </ul>
 *
 * <p>If all conditions are accepted, the activity:
 * <ol>
 *     <li>Updates the user's {@code healthDone} flag in Firestore</li>
 *     <li>Navigates to {@link SettingsActivity}</li>
 * </ol>
 *
 * <p><strong>Used after:</strong> User login or registration<br>
 * <strong>Data storage:</strong> Firebase Authentication & Firestore<br>
 * <strong>Layout:</strong> {@code activity_health.xml}
 */
package com.example.sambaapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sambaapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class HealthActivity extends AppCompatActivity {

    /** Checkbox: confirms the user does not suffer from heart problems */
    /** Checkbox: confirms the user is aware of pregnancy-related limitations */
    /** Checkbox: confirms the user agrees to the terms and conditions */
    CheckBox checkHeart, checkPregnant, checkTerms;
    /** Button to confirm health declaration */
    Button btnConfirm;
    /** TextView displaying an error if any checkbox is not marked */
    TextView errorMessage;
    /** Back button to return to the previous screen */
    ImageButton backButton;

    /**
     * Called when the activity is first created.
     * Sets up UI elements, validation logic, and button listeners.
     *
     * @param savedInstanceState Activity state from prior instance (if any)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);
        // ✨ Show welcome message with user name and role
        String name = UserManager.getName();
        String role = UserManager.getRole();
        TextView greeting = findViewById(R.id.text_greeting);
        greeting.setText("Welcome " + name + " (" + role + ")");


        // Link UI components
        checkHeart = findViewById(R.id.checkbox_heart);
        checkPregnant = findViewById(R.id.checkbox_pregnant);
        checkTerms = findViewById(R.id.checkbox_terms);
        btnConfirm = findViewById(R.id.btn_confirm);
        errorMessage = findViewById(R.id.error_message);
        backButton = findViewById(R.id.back_button);

        // Enable or disable confirmation button based on checkbox states
        View.OnClickListener checkboxListener = v -> validateCheckboxes();
        checkHeart.setOnClickListener(checkboxListener);
        checkPregnant.setOnClickListener(checkboxListener);
        checkTerms.setOnClickListener(checkboxListener);

        // Perform initial validation
        validateCheckboxes();

        // Go back to previous screen
        backButton.setOnClickListener(v -> finish());

        // Proceed if all declarations are checked
        btnConfirm.setOnClickListener(v -> {
            if (allChecked()) {
                errorMessage.setVisibility(View.GONE);

                // ✅ Save health declaration state to Firestore
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String uid = currentUser.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(uid).update("healthDone", true);
                }

                // 👉 Move to next screen
                Intent intent = new Intent(HealthActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();

            } else {
                errorMessage.setVisibility(View.VISIBLE);
            }
        });


    }

    /**
     * Validates whether all checkboxes are marked.
     * @return true if all declarations are accepted, false otherwise
     */
    private boolean allChecked() {
        return checkHeart.isChecked() &&
                checkPregnant.isChecked() &&
                checkTerms.isChecked();
    }

    /**
     * Updates the state of the confirm button and hides error message if all conditions are met.
     */
    private void validateCheckboxes() {
        boolean enable = allChecked();
        btnConfirm.setEnabled(enable);

        if (enable) {
            errorMessage.setVisibility(View.GONE);
        }
    }
}
