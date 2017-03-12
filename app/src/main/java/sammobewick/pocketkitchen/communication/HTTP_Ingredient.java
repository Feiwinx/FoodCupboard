package sammobewick.pocketkitchen.communication;

import com.google.gson.Gson;

import java.util.List;

import sammobewick.pocketkitchen.data_objects.Ingredient;

/**
 * Created by Sam on 06/02/2017.
 */
public class HTTP_Ingredient {
    private List<Ingredient> results;
    private String baseUri;
    private int offset;
    private int number;
    private int totalResults;
    private int processingTimeMs;
    private long expires;
    private boolean isStale;

    private HTTP_Ingredient(String baseUri, long expires, boolean isStale, int number, int offset, int processingTimeMs, List<Ingredient> results, int totalResults) {
        this.baseUri = baseUri;
        this.expires = expires;
        this.isStale = isStale;
        this.number = number;
        this.offset = offset;
        this.processingTimeMs = processingTimeMs;
        this.results = results;
        this.totalResults = totalResults;
    }

    public HTTP_Ingredient(String json) {
        Gson gson = new Gson();
        HTTP_Ingredient handler = gson.fromJson(json, HTTP_Ingredient.class);
        this.baseUri = handler.getBaseUri();
        this.expires = handler.getExpires();
        this.isStale = handler.isStale();
        this.number = handler.getNumber();
        this.offset = handler.getOffset();
        this.processingTimeMs = handler.getProcessingTimeMs();
        this.results = handler.getResults();
        this.totalResults = handler.getTotalResults();
    }

    public String getBaseUri() {
        return baseUri;
    }

    public long getExpires() {
        return expires;
    }

    public boolean isStale() {
        return isStale;
    }

    public int getNumber() {
        return number;
    }

    public int getOffset() {
        return offset;
    }

    public int getProcessingTimeMs() {
        return processingTimeMs;
    }

    public List<Ingredient> getResults() {
        return results;
    }

    public int getTotalResults() {
        return totalResults;
    }
}
