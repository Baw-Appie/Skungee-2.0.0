package com.skungee.spigot.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.NetworkVariable;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.packets.NetworkVariablePacket;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class EffSetNetworkVariable extends Effect {

	static {
		Skript.registerEffect(EffSetNetworkVariable.class, "set network variable %objects% to %objects%");
	}

	private Variable<?> networkVariable;
	private Variable<?> targetVariable;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		networkVariable = (Variable<?>) exprs[0];
		targetVariable = (Variable<?>) exprs[1];
		return true;
	}

	protected void setNetworkVariable(String name, NetworkVariable.Value[] values) {
		NetworkVariable variable = new NetworkVariable(name, values);
		variable.setChanger(NetworkVariable.SkriptChangeMode.SET);
		SpigotSkungee instance = SpigotSkungee.getInstance();
		try {
			new NetworkVariablePacket(variable).send();
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			instance.consoleMessage("Timed out attempting to send network variable {" + variable.getVariableString() + "}");
		}
	}

	@Override
	protected void execute(Event event) {
		String nVarName = networkVariable.getName().toString(event);
		String tVarName = targetVariable.getName().toString(event);
		Boolean local = targetVariable.isLocal();
		Boolean nVarList = nVarName.endsWith("::*");
		Boolean tVarList = tVarName.endsWith("::*");
		if(nVarList) nVarName = nVarName.substring(0, nVarName.length() - 1);

		Object variable = Variables.getVariable(tVarName, event, local);
		System.out.println(variable);
		System.out.println(tVarName);
		if(variable instanceof Map) {
			// List Array
			for(Object index : ((Map<Object, Object>) variable).keySet()) {
				ch.njol.skript.variables.SerializedVariable.Value value = Classes.serialize(((Map) variable).get(index));
				if(tVarList && index != null) {
					// List
					setNetworkVariable(nVarName+index, new NetworkVariable.Value[] { new NetworkVariable.Value(value.type, value.data) });
				} else if (!tVarList && index == null) {
					// Not List
					setNetworkVariable(nVarName, new NetworkVariable.Value[] { new NetworkVariable.Value(value.type, value.data) });
				}
			}
		} else if (variable instanceof Object) {
			// Normal
			ch.njol.skript.variables.SerializedVariable.Value value = Classes.serialize(variable);
			setNetworkVariable(nVarName, new NetworkVariable.Value[] { new NetworkVariable.Value(value.type, value.data) });
		} else {
			// Null
			Skript.error("NullPointerError: You're trying to save a null");
		}

	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "Set network variable";
	}

}
