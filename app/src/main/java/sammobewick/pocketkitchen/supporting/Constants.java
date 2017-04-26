package sammobewick.pocketkitchen.supporting;

/**
 * Created by Sam on 30/03/2017.
 */

public final class Constants {
    public static final String INTENT_CUSTOM_ID = "DISPLAY_CUSTOM";
    // General AWS / IntentService keys:
    public static final int    AWS_SUCCESS_CODE = 1;
    public static final String AWS_ID_POOL      = "eu-west-1:068120ce-873c-4faf-82c1-b7e8a41cd01c";
    public static final String AWS_RR_KEY       = "AWS_RR_OBJ";
    public static final String STATUS_KEY       = "RESULT_KEY";
    public static final String RES_BUNDLE_KEY   = "RES_BUNDLE_KEY";

    // Identifiers for Dynamo:
    public static final String DYNAMO_TABLE_ID  = "PK_USER_RECIPES";
    public static final String JSON_DYNAMO      = "JSON_BUNDLE_KEY";
    public static final String JSON_DYNAMO_KEY  = "JSON_KEY_KEY";

    // Identifiers for S3:
    public static final String INTENT_S3_FILE   = "INTENT_S3_FILE";
    public static final String S3_OBJECT_KEY    = "S3_OBJECT_KEY";
    public static final String S3_BUCKET_NAME   = "pocketkitchen";

    // Identifier for Broadcasting:
    public static final String BC_UPLOAD_ID     = "BC_UPLOAD_ID";
    public static final String BC_UPLOAD_NAME   = "AWS_UPLOAD_BROADCAST";

    public static final String BC_DOWNLOAD_ID   = "BC_DOWNLOAD_ID";
    public static final String BC_DOWNLOAD_NAME = "AWS_DOWNLOAD_BROADCAST";
    public static final String BC_DOWNLOAD_DATA = "BC_DOWNLOAD_DATA";

    public static final int    FILE_COUNT       = 4;
    public static final String MY_RECIPES       = "myRecipes.pk";
    public static final String IN_CUPBOARDS     = "inCupboards.pk";
    public static final String RECIPES          = "recipesToCook.pk";
    public static final String INGREDIENTS      = "ingredientsRequired.pk";
    public static final String TEMP_IMG_NAME    = "tempImage.png";

}
