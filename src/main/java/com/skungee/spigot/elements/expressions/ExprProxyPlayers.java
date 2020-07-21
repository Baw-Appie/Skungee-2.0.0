package com.skungee.spigot.elements.expressions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.packets.PlayersPacket;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprProxyPlayers extends SimpleExpression<SkungeePlayer> {

	static {
		Skript.registerExpression(ExprProxyPlayers.class, SkungeePlayer.class, ExpressionType.SIMPLE, "[(all [[of] the]|the)] prox(ied|y) players [o(f|n) server[s] %-skungeeservers%]");
	}

	@Nullable
	private Expression<SkungeeServer> servers;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		servers = (Expression<SkungeeServer>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected SkungeePlayer[] get(Event event) {
		PlayersPacket packet = new PlayersPacket();
		if (servers != null)
			packet.setServers(servers.getArray(event));
		try {
			return packet.send().stream().toArray(SkungeePlayer[]::new);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Skript.info(e.getMessage() + " (proxied players)");
			return null;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends SkungeePlayer> getReturnType() {
		return SkungeePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event == null || servers == null) // Skript Debug
			return "proxied players";
		return "proxied players on " + servers.toString(event, debug);
	}

}
