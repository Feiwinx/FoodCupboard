package sammobewick.pocketkitchen.communication;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.supporting.Constants;

/**
 * Base Drive Activity.
 * We use this to build the load/save/delete drive activities, as the same base operations are needed.
 */
public abstract class BaseDriveActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG                 = "BaseDriveAct";
    private static final int REQ_CODE_RESOLUTION    = 1;
    private static final int MAX_REQUESTS           = Constants.FILE_COUNT;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
    }

    /**
     * Main function of this class. It creates as connection if required, otherwise connects when
     * appropriate.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Used as we listen for ActivityResults from the GoogleClient. Standard practise.
     * @param requestCode int - the request code.
     * @param resultCode int - the result code.
     * @param data Intent - containing data (sometimes a resolution Intent).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * On Activity Pause - disconnect the client.
     */
    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    /**
     * onConnected Listener.
     * Here call our queries for the needed files (we always use the same ones, regardless of
     * account - but they are not consistently numbered! Hence the query.)
     * @param bundle Bundle - can be null, often will be.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connection established");

        // Run query for Drive files:
        for (int i=0; i<MAX_REQUESTS; i++) {
            Query q = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, getFilename(i)))
                    .build();

            Drive.DriveApi.getAppFolder(getmGoogleApiClient())
                    .queryChildren(getmGoogleApiClient(), q)
                    .setResultCallback(new myDriveCallback(getFilename(i)));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Required by Listener usage. Sometimes a connection failed can have a resolution attached to
     * it, which is ran if so. Otherwise, the issue is logged and feedback given as required.
     * @param result ConnectionResult - information on the connection failure.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQ_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Error when connecting to Google Drive: ", e);
        }
    }

    /**
     * Handler callback class.
     * Breaks down the queries, and references some Abstract methods which will require more
     * specific implementations.
     */
    private class myDriveCallback implements ResultCallback<DriveApi.MetadataBufferResult> {
        // Identifier for the query:
        private String identifier;

        myDriveCallback(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult result) {

            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Query for " + identifier + " failed.");
                queryResult(false, identifier);
            } else {
                boolean meta_found = false;
                Log.d(TAG, "Query for " + identifier + " successful.");

                // Loop through MetadataBuffer:
                MetadataBuffer mdb = result.getMetadataBuffer();
                for (Metadata meta : mdb) {
                    meta_found = true;
                    ///* DEBUG:
                    DriveId id = meta.getDriveId();
                    String title = meta.getTitle();
                    Log.d(TAG, "FILE TITLE: " + title + "\nID: " + id.toString());
                    // END-DEBUG //*/

                    handleMeta(meta);
                }
                if (!meta_found) {
                    queryResult(false, identifier);
                }
            }
        }
    }

    /**
     * Method to be implemented in child classes. It is expected that this will be used by the child
     * class elsewhere.
     * @param success boolean - being whether the operation was successful or not.
     * @param identifier String - the identifier (either for usage or logging).
     */
    public abstract void queryResult(boolean success, String identifier);

    /**
     * Method to be implemented in child classes. It is expected that this will vary greatly between
     * various children classes. This is called from the resulting query callback.
     * @param meta Metadata - being the result Metadata from the base queries.
     */
    abstract public void handleMeta(Metadata meta);

    /**
     * Getter for the GoogleApiClient.
     * @return GoogleApiClient - being the client itself.
     */
    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Getter for MAX_REQUESTS. This must be consistent between children.
     * @return int - being the MAX_REQUESTS.
     */
    public static int getMaxRequests() {
        return MAX_REQUESTS;
    }

    /**
     * Helper method for establishing a filename based on an ID value. This keeps child programs
     * consistent, and referring to the same files.
     * @param id int - being an ID representing the file.
     * @return String - being the filename.
     */
    public String getFilename(int id) {
        switch (id) {
            case 0:     // inCupboards.pk
                return Constants.MY_RECIPES;
            case 1:     // recipesToCook.pk
                return Constants.IN_CUPBOARDS;
            case 2:     // myCustomRecipes.pk
                return Constants.RECIPES;
            case 3:     // ingredientsRequired.pk
                return Constants.INGREDIENTS;
            default:    // DEFAULT
                return "";
        }
    }

    /**
     * Helper method to establish the ID value based on the filename. This allows more flexibility in
     * logging error messages.
     * @param identifier String - being the known filename.
     * @return int - being the ID of this filename.
     */
    public int getID(String identifier) {
        switch (identifier) {
            case Constants.MY_RECIPES:
                return 0;
            case Constants.IN_CUPBOARDS:
                return 1;
            case Constants.RECIPES:
                return 2;
            case Constants.INGREDIENTS:
                return 3;
            default:
                return -1;
        }
    }

    /**
     * Helper method to refer to each file's data.
     * @param id int - the id we want to get the associated data for.
     * @return Object - being the data.
     */
    public Object getData(int id) {
        PocketKitchenData pkData = PocketKitchenData.getInstance();
        switch (id) {
            case 0:     // inCupboards.pk
                return pkData.getMyCustomRecipes();
            case 1:     // recipesToCook.pk
                return pkData.getInCupboards();
            case 2:     // myCustomRecipes.pk
                return pkData.getRecipesToCook();
            case 3:     // ingredientsRequired.pk
                return pkData.getRecipe_ingredients();
            default:    // null
                return null;
        }
    }
}
