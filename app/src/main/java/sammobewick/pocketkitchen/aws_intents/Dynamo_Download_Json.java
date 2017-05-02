package sammobewick.pocketkitchen.aws_intents;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

import sammobewick.pocketkitchen.data_objects.DynamoDB_Wrapper;
import sammobewick.pocketkitchen.supporting.Constants;

/**
 * DynamoDB Downloader Intent.
 * Created by Sam on 30/03/2017.
 */
public class Dynamo_Download_Json extends Dynamo_Master_Json {
    private static final String TAG = "Dynamo_Downloader";

    /**
     * Required empty constructor for manifest.
     */
    public Dynamo_Download_Json() {
        super(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Dynamo_Download_Json(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Set up AWS Dynamo mapper:
        DynamoDBMapper mapper = new DynamoDBMapper(getClient());

        // Get the JSON+Key from Intent:
        String jsonKey  = "";

        Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            if (extras.containsKey(Constants.JSON_DYNAMO_KEY)) {
                jsonKey = extras.getString(Constants.JSON_DYNAMO_KEY);
            }
        }

        // Check key exists:
        if ((jsonKey != null ? jsonKey.length() : 0) > 0) {
            String error = "";
            Intent bcIntent = new Intent(Constants.BC_DOWNLOAD_NAME);

            try {   // Wrap our jsonKey, query DynamoDB with error-checking:
                DynamoDB_Wrapper db_wrapper = new DynamoDB_Wrapper(jsonKey, "");

                DynamoDBQueryExpression qExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(db_wrapper)
                        .withConsistentRead(false);

                PaginatedQueryList<DynamoDB_Wrapper> result = mapper.query(DynamoDB_Wrapper.class, qExpression);

                bcIntent.putExtra(Constants.BC_DOWNLOAD_DATA, result.get(0));

            } catch (AmazonServiceException ase) {
                error = "Amazon Error: " + ase.getLocalizedMessage();
                Log.e(TAG, error);
            }

            if (error.length() == 0)
                bcIntent.putExtra(Constants.BC_DOWNLOAD_ID, true);
            else
                bcIntent.putExtra(Constants.BC_DOWNLOAD_ID, false);

            LocalBroadcastManager.getInstance(this).sendBroadcast(bcIntent);
        }
    }
}
