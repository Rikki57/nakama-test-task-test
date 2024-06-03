package com.nakamatest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroiclabs.nakama.Client;
import com.heroiclabs.nakama.DefaultClient;
import com.heroiclabs.nakama.Session;
import com.heroiclabs.nakama.api.Rpc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;


public class NakamaTest {
    private static Client client;
    private static Session session;
    private static ObjectMapper objectMapper;

    private static final String DEFAULT_VERSION = "1.0.0";
    private static final String VERSION_1_0_1 = "1.0.1";
    private static final String DEFAULT_TYPE = "core";
    private static final String PLUGIN_TYPE = "plugin";
    private static final String HASH_FOR_V_1_0_0 = "52134b7da25a819faf06e0980ff46f103367c2051442f5c38fb4d9e9da99ae02";
    private static final String HASH_FOR_V_1_0_1 = "14323f668ba7513bf78c5070e60d469a2004810223ae02c2eca1723af95ab629";
    private static final String HASH_FOR_PLUGIN = "b0e39a513222542f3532ce96bd189e254b5febcc6505cf53cebe17359e3f5b2f";

    @BeforeAll
    public static void setup(){
        objectMapper = new ObjectMapper();
        client = new DefaultClient("defaultkey", "127.0.0.1", 7349, false);
        String deviceId = UUID.randomUUID().toString();
        try {
            session = client.authenticateDevice(deviceId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error initializing session", e);
        }
    }

    @Test
    void testNormalBehaviorWithEmptyRequest() throws ExecutionException, InterruptedException, JsonProcessingException {
        Request request = new Request();
        request.setHash(HASH_FOR_V_1_0_0);

        String payload = objectMapper.writeValueAsString(request);
        Rpc rpcResponse = client.rpc(session, "countHash", payload).get();
        Response response = objectMapper.readValue(rpcResponse.getPayload(), Response.class);

        assertEquals(HASH_FOR_V_1_0_0, response.getHash());
        assertEquals(DEFAULT_VERSION, response.getVersion());
        assertEquals(DEFAULT_TYPE, response.getType());
        assertNotEquals("", response.getContent());
    }

    @Test
    void testNormalBehaviorWithFullRequest() throws ExecutionException, InterruptedException, JsonProcessingException {
        Request request = new Request();
        request.setHash(HASH_FOR_V_1_0_1);
        request.setType(DEFAULT_TYPE);
        request.setVersion(VERSION_1_0_1);
        String payload = objectMapper.writeValueAsString(request);

        Rpc rpcResponse = client.rpc(session, "countHash", payload).get();
        ObjectMapper objectMapper = new ObjectMapper();
        Response response = objectMapper.readValue(rpcResponse.getPayload(), Response.class);

        assertEquals(HASH_FOR_V_1_0_1, response.getHash());
        assertEquals(VERSION_1_0_1, response.getVersion());
        assertEquals(DEFAULT_TYPE, response.getType());
        assertNotEquals("", response.getContent());
    }

    @Test
    void testEmptyContentWithWrongHash() throws ExecutionException, InterruptedException, JsonProcessingException {
        // Define the payload for the RPC call
        Request request = new Request();
        request.setHash(HASH_FOR_V_1_0_0);
        request.setType(DEFAULT_TYPE);
        request.setVersion(VERSION_1_0_1);
        String payload = objectMapper.writeValueAsString(request);

        // Call the custom RPC endpoint
        Rpc rpcResponse = client.rpc(session, "countHash", payload).get();
        Response response = objectMapper.readValue(rpcResponse.getPayload(), Response.class);

        assertEquals(HASH_FOR_V_1_0_1, response.getHash());
        assertEquals(VERSION_1_0_1, response.getVersion());
        assertEquals(DEFAULT_TYPE, response.getType());
        assertEquals("", response.getContent());
    }

    @Test
    void testNotDefaultTypeCorrectHashEmptyVersion() throws ExecutionException, InterruptedException, JsonProcessingException {
        // Define the payload for the RPC call
        Request request = new Request();
        request.setHash(HASH_FOR_PLUGIN);
        request.setType(PLUGIN_TYPE);
        String payload = objectMapper.writeValueAsString(request);

        // Call the custom RPC endpoint
        Rpc rpcResponse = client.rpc(session, "countHash", payload).get();
        Response response = objectMapper.readValue(rpcResponse.getPayload(), Response.class);

        assertEquals(HASH_FOR_PLUGIN, response.getHash());
        assertEquals(DEFAULT_VERSION, response.getVersion());
        assertEquals(PLUGIN_TYPE, response.getType());
        assertNotEquals("", response.getContent());
    }
}
