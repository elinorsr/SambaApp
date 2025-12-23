/**
 * {@code DownloadCompleteReceiver} is a {@link BroadcastReceiver} used to listen for system broadcasts
 * when a download initiated by {@link DownloadManager} is completed.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Register to receive download completion broadcast intents.</li>
 *   <li>Validate that the completed download matches the expected ID.</li>
 *   <li>Trigger a callback when the correct download completes.</li>
 * </ul>
 *
 * <p>This receiver is especially useful for cases where the app needs to be notified when a specific
 * download (e.g. a video or file) finishes and take action such as updating the UI or starting playback.
 *
 * Example usage:
 * <pre>{@code
 *     BroadcastReceiver receiver = new DownloadCompleteReceiver(downloadId, () -> {
 *         // handle post-download logic
 *     });
 *     context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
 * }</pre>
 */

package com.example.sambaapp.core;

        import android.app.DownloadManager;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;

public class DownloadCompleteReceiver extends BroadcastReceiver {
    /** The expected ID of the download we're waiting for (returned by DownloadManager.enqueue) */
    private final long expectedDownloadId;
    /** Callback to be executed once the correct download is complete */
    private final Runnable onDownloadComplete;

    /**
     * Constructs a new {@code DownloadCompleteReceiver}.
     *
     * @param expectedDownloadId The ID of the download to listen for
     * @param onDownloadComplete Callback that runs when the download is complete
     */
    public DownloadCompleteReceiver(long expectedDownloadId, Runnable onDownloadComplete) {
        this.expectedDownloadId = expectedDownloadId;
        this.onDownloadComplete = onDownloadComplete;
    }

    /**
     * Called when the receiver gets a broadcast (i.e., when any download finishes).
     * Checks if the received download ID matches the expected one, and if so, runs the callback.
     *
     * @param context The application context
     * @param intent  The received intent from the system
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        long receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (receivedId == expectedDownloadId) {
            onDownloadComplete.run();
        }
    }
}
