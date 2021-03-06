package org.neo4j.unmanaged;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.LocalConnector;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;

import java.io.IOException;
import java.net.ServerSocket;

import static junit.framework.Assert.assertEquals;

public class DummyResourceTest {
    private GraphDatabaseAPI db;
    private CommunityNeoServer server;

    @Before
    public void before() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);

        server = CommunityServerBuilder
                .server()
                .onPort(serverSocket.getLocalPort())
                .withThirdPartyJaxRsPackage("org.neo4j.unmanaged", "/unmanaged")
                .build();

        server.start();
        db = server.getDatabase().getGraph();
    }

    @After
    public void after() {
        server.stop();
    }

    @Test
    public void shouldReturnAllTheNodes() {
        Transaction tx = db.beginTx();
        db.createNode().setProperty("name", "Mark");
        db.createNode().setProperty("name", "Dave");
        tx.success();
        tx.close();

        JsonNode response = jerseyClient()
                .resource(server.baseUri().toString() + "unmanaged/dummy/all-nodes")
                .get(ClientResponse.class)
                .getEntity(JsonNode.class);

        assertEquals("Dave", response.get("n.name").get(0).asText());
        assertEquals("Mark", response.get("n.name").get(1).asText());
    }

    private Client jerseyClient() {
        DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
        defaultClientConfig.getClasses().add(JacksonJsonProvider.class);
        return Client.create(defaultClientConfig);
    }
}