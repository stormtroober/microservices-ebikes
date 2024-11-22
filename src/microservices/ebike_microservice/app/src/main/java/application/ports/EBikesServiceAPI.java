    package application.ports;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

    /**
     * Interface for EBike service API.
     */
    public interface EBikesServiceAPI {

        /**
         * Creates a new EBike.
         *
         * @param id the identifier of the EBike to be created
         * @param x the x-coordinate of the EBike's initial location
         * @param y the y-coordinate of the EBike's initial location
         */
        void createEBike(String id, float x, float y);

        /**
         * Recharges the battery of an EBike.
         *
         * @param id the identifier of the EBike to be recharged
         * @return the JSON object representing the updated EBike
         */
        JsonObject rechargeEBike(String id);

        /**
         * Retrieves an EBike by its identifier.
         *
         * @param id the identifier of the EBike to be retrieved
         * @return the JSON object representing the retrieved EBike
         */
        JsonObject getEBike(String id);

        /**
         * Updates an existing EBike.
         *
         * @param ebike the JSON object representing the EBike to be updated
         * @return the JSON object representing the updated EBike
         */
        JsonObject updateEBike(JsonObject ebike);

        /**
         * Retrieves all EBikes.
         *
         * @return a JSON array containing all EBikes
         */
        JsonArray getEBikes();
    }