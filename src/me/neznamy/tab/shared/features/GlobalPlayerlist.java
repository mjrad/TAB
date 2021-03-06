package me.neznamy.tab.shared.features;

import java.util.UUID;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public class GlobalPlayerlist implements SimpleFeature, CustomPacketFeature{

	@Override
	public void load() {
		//TODO
	}
	@Override
	public void unload() {
		//TODO
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		PacketPlayOutPlayerInfo add = getAddPacket(connectedPlayer);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == connectedPlayer) continue;
			if (all.getWorldName().equals(connectedPlayer.getWorldName())) continue;
			if (!PluginHooks._isVanished(connectedPlayer)) all.sendCustomPacket(add);
			if (!PluginHooks._isVanished(all)) connectedPlayer.sendCustomPacket(getAddPacket(all));
		}
	}
	public void onQuit(ITabPlayer disconnectedPlayer) {
		PacketPlayOutPlayerInfo remove = getRemovePacket(disconnectedPlayer);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == disconnectedPlayer) continue;
			all.sendCustomPacket(remove);
		}
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
	}
	public PacketPlayOutPlayerInfo getRemovePacket(ITabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(p.getName(), p.getTablistId(), null, 0, null, null));
	}
	public PacketPlayOutPlayerInfo getAddPacket(ITabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(p.getName(), p.getTablistId(), p.getSkin(), (int)p.getPing(), EnumGamemode.CREATIVE, p.getTabFormat(null)));
	}
	@Override
	public UniversalPacketPlayOut onPacketSend(ITabPlayer receiver, UniversalPacketPlayOut packet) {
		if (!(packet instanceof PacketPlayOutPlayerInfo)) return packet;
		if (receiver.getVersion().getMinorVersion() < 8) return packet;
		PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
		if (info.action == EnumPlayerInfoAction.REMOVE_PLAYER) {
			for (PlayerInfoData playerInfoData : info.entries) {
				ITabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
				if (packetPlayer != null) { //player online
					if (!PluginHooks._isVanished(packetPlayer)) {
						//changing to random non-existing player, the easiest way to cancel the removal
						playerInfoData.uniqueId = UUID.randomUUID();
					}
				}
			}
		}
		return info;
	}
	@Override
	public String getCPUName() {
		return "GlobalPlayerlist";
	}
}