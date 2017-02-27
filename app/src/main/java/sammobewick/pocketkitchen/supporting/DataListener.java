package sammobewick.pocketkitchen.supporting;

/**
 * Simple interface to allow classes to listen to the PocketKitchenData class.
 * Only those elements that wish to receive updates should implement this.
 * This should therefore be the adapters for the ListViews, as they update.
 * Created by Sam on 27/02/2017.
 */
public interface DataListener {

    // Simple method just to pass information back:
    void dataUpdate();
}
