package sammobewick.pocketkitchen.data_objects;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.io.Serializable;

import sammobewick.pocketkitchen.supporting.Constants;

/**
 * Wrapper class to handle data through the Amazon SDK for Dynamo.
 * Dynamo requires objects to have tags such as DBHashKey, and tagging all of the fields in
 * Recipe_Full did not work as expected. Therefore I made this wrapper object so I can handle data.
 * Created by Sam on 30/03/2017.
 */
@DynamoDBTable(tableName = Constants.DYNAMO_TABLE_ID)
public class DynamoDB_Wrapper implements Serializable {

    private String jsonKey;
    private String jsonString;

    /**
     * Prevents instantiation without data.
     */
    public DynamoDB_Wrapper() {/* Empty */}

    /**
     * Constructor.
     * @param jsonKey String - the key for this object.
     * @param jsonString String - the JSON data for this object.
     */
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

    public void setJsonKey(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}