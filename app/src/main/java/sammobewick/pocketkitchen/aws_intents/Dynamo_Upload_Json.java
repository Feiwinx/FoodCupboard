package sammobewick.pocketkitchen.aws_intents;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import sammobewick.pocketkitchen.data_objects.DynamoDB_Wrapper;
import sammobewick.pocketkitchen.supporting.Constants;

/**
 * DynamoDB Uploader Intent.
 * Created by Sam on 30/03/2017.
 */
public class Dynamo_Upload_Json extends Dynamo_Master_Json {
    private static final String TAG = "Dynamo_Uploader";

    /**
     * Required empty constructor for manifest.
     */
    public Dynamo_Upload_Json() {
        super(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Dynamo_Upload_Json(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Set up AWS Dynamo Mapper:
        DynamoDBMapper mapper = new DynamoDBMapper(getClient());

        // Get the JSON+Key from Intent:
        String jsonData = "";
        String jsonKey  = "";

        Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            if (extras.containsKey(Constants.JSON_DYNAMO_KEY)) {
                jsonKey = extras.getString(Constants.JSON_DYNAMO_KEY);
            }
            if (extras.containsKey(Constants.JSON_DYNAMO)) {
                jsonData = extras.getString(Constants.JSON_DYNAMO);
            }
        }

        // Check that we have the necessary data:
        if ((jsonData != null ? jsonData.length() : 0) > 0 & (jsonKey != null ? jsonKey.length() : 0) >0) {
            String error = "";

            try {   // Wrap our data, upload to DynamoDB with error-checking:
                DynamoDB_Wrapper db_wrapper = new DynamoDB_Wrapper(jsonKey, jsonData);
                mapper.save(db_wrapper);
            } catch (AmazonServiceException ase) {
                error = "Amazon Error: " + ase.getLocalizedMessage();
                Log.e(TAG, error);
            }

            /*// Debug:
            System.out.println(TAG + " error: " + error.length());
            //*/

            // Broadcast the result back:
            Intent bcIntent = new Intent(Constants.BC_UPLOAD_NAME);
            if (error.length() == 0)
                bcIntent.putExtra(Constants.BC_UPLOAD_ID, true);
            else
                bcIntent.putExtra(Constants.BC_UPLOAD_ID, false);

            LocalBroadcastManager.getInstance(this).sendBroadcast(bcIntent);
        }
    }
}