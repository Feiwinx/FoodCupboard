package sammobewick.pocketkitchen.core;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.RvA_Custom_Ingredients;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;

public class AddRecipeActivity extends AppCompatActivity implements View.OnClickListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private AmazonDynamoDBClient            ddbClient;
    private RecyclerView                    mRecyclerView;
    private RvA_Custom_Ingredients mRvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Set-up Amazon DynamoDB:
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),    /* get the context for the application */
                "eu-west-1:4f4d2fac-eab3-4a5f-8a96-4f34dc2e6807",    /* Identity Pool ID */
                Regions.EU_WEST_1         /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
        );

        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        ddbClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

        // Set up RecyclerView:
        mRecyclerView   = (RecyclerView) findViewById(R.id.rv_add_custom_recipe_ingredients);
        mRvAdapter      = new RvA_Custom_Ingredients(this);
        mRecyclerView.setAdapter(mRvAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up OnClickListeners:
        findViewById(R.id.btn_add_custom_recipe).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_ingredients).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_instructions).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_dietary).setOnClickListener(this);
        findViewById(R.id.btn_custom_recipe_ing).setOnClickListener(this);
    }

    private void saveRecipe() {
        // TODO: Save the recipe!
        ActivityHelper.displaySnackBarNoAction(AddRecipeActivity.this, R.layout.activity_add_recipe, R.string.snackbar_wip_feature);
    }

    @Override
    public void onClick(View v) {
        int id  = v.getId();
        View gist;
        switch (id) {
            // ADD BLANK ITEM
            case R.id.lbl_add_custom_recipe_ingredients:
                mRvAdapter.addBlankItem();
                break;
            // HIDE GIST - RECIPE INGREDIENTS
            case R.id.btn_custom_recipe_ing:
                gist = findViewById(R.id.rv_add_custom_recipe_ingredients);
                if (gist.isShown()) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_arrow_up);
                    gist.setVisibility(View.GONE);
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_arrow_down);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
            // HIDE GIST - RECIPE INSTRUCTIONS
            case R.id.lbl_add_custom_recipe_instructions:
                gist = findViewById(R.id.edit_add_custom_recipe_instructions);
                if (gist.isShown()) {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                    gist.setVisibility(View.GONE);
                } else {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
            // HIDE GIST - DIETARY INFORMATION
            case R.id.lbl_add_custom_recipe_dietary:
                gist = findViewById(R.id.grid_custom_recipe_dietary);
                if (gist.isShown()) {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                    gist.setVisibility(View.GONE);
                } else {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
            // ATTEMPT TO SAVE RECIPE:
            case R.id.btn_add_custom_recipe:
                saveRecipe();
                break;
        }
    }

    // TODO: ASYNC CLASS FOR DYNAMO DATA SEND
    private class TestAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            mapper.save(new Recipe_Short(params[0]));
            return null;
        }
    }
}
