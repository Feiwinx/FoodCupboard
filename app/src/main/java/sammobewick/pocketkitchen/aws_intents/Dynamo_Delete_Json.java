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
 * DynamoDB Deletion Intent
 * Created by Sam on 27/04/2017.
 */
public class Dynamo_Delete_Json extends Dynamo_Master_Json {
    private static final String TAG = "Dynamo_Deletion";

    /**
     * Required empty constructor for manifest.
     */
    public Dynamo_Delete_Json() {
        super(TAG);
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
                mapper.delete(db_wrapper);
            } catch (AmazonServiceException ase) {
                Log.e(TAG, "Dynamo_Delete Error ", ase);
            }

            // Broadcast the result back:
            Intent bcIntent = new Intent(Constants.BC_DELETE_NAME);
            if (error.length() == 0)
                bcIntent.putExtra(Constants.BC_DELETE_ID, true);
            else
                bcIntent.putExtra(Constants.BC_DELETE_ID, false);

            LocalBroadcastManager.getInstance(this).sendBroadcast(bcIntent);
        }
    }
}
