//package org.example;
//
//import application.EBikeServiceImpl;
//import application.ports.EBikeRepository;
//import infrastructure.adapters.map.MapCommunicationAdapter;
//import io.vertx.core.Future;
//import io.vertx.core.Vertx;
//import io.vertx.core.buffer.Buffer;
//import io.vertx.core.http.HttpClient;
//import io.vertx.core.http.HttpClientRequest;
//import io.vertx.core.http.HttpClientResponse;
//import io.vertx.core.json.JsonObject;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.util.concurrent.CompletableFuture;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class EBikeServiceTest {
//
//    private EBikeRepository repository;
//    private MapCommunicationAdapter mapCommunicationAdapter;
//    private EBikeServiceImpl ebikeService;
//    private HttpClient httpClientMock;
//    private Vertx vertx;
//
//    @BeforeEach
//    public void setUp() {
//        // Inizializza Vertx
//        vertx = Vertx.vertx();
//
//        // Mock del repository e del client HTTP
//        repository = Mockito.mock(EBikeRepository.class);
//        httpClientMock = Mockito.mock(HttpClient.class);
//
//        // Inizializza l'adattatore di comunicazione con Vertx e il client HTTP mockato
//        mapCommunicationAdapter = new MapCommunicationAdapter(vertx, "http://localhost:8081") {
//            protected HttpClient createHttpClient() {
//                return httpClientMock;
//            }
//        };
//
//        // Inizializza il servizio con i mock
//        ebikeService = new EBikeServiceImpl(repository, mapCommunicationAdapter);
//    }
//
//    @Test
//    public void testCreateEBike() {
//        JsonObject ebike = new JsonObject()
//                .put("id", "ebike1")
//                .put("state", "AVAILABLE")
//                .put("batteryLevel", 100)
//                .put("location", new JsonObject().put("x", 10.0f).put("y", 20.0f));
//
//        // Mock del comportamento del repository
//        when(repository.save(any(JsonObject.class))).thenReturn(CompletableFuture.completedFuture(null));
//
//        // Mock della richiesta HTTP
//        HttpClientRequest httpClientRequestMock = mock(HttpClientRequest.class);
//        HttpClientResponse httpClientResponseMock = mock(HttpClientResponse.class);
//
//        when(httpClientRequestMock.putHeader(anyString(), anyString())).thenReturn(httpClientRequestMock);
//        when(httpClientRequestMock.send(any(Buffer.class))).thenReturn(Future.succeededFuture(httpClientResponseMock));
//        when(httpClientMock.request(any(), anyInt(), anyString(), anyString()))
//                .thenReturn(Future.succeededFuture(httpClientRequestMock));
//
//        // Esegui il metodo createEBike
//        CompletableFuture<JsonObject> result = ebikeService.createEBike("ebike1", 10.0f, 20.0f);
//
//        // Asserzioni
//        assertEquals(ebike, result.join());
//        verify(repository, times(1)).save(ebike);
//        verify(httpClientMock, times(1)).request(any(), anyInt(), anyString(), anyString());
//    }
//}
