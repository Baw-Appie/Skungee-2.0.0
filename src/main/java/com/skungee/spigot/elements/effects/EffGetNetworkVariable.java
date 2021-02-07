package com.skungee.spigot.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.skungee.shared.objects.NetworkVariable;
import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.packets.NetworkVariablePacket;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class EffGetNetworkVariable extends Effect {

	static {
		Skript.registerEffect(EffGetNetworkVariable.class, "get network variable %objects% to variable %objects%");
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

	@Override
	protected void execute(Event event) {
		String nVarName = networkVariable.getName().toString(event);
		String tVarName = targetVariable.getName().toString(event);
		Boolean local = targetVariable.isLocal();
		Boolean muliple = nVarName.endsWith("::*");



		SpigotSkungee instance = SpigotSkungee.getInstance();
		List<NetworkVariable> variables = new ArrayList<>();;
		try {
			NetworkVariablePacket packet = new NetworkVariablePacket(new NetworkVariable[0]).setNames(nVarName);
			variables.addAll(instance.getJapsonClient().sendPacket(packet));
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			instance.consoleMessage("Timed out attempting to send network variable {" + nVarName + "}");
		}

		for(NetworkVariable variable : variables) {
			for(NetworkVariable.Value value : variable.getValues()) {
				if(muliple) Variables.setVariable(variable.getVariableString().replace(nVarName.substring(0, nVarName.length()-1), tVarName.substring(0, tVarName.length()-1)), Classes.deserialize(value.type, value.data), event, local);
				else Variables.setVariable(variable.getVariableString().replace(nVarName, tVarName), Classes.deserialize(value.type, value.data), event, local);
			}
		}

	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "Set network variable";
	}

}
