package com.test;


import com.heroiclabs.nakama.Client;
import com.heroiclabs.nakama.DefaultClient;
import com.heroiclabs.nakama.Session;
import com.heroiclabs.nakama.api.Rpc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;


public class NakamaClient {
    static final Logger logger = LogManager.getLogger(NakamaClient.class);

    public static void main(String[] args) {
        // Create a new client instance
        Client client = new DefaultClient("defaultkey", "127.0.0.1", 7349, false);

        // Authenticate a user with email and password
        Session session;
        try {
            String deviceId = UUID.randomUUID().toString();
            session = client.authenticateDevice(deviceId).get();
            logger.info("Successfully authenticated: {}", session);
        } catch (Exception e) {
            logger.error("Error upon auth", e);
            return;
        }

        // Define the payload for the RPC call
        String payload = "{\"hash\": \"52134b7da25a819faf06e0980ff46f103367c2051442f5c38fb4d9e9da99ae02\"}";

        // Call the custom RPC endpoint
        try {
            Rpc rpcResponse = client.rpc(session, "countHash", payload).get();
            logger.info("RPC response: {}", rpcResponse.getPayload());
        } catch (Exception e) {
            logger.error("Error upon sending RPC request", e);
        }

    }
}
