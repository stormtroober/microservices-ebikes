//package org.dialogs.admin;
//
//import sap.ass01.hexagonal.infrastructure.presentation.PresentationController;
//import sap.ass01.hexagonal.infrastructure.presentation.dialogs.AbstractDialog;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//
//public class AddEBikeDialog extends AbstractDialog {
//
//    private JTextField idField;
//    private JTextField xCoordField;
//    private JTextField yCoordField;
//    private final PresentationController presentationController;
//
//    public AddEBikeDialog(JFrame parent, PresentationController presentationController) {
//        super(parent, "Adding E-Bike");
//        this.presentationController = presentationController;
//        setupDialog();
//    }
//
//    private void setupDialog() {
//        idField = new JTextField();
//        xCoordField = new JTextField();
//        yCoordField = new JTextField();
//
//        addField("E-Bike ID:", idField);
//        addField("E-Bike location - X coord:", xCoordField);
//        addField("E-Bike location - Y coord:", yCoordField);
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        super.actionPerformed(e);
//        if (e.getSource() == confirmButton) {
//            String id = idField.getText();
//            double xCoord = Double.parseDouble(xCoordField.getText());
//            double yCoord = Double.parseDouble(yCoordField.getText());
//            presentationController.createEBike(id, xCoord, yCoord, ebikeViewModel -> {
//                JOptionPane.showMessageDialog(this, "E-Bike added successfully: " + ebikeViewModel);
//                dispose();
//            }, throwable -> {
//                JOptionPane.showMessageDialog(this, "Error adding E-Bike: " + throwable.getMessage());
//            });
//            dispose();
//        }
//    }
//}
package org.dialogs.admin;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.dialogs.AbstractDialog;
import org.views.AdminView;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AddEBikeDialog extends AbstractDialog {

    private JTextField idField;
    private JTextField xCoordField;
    private JTextField yCoordField;
    private final Vertx vertx;

    public AddEBikeDialog(JFrame parent, Vertx vertx) {
        super(parent, "Adding E-Bike");
        this.vertx = vertx;
        setupDialog();
    }

    private void setupDialog() {
        idField = new JTextField();
        xCoordField = new JTextField();
        yCoordField = new JTextField();

        addField("E-Bike ID:", idField);
        addField("E-Bike location - X coord:", xCoordField);
        addField("E-Bike location - Y coord:", yCoordField);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (e.getSource() == confirmButton) {
            String id = idField.getText();
            double xCoord = Double.parseDouble(xCoordField.getText());
            double yCoord = Double.parseDouble(yCoordField.getText());

            JsonObject bikeDetails = new JsonObject()
                    .put("id", id)
                    .put("x", xCoord)
                    .put("y", yCoord);

            vertx.eventBus().request("admin.bike.create", bikeDetails, reply -> {
                if (reply.succeeded()) {
                    JOptionPane.showMessageDialog(this, "E-Bike added successfully");
                    System.out.println("E-Bike added successfully: " + reply.result().body());

                } else {
                    JOptionPane.showMessageDialog(this, "Error adding E-Bike: " + reply.cause().getMessage());
                    System.out.println("Error adding E-Bike: " + reply.cause().getMessage());
                }
                dispose();
            });
        }
    }
}