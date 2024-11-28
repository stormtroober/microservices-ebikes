package application.ports;

public interface MapCommunicationPort {
    void notifyStartRide(String bikeId, String userId);
    void notifyEndRide(String bikeId, String userId);
    void init();
}
