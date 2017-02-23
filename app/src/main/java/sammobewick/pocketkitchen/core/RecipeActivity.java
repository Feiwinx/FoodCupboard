package sammobewick.pocketkitchen.core;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.DownloadImageAsync;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;

public class RecipeActivity extends AppCompatActivity {
    // As we need to load images here, we need to save the meta-data for URL start:
    private String          urlStart;
    private boolean         btnPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_full);
        setupActionBar();

        // Load both versions of the recipe from the intent + set the title:
        final Recipe_Short recipe_short = (Recipe_Short) getIntent().getExtras().get("recipe_short");
        final Recipe_Full recipe_full = (Recipe_Full) getIntent().getExtras().get("recipe_full");

        // Load meta-data:
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            urlStart = ai.metaData.getString("recipe_image_url");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            ActivityHelper helper = new ActivityHelper(getApplicationContext());
            helper.displayErrorDialog(e.getLocalizedMessage());
        }

        loadData(recipe_short, recipe_full);

        // Get Data Handler:
        final PocketKitchenData pkData = PocketKitchenData.getInstance();

        // Button Listener + View Setup:
        final Button mainButton = ((Button) findViewById(R.id.btn_do_recipe_f));

        // Insert a check for if the user wanted to cook this previously!
        if (pkData.checkForSetOfIngredients(recipe_short.getId())) {
            btnPressed = true;
            mainButton.setText(R.string.lbl_btn_rem_recipe);
            mainButton.setHint(R.string.hint_btn_rem_recipe);
        } else { btnPressed = false; }

        mainButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!btnPressed) {
                            if (pkData.addSetOfIngredients(recipe_short.getId(), recipe_full.getExtendedIngredients())) {
                                mainButton.setText(R.string.lbl_btn_rem_recipe);
                                mainButton.setHint(R.string.hint_btn_rem_recipe);

                                // Inform the user:
                                Snackbar.make(findViewById(R.id.recipe_full_content), R.string.feedback_added_recipe, Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            } else {
                                ActivityHelper helper = new ActivityHelper(getApplicationContext());
                                helper.displayErrorDialog("Failed to add this recipe to your cooking list!" +
                                        "\nYou might benefit from reloading the application.");
                            }
                        } else {
                            if (pkData.removeSetOfIngredients(recipe_short.getId())) {
                                mainButton.setText(R.string.lbl_btn_add_recipe);
                                mainButton.setHint(R.string.hint_btn_rem_recipe);

                                // Inform the user:
                                Snackbar.make(findViewById(R.id.recipe_full_content), R.string.feedback_removed_recipe, Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            } else {
                                ActivityHelper helper = new ActivityHelper(getApplicationContext());
                                helper.displayErrorDialog("Failed to add this recipe to your cooking list!" +
                                        "\nYou might benefit from reloading the application.");
                            }
                        }
                        btnPressed = !btnPressed;
                    }
                }
        );
    }

    /**
     * This method simply sets up the action bar:
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Here we load the recipe data into the view, showing/hiding images and setting text.
     * @param recipe_short - contains the image details.
     * @param recipe_full - contains everything else to display.
     */
    private void loadData(Recipe_Short recipe_short, Recipe_Full recipe_full){
                            // ***** RECIPE TITLE:                  ***** //
        ((TextView) this.findViewById(R.id.recipe_title_f)).setText(recipe_full.getTitle());

                            // ***** MAIN RECIPE IMAGE:             ***** //
        String url = urlStart + recipe_short.getImage();
        ImageView recipe_img = (ImageView) this.findViewById(R.id.recipe_image_f);
        new DownloadImageAsync(recipe_img).execute(url);

                            // ***** RIGHT-HAND INFORMATION PANE:   ***** //
        if (recipe_full.isVegan()) {
            this.findViewById(R.id.dietary_vegan_f).setVisibility(View.VISIBLE);
            this.findViewById(R.id.dietary_veg_f).setVisibility(View.GONE);
        } else if (recipe_full.isVegetarian()) {
            this.findViewById(R.id.dietary_vegan_f).setVisibility(View.GONE);
            this.findViewById(R.id.dietary_veg_f).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.dietary_vegan_f).setVisibility(View.GONE);
            this.findViewById(R.id.dietary_veg_f).setVisibility(View.GONE);
        }

        if (recipe_full.isGlutenFree()) {
            this.findViewById(R.id.dietary_gluten_f).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.dietary_gluten_f).setVisibility(View.GONE);
        }

        if (recipe_full.isDairyFree() & !recipe_full.isVegan()) {
            this.findViewById(R.id.dietary_dairy_f).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.dietary_dairy_f).setVisibility(View.GONE);
        }

        if (recipe_full.isVeryHealthy()) {
            this.findViewById(R.id.dietary_healthy_f).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.dietary_healthy_f).setVisibility(View.GONE);
        }

        if (recipe_full.isCheap()) {
            this.findViewById(R.id.dietary_cheap_f).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.dietary_cheap_f).setVisibility(View.GONE);
        }

        /* TODO: Optional information that is not yet displayable!
        if (recipe_full.isKetogenic()) {}
        if (recipe_full.isLowFodmap()) {}
        if (recipe_full.isWhole30()) {}
        */

                        // ***** BENEATH IMAGE AREA FOR INFORMATION:    ***** //
        TextView recipe_time = (TextView) findViewById(R.id.recipe_time_f);
        recipe_time.setText(recipe_full.getReadyInMinutes() + "m");

        String plates = " plates";
        int servings = recipe_full.getServings();
        if (servings == 1) { plates = " plate"; }
        TextView recipe_serves = (TextView) findViewById(R.id.recipe_serves_f);
        recipe_serves.setText(recipe_full.getServings() + plates);

        if (recipe_full.isVeryPopular()) {
            this.findViewById(R.id.recipe_popular_img_f).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.recipe_popular_img_f).setVisibility(View.GONE);
        }

        TextView ingredients = (TextView) findViewById(R.id.recipe_ing_f);
        ingredients.setMovementMethod(new ScrollingMovementMethod());
        ingredients.setText(readIngredients(recipe_full.getExtendedIngredients()));

                        // ***** RECIPE INSTRUCTIONS ***** //
        TextView instructions = (TextView) findViewById(R.id.recipe_ins_f);
        instructions.setMovementMethod(new ScrollingMovementMethod());

        String instr_text = recipe_full.getInstructions();
        if (instr_text != null) {
            instructions.setText(instr_text.replaceAll("<[^>]*>","\n").trim());
        } else {
            instructions.setText("Oops!\nNo instructions have been provided by Spoonacular!");
        }

        /* DEBUG:
        System.out.println("RECIPEINFO: " + recipe_full.toString());
        for (Ingredient i : recipe_full.getExtendedIngredients()) {
            System.out.println("RECIPEINFO: " + i.toString());
        }
        // END-DEBUG */
    }

    /**
     * Simple method to save space above. Loops through the ingredients and generates a
     * String to represent all ingredients.
     * @param ingredientList
     * @return
     */
    private String readIngredients(List<Ingredient> ingredientList) {
        String result = "";
        for (Ingredient i : ingredientList) {
            if (i.getUnitShort().length() > 1) {
                result += i.getAmount() + " " + i.getUnitShort() + " " + i.getName() + "\n";
            } else {
                result += i.getOriginalString() + "\n";
            }
        }
        return result;
    }
}
