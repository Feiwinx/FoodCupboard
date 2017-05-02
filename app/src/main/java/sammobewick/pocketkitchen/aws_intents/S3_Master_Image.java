package sammobewick.pocketkitchen.aws_intents;

import android.app.IntentService;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import sammobewick.pocketkitchen.supporting.Constants;

/**
 * S3 Master Intent Service.
 * Contains the base getClient method for all children.
 * Created by Sam on 27/04/2017.
 */
public abstract class S3_Master_Image extends IntentService {
    /**
     * Helper class to initialise connection to AWS connection pool.
     * @return AmazonS3 - being out S3 client to use.
     */
    protected AmazonS3 getClient() {
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                Constants.AWS_ID_POOL,
                Regions.EU_WEST_1
        );
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1));
        return s3;
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public S3_Master_Image(String name) {
        super(name);
    }
}
