//package org.dialogs.user;
//
//import sap.ass01.hexagonal.infrastructure.presentation.PresentationController;
//import sap.ass01.hexagonal.infrastructure.presentation.dialogs.AbstractDialog;
//import sap.ass01.hexagonal.infrastructure.presentation.models.EBikeViewModel;
//import sap.ass01.hexagonal.infrastructure.presentation.models.UserViewModel;
//import sap.ass01.hexagonal.infrastructure.presentation.views.UserView;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//import java.util.Optional;
//import java.util.UUID;
//
//public class StartRideDialog extends AbstractDialog {
//
//    private JTextField idEBikeField;
//    private final PresentationController presentationController;
//    private final UserViewModel user;
//
//    public StartRideDialog(JFrame parent, PresentationController presentationController, UserViewModel user) {
//        super(parent, "Start Riding an E-Bike");
//        this.presentationController = presentationController;
//        this.user = user;
//        setupDialog();
//    }
//
//    private void setupDialog() {
//        idEBikeField = new JTextField();
//        addField("E-Bike ID:", idEBikeField);
//        confirmButton.setText("Start Ride");
//        cancelButton.setText("Cancel");
//        pack();
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        super.actionPerformed(e);
//        if (e.getSource() == confirmButton) {
//            String bikeId = idEBikeField.getText();
//            String rideId = UUID.randomUUID().toString();
//
//            Optional<EBikeViewModel> bike = ((UserView) getParent()).findBike(bikeId);
//            if(bike.isPresent()) {
//                presentationController.startRide(rideId, user, bike.get(),
//                        startedRide -> {
//                            ((UserView) getParent()).initRide(startedRide);
//                            observeRideUpdates(rideId, user, bike.get());
//                            dispose();
//                        },
//                        throwable -> {
//                            JOptionPane.showMessageDialog(this, "Error starting ride: " + throwable.getMessage());
//                        }
//                );
//            } else {
//                log("Bike not found");
//                JOptionPane.showMessageDialog(this, "Bike not found");
//            }
//        }
//    }
//
//    private void observeRideUpdates(String rideId, UserViewModel user, EBikeViewModel bike) {
//        presentationController.observeRide(rideId, user, bike,
//                rideViewModel -> {
//                    ((UserView) getParent()).updateRide(rideViewModel);
//                },
//                throwable -> {
//                    System.err.println("Error observing ride: " + throwable.getMessage());
//                },
//                () -> {
//                    log("Ride completed");
//                    ((UserView) getParent()).endRide();
//                }
//        );
//    }
//
//    private void log(String msg){
//        System.out.println("[StartRideDialog] "+msg);
//    }
//}