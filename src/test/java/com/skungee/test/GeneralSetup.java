package com.skungee.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import com.sitrica.japson.client.JapsonClient;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Handler;
import com.sitrica.japson.shared.ReturnablePacket;
import com.skungee.shared.Packets;

public class GeneralSetup {

	@Test
	public void start() throws UnknownHostException, SocketException, TimeoutException, InterruptedException, ExecutionException {
		JapsonClient client = new JapsonClient(1337);
		client.enableDebug();
		JapsonServer server = new JapsonServer(1337);
		server.enableDebug();
		server.registerHandlers(new Reflections("com.skungee.proxy.handlers", "com.skungee.bungeecord.handlers")
				.getSubTypesOf(Handler.class).stream().map(clazz -> {
					try {
						return clazz.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
						return null;
					}
				}).filter(handler -> handler != null).toArray(Handler[]::new));
		server.registerHandlers(new Handler(Packets.API.getPacketId()) {

			@Override
			public JsonObject handle(InetAddress address, int port, JsonObject json) {
				JsonObject returning = new JsonObject();
				assertNotNull(json);
				assertTrue(json.has("test"));
				String got = json.get("test").getAsString();
				assertEquals(got, "Hello World!");
				returning.addProperty("return", "Returning!");
				return returning;
			}

		});
		String returned = client.sendPacket(new ReturnablePacket<String>(Packets.API.getPacketId()) {

			@Override
			public String getObject(JsonObject json) {
				return json.get("return").getAsString();
			}

			@Override
			public JsonObject toJson() {
				JsonObject object = new JsonObject();
				object.addProperty("test", "Hello World!");
				return object;
			}

		});
		assertEquals(returned, "Returning!");
	}

}
