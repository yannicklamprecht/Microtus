package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class PlayerHeldIntegrationTest {

    @Test
    void playerHeld(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0)).join();

        player.getInventory().setItemStack(1, ItemStack.of(Material.STONE));
        assertEquals(ItemStack.AIR, player.getItemInMainHand());
        assertEquals(0, player.getHeldSlot());

        player.addPacketToQueue(new ClientHeldItemChangePacket((short) 1));
        player.interpretPacketQueue();

        assertEquals(ItemStack.of(Material.STONE), player.getItemInMainHand());
        assertEquals(1, player.getHeldSlot());
    }

    @Test
    void playerHeldEvent(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0)).join();

        player.getInventory().setItemStack(1, ItemStack.of(Material.STONE));
        assertEquals(ItemStack.AIR, player.getItemInMainHand());
        assertEquals(0, player.getHeldSlot());

        player.addPacketToQueue(new ClientHeldItemChangePacket((short) 1));
        var listener = env.listen(PlayerChangeHeldSlotEvent.class);
        listener.followup(event -> {
            assertEquals(player, event.getPlayer());
            assertEquals(1, event.getSlot());
        });
        player.interpretPacketQueue();
        assertEquals(ItemStack.of(Material.STONE), player.getItemInMainHand());
        assertEquals(1, player.getHeldSlot());
    }
}
