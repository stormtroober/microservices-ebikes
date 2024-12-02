package org.views;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.dialogs.admin.AddEBikeDialog;
import org.dialogs.admin.RechargeBikeDialog;
import org.models.EBikeViewModel;
import org.models.UserViewModel;
import org.verticles.AdminVerticle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AdminView extends AbstractView {

    private final List<UserViewModel> userList = new CopyOnWriteArrayList<>();
    private final AdminVerticle verticle;
    private final Vertx vertx;

    public AdminView(UserViewModel user, Vertx vertx) {
        super("Admin View", user);
        this.vertx = vertx;
        this.verticle = new AdminVerticle(vertx);
        this.verticle.init();
        setupView();
        observeAllBikes();
        observeAllUsers();
        refreshView();
    }

    private void setupView() {
        topPanel.setLayout(new FlowLayout());

        JButton addBikeButton = new JButton("Add Bike");
        addBikeButton.addActionListener(e -> {
            AddEBikeDialog addEBikeDialog = new AddEBikeDialog(AdminView.this, vertx);
            addEBikeDialog.setVisible(true);
        });
        topPanel.add(addBikeButton);

        JButton rechargeBikeButton = new JButton("Recharge Bike");
        rechargeBikeButton.addActionListener(e -> {
            RechargeBikeDialog rechargeBikeDialog = new RechargeBikeDialog(AdminView.this, vertx);
            rechargeBikeDialog.setVisible(true);
        });
        topPanel.add(rechargeBikeButton);

    }

    private void observeAllBikes() {
        vertx.eventBus().consumer("admin.bike.update", message -> {
            JsonObject update = (JsonObject) message.body();

            String id = update.getString("id");
            Integer batteryLevel = update.getInteger("batteryLevel");
            String stateStr = update.getString("state");
            JsonObject location = update.getJsonObject("location");
            Double x = location.getDouble("x");
            Double y = location.getDouble("y");
            EBikeViewModel.EBikeState state = EBikeViewModel.EBikeState.valueOf(stateStr);

            EBikeViewModel bike = new EBikeViewModel(id, x, y, batteryLevel, state);
            eBikes.add(bike);
            refreshView();
        });
    }


    private void observeAllUsers() {
        vertx.eventBus().consumer("admin.user.update", message -> {
            JsonObject update = (JsonObject) message.body();
            String username = update.getString("username");
            String type = update.getString("type");
            Integer credit = update.getInteger("credit");

            if (type.equals("USER") && userList.stream().noneMatch(user -> user.username().equals(username))) {
                System.out.println("Adding user: " + username);
                UserViewModel user = new UserViewModel(username, credit , false);
                userList.add(user);
            } else if (type.equals("USER")) {
                System.out.println("Updating user: " + username);
                userList.stream()
                        .filter(u -> u.username().equals(username))
                        .findFirst()
                        .ifPresent(u -> {
                            userList.remove(u);
                            userList.add(new UserViewModel(username, credit, false));
                        });
            }
            System.out.println("Received user update: " + update);
            refreshView();
        });
    }

//    private void updateAllUsers(UserViewModel user) {
//        userList.stream().filter(u -> u.username().equals(user.username()))
//                .forEach(u -> u.updateCredit(user.credit()));
//        refreshView();
//    }

//    private void updateAllBikes(Collection<EBikeDTO> allBikes) {
//        eBikes = allBikes.stream()
//                .map(bike -> {
//                    EBikeDTOExt bikeExt = pluginService.applyPluginEffect("ColorStateEffect", bike);
//                    return Mapper.toDomain(bikeExt);
//                })
//                .toList();
//        refreshView();
//    }

    @Override
    protected void paintAdminView(Graphics2D g2) {
        super.paintAdminView(g2);
        printAllUsers(g2);
    }

    private void printAllUsers(Graphics2D g2) {
        int dy = 20;
        g2.drawString("ALL USERS: ", 10, dy);
        dy += 15;
        for (UserViewModel user : userList) {
            g2.drawString("User ID: " + user.username() + " - Credit: " + user.credit(), 10, dy);
            dy += 15;
        }
    }

    public void refreshView() {
        updateVisualizerPanel();
    }

    private void log(String msg) {
        System.out.println("[AdminView] " + msg);
    }
}