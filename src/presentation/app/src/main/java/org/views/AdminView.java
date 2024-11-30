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

public class AdminView extends AbstractView {

    private final List<UserViewModel> userList = new ArrayList<>();
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

            if (type.equals("USER") && userList.stream().noneMatch(user -> user.id().equals(username))) {
                UserViewModel user = new UserViewModel(username, credit , false);
                userList.add(user);
            }
            System.out.println("Received user update: " + update);
            refreshView();
        });
    }

    private void updateAllUsers(List<UserViewModel> userModels) {
        userList.clear();
        userList.addAll(userModels);
        refreshView();
    }

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
            g2.drawString("User ID: " + user.id() + " - Credit: " + user.credit(), 10, dy);
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