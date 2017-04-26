package sammobewick.pocketkitchen.communication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import sammobewick.pocketkitchen.supporting.ActivityHelper;

/**
 * Save to Drive Activity.
 * This class handles the uploading of data to the user's Google Drive app-folder. There are two
 * key aspects at play here; editing and creating a new file. Firstly, the superclass will run
 * the query to look for existing files, which will then establish if editing is possible.
 *
 * Editing is conducted through an ASyncTask using the Metadata.
 * Creating files is a configured process with the DriveAPI and requires callback usage instead.
 *
 * The results are asynchonous to prevent the activity being killed, but it is important the
 * activity can display information to the user, so it waits for confirmation everything has worked.
 *
 * Created by Sam on 06/03/2017.
 */
public class SaveDriveActivity extends BaseDriveActivity {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG = "SaveDriveAct";
    private boolean errors;
    private int     result_count;

    /**
     * On a connection, we just want to reset our counter (which would not be useful in the super).
     * @param bundle Bundle - can be null, often will be.
     */
    @Override
    public void onConnected(Bundle bundle) {
        errors = false;
        result_count = 0;
        super.onConnected(bundle);
    }

    /**
     * Inform the user before exiting. There is very little to be done if it errors, but as it's the
     * saving process then re-entering the application
     */
    @Override
    public void finish() {
        if (errors) {
            errors = false;
            ActivityHelper.displayKnownError(SaveDriveActivity.this,
                    "Warning: some files had some issues syncing. It's recommended you don\'t " +
                            "delete your local data or sign in with another account for now.\n" +
                            "If you continue to see this message, please report it.");
        } else
            super.finish();
    }

    /**
     * Inherited from super. Handles a successful or erroneous query from the super.
     * @param success boolean - being whether the operation was successful or not.
     * @param identifier String - the identifier (either for usage or logging).
     */
    @Override
    public void queryResult(boolean success, String identifier) {
        if (!success) {
            // If erroneous, create a new file:
            Log.i(TAG, "Error finding file. Creating new one for " + identifier);
            Drive.DriveApi.newDriveContents(getmGoogleApiClient())
                    .setResultCallback(new NewContentsCallback(identifier));
        } else {
            // If successful, count it:
            Log.i(TAG, "Successfully handled data for " + identifier + " | count = " + result_count);
            result_count += 1;
            if (result_count >= getMaxRequests())
                finish();
        }
    }

    /**
     * Called from super. Create an asynctask to attempt our file editing process.
     * @param meta Metadata - being the result Metadata from the base queries.
     */
    @Override
    public void handleMeta(Metadata meta) {
        new EditFileASync(getmGoogleApiClient()).execute(meta);
    }

    /**
     * ASyncTask for Editing DriveContents.
     */
    private class EditFileASync extends AsyncTask<Metadata, Boolean, Boolean> {
        private String title;
        private GoogleApiClient client;

        /**
         * Constructor, allows us to pass the client to the task.
         * @param client GoogleApiClient - being the client.
         */
        EditFileASync(GoogleApiClient client) {
            this.client = client;
        }

        /**
         * Main process.
         * @param params Metadata - being the data to work with.
         * @return Boolean - if the operation was successful or not.
         */
        @Override
        protected Boolean doInBackground(Metadata... params) {
            title = params[0].getTitle();
            DriveFile   file  = params[0].getDriveId().asDriveFile();
            Object      data  = getData(getID(title));

            // Check there is something to save!
            if (data != null) {
                try {
                    DriveApi.DriveContentsResult contentsResult = file.open(
                            client, DriveFile.MODE_WRITE_ONLY, null).await();

                    // We could still have this operation fail, so insert a quick check:
                    if (!contentsResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Error getting DriveContentsResult, MSG: "
                                + contentsResult.getStatus().getStatusMessage());
                        return false;
                    }

                    // Write our data to the contents:
                    DriveContents driveContents = contentsResult.getDriveContents();
                    OutputStream os = driveContents.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(os);
                    oos.writeObject(data);
                    oos.flush();
                    oos.close();

                    // Commit changes + await status:
                    com.google.android.gms.common.api.Status status =
                            driveContents.commit(client, null).await();

                    return status.getStatus().isSuccess();

                } catch (IOException e) {
                    Log.e(TAG, "IOException when editing file!", e);
                }
            } else { Log.d(TAG, "Skipping over " + title + " due to NULL data."); }

            // If this line is reached, we have errored!
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            // Log + record our result:
            Log.d(TAG, "result of " + result + " for " + title + " from AsyncTask");
            queryResult(result, title);
        }
    }

    /**
     * File callback for when creating a new one. Simply links up with the queryResult method.
     */
    private class NewFileCallback implements  ResultCallback<DriveFolder.DriveFileResult> {
        private String identifier;

        /**
         * Constructor. The callback already knows the Drive data, but needs to know the identifier.
         * @param identifier String - being our identifier.
         */
        NewFileCallback(String identifier) {
            this.identifier = identifier;
        }

        /**
         * onResult of attempt to create the file.
         * @param result DriveFileResult - being the resulting file.
         */
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Code: " + result.getStatus().getStatusCode() +
                        "\nMsg: " + result.getStatus().getStatusMessage());
                Log.e(TAG, "Error saving " + identifier + " as new file.");
                return;
            }
            queryResult(true, identifier);
        }
    }

    /**
     * Callback for creating new data on Drive.
     */
    private class NewContentsCallback implements ResultCallback<DriveApi.DriveContentsResult> {
        private String identifier;

        /**
         * Constructor. This callback needs to know about the identifier.
         * @param identifier String - being our identifier.
         */
        NewContentsCallback(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            // Check the status of the DriveContentsResult, if failed, then log + return query result:
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error creating new contents for " + identifier);
                queryResult(true, identifier);
                return;
            }

            // Set-up our data + filename:
            Object data = getData(getID(identifier));
            String file = getFilename(getID(identifier));

            try {
                if (data != null & file.length() > 0) {
                    OutputStream oStream    = result.getDriveContents().getOutputStream();
                    ObjectOutputStream oos  = new ObjectOutputStream(oStream);

                    oos.writeObject(data);
                    oos.close();

                    // When creating a file we need to setup the filetype + title:
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(file)
                            .setMimeType("text/plain")
                            .build();

                    ///* DEBUG:
                    Log.d(TAG, "NEW FILE: " + file +
                            "\nDATA: " + data.toString() +
                            "\nMIME: " + changeSet.getMimeType());
                    //*/ END-DEBUG.

                    // Perform creation in app-folder and set callback:
                    Drive.DriveApi.getAppFolder(getmGoogleApiClient())
                            .createFile(getmGoogleApiClient(), changeSet, result.getDriveContents())
                            .setResultCallback(new NewFileCallback(identifier));

                } else {
                    // If no need to save, then skip and record this:
                    Log.i(TAG, "Skipping file due to not data or name: " + identifier + " = " + getID(identifier));
                    queryResult(true, identifier);
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException when trying to create new file!", e);
            }
        }
    }
}