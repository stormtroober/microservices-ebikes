package domain.model;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.CompletableFuture;

public class RideSimulation {
    private final Ride ride;
    private final EventBus eventBus;
    private final Vertx vertx;
    private volatile boolean stopped = false;
    private long lastTimeChangedDir = System.currentTimeMillis();
    private static final String RIDE_UPDATE_ADDRESS = "ride.updates";

    public RideSimulation(Ride ride, Vertx vertx) {
        this.ride = ride;
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
    }

    public Ride getRide() {
        return ride;
    }

    public CompletableFuture<Void> startSimulation() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ride.start();

        if (ride.isOngoing()) {
            vertx.setPeriodic(100, timerId -> {
                if (stopped) {
                    vertx.cancelTimer(timerId);
                    future.complete(null);
                    completeSimulation();
                    return;
                }

                updateRide();
            });
        } else {
            future.complete(null);
        }

        return future;
    }

    private void updateRide() {
        EBike bike = ride.getEbike();
        User user = ride.getUser();

        synchronized (bike) {
            if (bike.getBatteryLevel() == 0) {
                System.out.println("Bike has no battery");
                ride.end();
                stopSimulation();
                completeSimulation();
            }

            if (user.getCredit() == 0) {
                ride.end();
                stopSimulation();
                bike.setState(EBikeState.AVAILABLE);
                completeSimulation();
            }

            // Simulate movement and battery usage
            V2d direction = bike.getDirection();
            double speed = 0.5;  // Set speed to a constant value for simplicity
            V2d movement = direction.mul(speed);
            bike.setLocation(bike.getLocation().sum(movement));

            // Boundary checks to reverse direction
            if (bike.getLocation().x() > 200 || bike.getLocation().x() < -200) {
                bike.setDirection(new V2d(-direction.x(), direction.y()));
            }
            if (bike.getLocation().y() > 200 || bike.getLocation().y() < -200) {
                bike.setDirection(new V2d(direction.x(), -direction.y()));
            }

            // Change direction every 500ms
            long elapsedTimeSinceLastChangeDir = System.currentTimeMillis() - lastTimeChangedDir;
            if (elapsedTimeSinceLastChangeDir > 500) {
                double angle = Math.random() * 60 - 30;
                bike.setDirection(direction.rotate(angle));
                lastTimeChangedDir = System.currentTimeMillis();
            }

            // Decrease battery and user credit
            bike.decreaseBattery(1);
            user.decreaseCredit(1);

            // Publish updated ride information
            eventBus.publish(RIDE_UPDATE_ADDRESS, ride.toString());
        }
    }

    private void completeSimulation() {
        // Emit the completion of the simulation
        System.out.println("Ride completed---------------");
        eventBus.publish(RIDE_UPDATE_ADDRESS, "Simulation completed");
    }

    public void stopSimulation() {
        System.out.println("Stopping simulation " + stopped);
        stopped = true;
    }

    public void stopSimulationManually(){
        System.out.println("Stopping simulation manually");
        stopped = true;
        if(ride.getEbike().getState() == EBikeState.IN_USE){
            ride.getEbike().setState(EBikeState.AVAILABLE);
            eventBus.publish(RIDE_UPDATE_ADDRESS, ride.toString());
        }
    }
}