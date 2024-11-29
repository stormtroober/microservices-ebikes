//package org.views;
//
//import sap.ass01.hexagonal.infrastructure.presentation.PresentationController;
//import sap.ass01.hexagonal.infrastructure.presentation.dialogs.user.RechargeCreditDialog;
//import sap.ass01.hexagonal.infrastructure.presentation.dialogs.user.StartRideDialog;
//import sap.ass01.hexagonal.infrastructure.presentation.models.EBikeViewModel;
//import sap.ass01.hexagonal.infrastructure.presentation.models.RideViewModel;
//import sap.ass01.hexagonal.infrastructure.presentation.models.UserViewModel;
//
//import javax.swing.*;
//import java.util.List;
//import java.util.Optional;
//
//public class UserView extends AbstractView {
//
//    private Optional<RideViewModel> ongoingRide = Optional.empty();
//    private JButton rideButton;
//    private final PresentationController presentationController;
//
//    public UserView(UserViewModel user, PresentationController presentationController) {
//        super("User View", user);
//        this.presentationController = presentationController;
//        setupView();
//        observeAvailableBikes();
//        updateVisualizerPanel();
//    }
//
//    private void setupView() {
//
//        rideButton = new JButton("Start Ride");
//        rideButton.addActionListener(e -> toggleRide());
//        buttonPanel.add(rideButton);
//
//        addTopPanelButton("Recharge Credit", e -> {
//            RechargeCreditDialog rechargeCreditDialog = new RechargeCreditDialog(UserView.this, presentationController, actualUser);
//            rechargeCreditDialog.setVisible(true);
//        });
//        updateRideButtonState();
//    }
//
//    private void toggleRide() {
//        if (ongoingRide.isPresent()) {
//            stopRide();
//        } else {
//            startRide();
//        }
//    }
//
//    private void startRide() {
//        StartRideDialog startRideDialog = new StartRideDialog(UserView.this, presentationController, actualUser);
//        startRideDialog.setVisible(true);
//    }
//
//    private void stopRide() {
//        ongoingRide.ifPresent(ride -> {
//            presentationController.endRide(actualUser.id(), ride.id(), ride.bike().id(),
//                    this::endRide,
//                    throwable -> {
//                        log("Error ending ride: " + throwable.getMessage());
//                        throwable.printStackTrace();
//                    });
//        });
//    }
//
//    private void updateRideButtonState() {
//        rideButton.setText(ongoingRide.isPresent() ? "Stop Ride" : "Start Ride");
//    }
//
//    private void observeAvailableBikes() {
//        presentationController.observeAvailableBikes(
//                this::updateAvailableBikes,
//                throwable -> {
//                    log("Error observing available bikes: " + throwable.getMessage());
//                    throwable.printStackTrace();
//                }
//        );
//    }
//
//    private void updateAvailableBikes(List<EBikeViewModel> availableBikes) {
//        try {
//
//            Optional<EBikeViewModel> ongoingBike = ongoingRide.map(RideViewModel::bike);
//            eBikes = availableBikes;
//            ongoingBike.ifPresent(eBikes::add);
//            updateVisualizerPanel();
//        } catch (Exception e) {
//            log("Exception while updating available bikes: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public void initRide(RideViewModel ride){
//        this.ongoingRide = Optional.of(ride);
//        updateRideButtonState();
//    }
//    public void updateRide(RideViewModel ride) {
//        if(ongoingRide.isPresent()){
//            log("On going is present");
//            ongoingRide = Optional.of(ride);
//            eBikes = eBikes.stream()
//                    .map(bike -> bike.id().equals(ongoingRide.get().bike().id()) ? ongoingRide.get().bike() : bike)
//                    .toList();
//
//        }
//        updateVisualizerPanel();
//        updateRideButtonState();
//        updateCredit(ride.user().credit());
//    }
//
//    public Optional<EBikeViewModel> findBike(String bikeId) {
//        return eBikes.stream()
//                .filter(bike -> bike.id().equals(bikeId))
//                .findFirst();
//    }
//
//    private void log(String msg){
//        System.out.println("[UserView] " + msg);
//    }
//
//    public void endRide() {
//        ongoingRide = Optional.empty();
//        updateVisualizerPanel();
//        updateRideButtonState();
//    }
//
//    public void updateCredit(int credit){
//        actualUser = actualUser.updateCredit(credit);
//        updateVisualizerPanel();
//    }
//}