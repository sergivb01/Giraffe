package me.sergivb01.giraffe.utils.report;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public interface ReportListener extends Listener {

    void sendReport(CommandSender sender);

    void start();

    void stop();

}