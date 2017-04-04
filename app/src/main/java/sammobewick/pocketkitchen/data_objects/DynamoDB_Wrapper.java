package sammobewick.pocketkitchen.data_objects;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.io.Serializable;

import sammobewick.pocketkitchen.supporting.Constants;

/**
 * Created by Sam on 30/03/2017.
 */
@DynamoDBTable(tableName = Constants.DYNAMO_TABLE_ID)
public class DynamoDB_Wrapper implements Serializable {

    private String jsonKey;
    private String jsonString;

    public DynamoDB_Wrapper(String jsonKey, String jsonString) {
        this.jsonKey = jsonKey;
        this.jsonString = jsonString;
    }

    @DynamoDBHashKey(attributeName = "jsonKey")
    public String getJsonKey() {
        return jsonKey;
    }

    @DynamoDBAttribute(attributeName = "json")
    public String getJsonString() {
        return jsonString;
    }
}