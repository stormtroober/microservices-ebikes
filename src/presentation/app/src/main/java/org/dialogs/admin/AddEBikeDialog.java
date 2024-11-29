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