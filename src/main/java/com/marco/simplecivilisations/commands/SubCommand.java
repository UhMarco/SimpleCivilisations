package com.marco.simplecivilisations.commands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.MySQL;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {
    protected final SimpleCivilisations plugin;
    protected final MySQL SQL;

    public SubCommand(SimpleCivilisations plugin) {
        this.plugin = plugin;
        this.SQL = plugin.getSQL();
    }

    public abstract List<String> getLabels();


    public abstract String getDescription();

    public abstract String getUsage();

    public boolean showInHelp = true;

    public abstract void perform(CommandSender sender, String[] args);

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
