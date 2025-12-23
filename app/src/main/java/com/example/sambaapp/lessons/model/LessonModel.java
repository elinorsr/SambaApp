/**
 * Represents a lesson in the SambaApp, containing information such as title, time, description,
 * video link, user registration details, and associated metadata like level and icon.
 * Implements {@link Serializable} to allow easy passing between Android components.
 */
package com.example.sambaapp.lessons.model;

import java.io.Serializable;

public class LessonModel implements Serializable {
    // Core lesson fields
    private String time;
    private String title;
    private String subtitle;
    private String description;
    private String videoUri;
    private String level;
    private String iconId;

    private int registered;
    private int capacity;
    private boolean isFavorite;
    private boolean isPast;
    private String videoPath;

    private String id;

    /**
     * Full constructor including all fields.
     *
     * @param time         The time of the lesson.
     * @param title        The main title of the lesson.
     * @param subtitle     A subtitle or additional descriptor.
     * @param description  A textual description of the lesson's content.
     * @param videoUri     The URI string pointing to the lesson's video.
     * @param registered   Number of users registered to this lesson.
     * @param capacity     Maximum capacity of the lesson.
     * @param isFavorite   Whether the user marked this lesson as a favorite.
     * @param isPast       Whether the lesson is in the past.
     * @param iconId       Identifier for the lesson's icon or visual representation.
     * @param level        The difficulty level (e.g., Beginners, Advanced, Expert).
     */
    public LessonModel(String time, String title, String subtitle, String description, String videoUri,
                       int registered, int capacity, boolean isFavorite, boolean isPast, String iconId,
                       String level) {
        this.time = time;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.videoUri = videoUri;
        this.registered = registered;
        this.capacity = capacity;
        this.isFavorite = isFavorite;
        this.isPast = isPast;
        this.iconId = iconId;
        this.level = level;
    }



    /**
     * Simplified constructor with default description, videoUri, and level.
     */
    // בנאי מקוצר
    public LessonModel(String time, String title, String subtitle,
                       int registered, int capacity, boolean isFavorite, boolean isPast, String iconId) {
        this(time, title, subtitle, "", "", registered, capacity, isFavorite, isPast, iconId, "Beginners"); // או null
    }

    /**
     * Alternate constructor omitting the iconId (uses a default), with full description and videoUri.
     */
    // בנאי חדש ללא iconId
    public LessonModel(String time, String title, String subtitle,
                       String description, String videoUri,
                       int registered, int capacity,
                       boolean isFavorite, boolean isPast) {
        this(time, title, subtitle, description, videoUri,
                registered, capacity, isFavorite, isPast,
                "default_icon_image", "Beginners"); // או null
    }


    // --- Getters and Setters ---
    // Getters
    /** @return The scheduled time of the lesson. */
    public String getTime() {
        return time;
    }
    /** @return Unique ID of the lesson (used for Firestore). */
    public String getId() { return id; }
    /** @param id Set the lesson's unique identifier. */
    public void setId(String id) { this.id = id; }
    /** @return Title of the lesson. */
    public String getTitle() {
        return title;
    }

    /** @return Subtitle or additional descriptor. */
    public String getSubtitle() {
        return subtitle;
    }
    private String createdBy;
    /** @return Full description of the lesson content. */
    public String getDescription() {
        return description;
    }
    /** @return URI string pointing to the lesson's video file. */
    public String getVideoUri() {
        return videoUri;
    }

    /** @return Number of users registered to this lesson. */
    public int getRegistered() {
        return registered;
    }

    /** @return Lesson capacity (maximum allowed users). */
    public int getCapacity() {
        return capacity;
    }

    /** @return Whether the user marked this lesson as favorite. */
    public boolean isFavorite() {
        return isFavorite;
    }

    /** @return Whether the lesson is in the past. */
    public boolean isPast() {
        return isPast;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    /** @return ID of the lesson's icon (used to load from drawable or URL). */
    public String getIconId() {
        return iconId;
    }

    /** @param iconId Set the icon ID. */
    public void setIconId(String iconId) {
        this.iconId = iconId;
    }


    public void setTime(String time) {
        this.time = time;
    }
    public String getLevel() { return level; }
    /** @param level Set the lesson level (e.g., Beginners, Advanced, Expert). *//** @param level Set the lesson level (e.g., Beginners, Advanced, Expert). */
    public void setLevel(String level) { this.level = level; }

    // Setter
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    /** @return The UID or email of the user who created the lesson. */
    public String getCreatedBy() {
        return createdBy;
    }
    /** @param createdBy Set the creator's identifier (Firebase UID or email). */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    /** @return Path to the local video file (if downloaded). */
    public String getVideoPath() {
        return videoPath;
    }
    /** @param videoPath Set the path to the video file. */
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

}
