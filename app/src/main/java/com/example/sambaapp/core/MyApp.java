/**
 * {@code MyApp} is the custom {@link Application} class for the SambaApp.
 * <p>
 * It stores the application-wide {@link Context} statically,
 * allowing other classes to access a global context
 * even when a component context (like Activity or Service) is not available.
 * <p>
 * This is particularly useful for utility classes or background operations
 * that require a {@link Context} for accessing shared preferences, resources, etc.
 * <p>
 * ⚠️ Note: Use with caution to avoid memory leaks.
 * Always prefer component context when available.
 *
 * Usage Example:
 * <pre>
 * Context appContext = MyApp.getContext();
 * </pre>
 *
 * @author Elinor
 */
package com.example.sambaapp.core;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
    /**
     * Static reference to application context.
     */
    private static Context context;
    /**
     * Initializes the application and saves the context.
     * Called once when the application is launched.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    /**
     * Returns the application-wide context.
     *
     * @return the {@link Context} of the application.
     */
    public static Context getContext() {
        return context;
    }
}
