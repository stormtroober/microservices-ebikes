package org.views;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.dialogs.user.RechargeCreditDialog;
import org.models.EBikeViewModel;
import org.models.UserViewModel;
import org.verticles.UserVerticle;

import javax.swing.*;
import java.awt.*;

public class UserView extends AbstractView {

    private final UserVerticle verticle;
    private final Vertx vertx;
    private JButton rideButton;

    public UserView(UserViewModel user, Vertx vertx) {
        super("User View", user);
        this.vertx = vertx;
        this.verticle = new UserVerticle(vertx, user.username());
        setupView();
        this.verticle.init();
        observeAvailableBikes();
        observeUser();
        refreshView();

    }


    private void setupView() {
        topPanel.setLayout(new FlowLayout());

        rideButton = new JButton("Start Ride");
        rideButton.addActionListener(e -> toggleRide());
        buttonPanel.add(rideButton);

        addTopPanelButton("Recharge Credit", e -> {
            RechargeCreditDialog rechargeCreditDialog = new RechargeCreditDialog(UserView.this, vertx, actualUser);
            rechargeCreditDialog.setVisible(true);
        });
        updateRideButtonState();
    }

    private void toggleRide() {

    }

    private void updateRideButtonState() {
        rideButton.setText("Start Ride");
    }

    private void startRide() {
        // Start ride logic
    }

    private void stopRide() {
        // Stop ride logic
    }

    private void observeAvailableBikes() {

        vertx.eventBus().consumer("user.bike.update."+actualUser.username(), message -> {
            JsonArray update = (JsonArray) message.body();
            log("Received bike update: " + update);
            eBikes.clear();
            for (int i = 0; i < update.size(); i++) {
                Object element = update.getValue(i);
                if (element instanceof String) {
                    JsonObject bikeObj = new JsonObject((String) element);
                    String id = bikeObj.getString("bikeName");
                    Integer batteryLevel = bikeObj.getInteger("batteryLevel");
                    String stateStr = bikeObj.getString("state");
                    JsonObject location = bikeObj.getJsonObject("position");
                    Double x = location.getDouble("x");
                    Double y = location.getDouble("y");
                    EBikeViewModel.EBikeState state = EBikeViewModel.EBikeState.valueOf(stateStr);

                    EBikeViewModel bikeModel = new EBikeViewModel(id, x, y, batteryLevel, state);
                    eBikes.add(bikeModel);
                } else {
                    log("Invalid bike data: " + element);
                }
            }
            refreshView();
        });
    }

    public void observeUser(){
        vertx.eventBus().consumer("user.update." + actualUser.username(), message -> {
            JsonObject update = (JsonObject) message.body();

            String username = update.getString("username");
            int credit = update.getInteger("credit");
            if (username.equals(actualUser.username())) {
                actualUser = actualUser.updateCredit(credit);
            }
            refreshView();

        });
    }

//    @Override
//    public void paintUserView(Graphics2D g2) {
//        super.paintUserView(g2);
//    }

    private void refreshView() {
        updateVisualizerPanel();
    }

    private void log(String msg) {
        System.out.println("[UserView] " + msg);
    }

}