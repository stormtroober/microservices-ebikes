//package org.dialogs.admin;
//
//import sap.ass01.hexagonal.infrastructure.presentation.PresentationController;
//import sap.ass01.hexagonal.infrastructure.presentation.dialogs.AbstractDialog;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//
//public class RechargeBikeDialog extends AbstractDialog {
//
//    private JTextField idField;
//    private final PresentationController presentationController;
//
//    public RechargeBikeDialog(JFrame parent, PresentationController presentationController) {
//        super(parent, "Recharge Bike");
//        this.presentationController = presentationController;
//        setupDialog();
//    }
//
//    private void setupDialog() {
//        idField = new JTextField();
//        addField("E-Bike ID to recharge:", idField);
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        super.actionPerformed(e);
//        if (e.getSource() == confirmButton) {
//            String id = idField.getText();
//            presentationController.rechargeEBike(id, ebikeViewModel -> {
//                JOptionPane.showMessageDialog(this, "E-Bike recharged successfully: " + ebikeViewModel);
//                dispose();
//            }, throwable -> {
//                JOptionPane.showMessageDialog(this, "Error recharging E-Bike: " + throwable.getMessage());
//            });
//        }
//    }
//}