package com.aurgiyalgo.BetterTownyWars.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.aurgiyalgo.BetterTownyWars.BetterTownyWars;
import com.aurgiyalgo.BetterTownyWars.wars.War;
import com.aurgiyalgo.BetterTownyWars.wars.WarType;

public class BTWCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] args) {
		if (args.length == 0) {
			showHelp(sender);
			return true;
		}
		switch (args[0]) {
		case "help": {
			showHelp(sender);
		}
			break;
		case "declare": {
			if (args.length < 3) {
				sender.sendMessage(BetterTownyWars.getInstance().getLanguageHandler().getMessage("not-enough-arguments"));
				return true;
			}
			WarType warType = WarType.getWarType(args[1]);
			if (warType == null) {
				sender.sendMessage(ChatColor.RED + "Invalid war type!");
				return true;
			}
			try {
				Method method = warType.getClassType().getMethod("declareCommandHandler", CommandSender.class, Command.class, String.class, String[].class);
				method.invoke(null, sender, cmd, cmdString, args);
			} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
			break;
		case "finish": {
			if (args.length < 3) {
				sender.sendMessage(BetterTownyWars.getInstance().getLanguageHandler().getMessage("not-enough-arguments"));
				return true;
			}
			WarType warType = WarType.getWarType(args[1]);
			if (warType == null) {
				sender.sendMessage(ChatColor.RED + "Invalid war type!");
				return true;
			}
			try {
				Method method = warType.getClassType().getMethod("finishCommandHandler", CommandSender.class, Command.class, String.class, String[].class);
				method.invoke(null, sender, cmd, cmdString, args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			break;
		case "list": {
			listActiveWars(sender);
		}
			break;
		default: {
			sender.sendMessage(ChatColor.RED + "Type a correct argument (/btw help)");
		}
			break;
		}
		return true;
	}
	
	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
				"&3------ &bBetterTownyWars &3------&r\n"
				+ "\n"
				+ "&7&oSubcommands:&r\n"
				+ "&f  - help &8- &7Shows this message&7&r\n"
				+ "&f  - declare [type of war] [town/nation] &8- &7Declare a war to a nation&r\n"
				+ "&f  - finish [type of war] [town/nation] &8- &8Finish a war&r\n"
				+ "&f  - list &8- &7List of active wars"));
	}
	
	private void listActiveWars(CommandSender sender) {
		List<War> allWars = BetterTownyWars.getInstance().getWarManager().getAllWars();
		if (allWars.size() <= 0) {
			sender.sendMessage(ChatColor.RED + "There are no active wars");
			return;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Active wars Now" + ChatColor.RESET + "\n\n");
		for (War war : allWars) {
			builder.append(ChatColor.WHITE + " - ");
			builder.append(war.getMemberName(war.getMembers().get(0)));
			for (int i = 1; i < war.getMembers().size(); i++) {
				builder.append(", ");
				builder.append(war.getMemberName(war.getMembers().get(i)));
			}
			builder.append(ChatColor.GRAY + " (" + war.getType().getFormattedName() + ")\n");
		}
		sender.sendMessage(builder.toString());
	}

}
