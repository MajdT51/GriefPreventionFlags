package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.SetFlagResult;
import me.ryanhamshire.GPFlags.TextMode;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlagDef_ExitMessage extends PlayerMovementFlagDefinition {

    private final String prefix;

    public FlagDef_ExitMessage(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
        this.prefix = plugin.getFlagsDataStore().getMessage(Messages.EnterExitPrefix);
    }

    @Override
    public void onChangeClaim(Player player, Location lastLocation, Location to, Claim claimFrom, Claim claimTo) {
        if (lastLocation == null) return;
        Flag flag = this.getFlagInstanceAtLocation(lastLocation, player);
        if (flag == null) return;

        // get specific ExitMessage flag of origin claim and EnterMessage flag of destination claim
        Flag flagFrom = plugin.getFlagManager().getFlag(claimFrom, this);
        Flag flagToEnter = plugin.getFlagManager().getFlag(claimTo, plugin.getFlagManager().getFlagDefinitionByName("EnterMessage"));

        // Don't repeat the exit message of a claim in certain cases
        if (claimFrom != null && claimTo != null) {
            // moving to parent claim, and the sub claim does not have its own exit message
            if (claimFrom.parent == claimTo && (flagFrom == null || !flagFrom.getSet())) {
                return;
            }
            // moving to sub-claim, and the sub claim does not have its own enter message
            if (claimTo.parent == claimFrom && (flagToEnter == null || !flagToEnter.getSet())) {
                return;
            }
        }

        String message = flag.parameters;
        if (claimFrom != null) {
            message = message.replace("%owner%", claimFrom.getOwnerName()).replace("%name%", player.getName());
        }

        Util.sendClaimMessage(player, TextMode.Info, prefix + message);
    }

    @Override
    public String getName() {
        return "ExitMessage";
    }

    @Override
    public SetFlagResult validateParameters(String parameters) {
        if (parameters.isEmpty()) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.MessageRequired));
        }

        return new SetFlagResult(true, this.getSetMessage(parameters));
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.AddedExitMessage, parameters);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.RemovedExitMessage);
    }

}
