//package org.dialogs.user;
//
//import sap.ass01.hexagonal.infrastructure.presentation.PresentationController;
//import sap.ass01.hexagonal.infrastructure.presentation.dialogs.AbstractDialog;
//import sap.ass01.hexagonal.infrastructure.presentation.models.UserViewModel;
//import sap.ass01.hexagonal.infrastructure.presentation.views.UserView;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//
//public class RechargeCreditDialog extends AbstractDialog {
//
//    private JTextField creditAmountField;
//    private final PresentationController presentationController;
//    private final UserViewModel user;
//
//    public RechargeCreditDialog(JFrame parent, PresentationController presentationController, UserViewModel user) {
//        super(parent, "Recharge Credit");
//        this.presentationController = presentationController;
//        this.user = user;
//        setupDialog();
//    }
//
//    private void setupDialog() {
//        creditAmountField = new JTextField();
//        addField("Credit Amount:", creditAmountField);
//        confirmButton.setText("Recharge");
//        cancelButton.setText("Cancel");
//        pack();
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        super.actionPerformed(e);
//        if (e.getSource() == confirmButton) {
//            String creditAmount = creditAmountField.getText();
//            presentationController.rechargeCredit(user.id(), Integer.parseInt(creditAmount),
//                            updatedUser -> {
//                        ((UserView) getParent()).updateCredit(updatedUser.credit());
//                        dispose();
//                    }, throwable -> {
//                        JOptionPane.showMessageDialog(this, "Error recharging credit: " + throwable.getMessage());
//                    });
//        }
//    }
//}