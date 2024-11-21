package org.example;

import io.vertx.core.Vertx;

public class Launcher {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new EurekaVerticle("VERTX-SERVICE", "localhost:vertx-service:8080", 8080));
    }
}
