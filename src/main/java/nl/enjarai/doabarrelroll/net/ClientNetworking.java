package nl.enjarai.doabarrelroll.net;

//? if fabric {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//?} else {
/*import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
*///?}
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import nl.enjarai.doabarrelroll.api.RollEntity;
import nl.enjarai.doabarrelroll.api.event.ClientEvents;
import nl.enjarai.doabarrelroll.config.ModConfigServer;
import nl.enjarai.doabarrelroll.net.packet.*;

public class ClientNetworking {
    public static final HandshakeClient<ConfigResponseC2SPacket> HANDSHAKE_CLIENT = new HandshakeClient<>(
            ConfigResponseC2SPacket::new,
            ClientEvents::updateServerConfig
    );
    public static final ServerConfigUpdateClient<ConfigUpdateC2SPacket> CONFIG_UPDATE_CLIENT = new ServerConfigUpdateClient<>(
            ConfigUpdateC2SPacket::new
    );

    public static void init() {
        //? if fabric {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncS2CPacket.PACKET_ID, (payload, context) -> {
            var response = HANDSHAKE_CLIENT.handleConfigSync(payload);
            context.responseSender().sendPacket(response);

            if (HANDSHAKE_CLIENT.hasConnected()) {
                ClientPlayNetworking.registerReceiver(RollSyncS2CPacket.PACKET_ID, (payload1, context1) -> {
                    var client = MinecraftClient.getInstance();
                    if (client.world == null) return;
                    var entity = client.world.getEntityById(payload1.entityId());
                    if (entity == null) return;
                    var rollEntity = (RollEntity) entity;
                    rollEntity.doABarrelRoll$setRolling(payload1.rolling());
                    rollEntity.doABarrelRoll$setRoll(MathHelper.wrapDegrees(payload1.roll()));
                });

                ClientPlayNetworking.registerReceiver(ConfigUpdateAckS2CPacket.PACKET_ID, (payload1, context1) -> {
                    CONFIG_UPDATE_CLIENT.updateAcknowledged(payload1);
                });
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> HANDSHAKE_CLIENT.reset());
        //?} else {
        /*NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) -> HANDSHAKE_CLIENT.reset());
        *///?}
    }

    //? if !fabric {
    /*// Register only S2C payloads here.
    // C2S payloads are registered in ServerNetworking.registerPayloads() to avoid double registration.
    public static void registerClientPayloads(PayloadRegistrar registrar) {
        registrar.playToClient(
                ConfigSyncS2CPacket.PACKET_ID, ConfigSyncS2CPacket.PACKET_CODEC,
                (payload, context) -> {
                    var response = HANDSHAKE_CLIENT.handleConfigSync(payload);
                    ClientPacketDistributor.sendToServer(response);
                }
        );
        registrar.playToClient(
                RollSyncS2CPacket.PACKET_ID, RollSyncS2CPacket.PACKET_CODEC,
                (payload, context) -> {
                    var client = MinecraftClient.getInstance();
                    if (client.world == null) return;
                    var entity = client.world.getEntityById(payload.entityId());
                    if (entity == null) return;
                    var rollEntity = (RollEntity) entity;
                    rollEntity.doABarrelRoll$setRolling(payload.rolling());
                    rollEntity.doABarrelRoll$setRoll(MathHelper.wrapDegrees(payload.roll()));
                }
        );
        registrar.playToClient(
                ConfigUpdateAckS2CPacket.PACKET_ID, ConfigUpdateAckS2CPacket.PACKET_CODEC,
                (payload, context) -> CONFIG_UPDATE_CLIENT.updateAcknowledged(payload)
        );
    }
    *///?}

    public static void sendRollUpdate(RollEntity entity) {
        if (HANDSHAKE_CLIENT.hasConnected()) {
            boolean rolling = entity.doABarrelRoll$isRolling();
            float roll = entity.doABarrelRoll$getRoll();
            //? if fabric {
            ClientPlayNetworking.send(new RollSyncC2SPacket(rolling, roll));
            //?} else {
            /*ClientPacketDistributor.sendToServer(new RollSyncC2SPacket(rolling, roll));
            *///?}
        }
    }

    public static void sendConfigUpdatePacket(ModConfigServer config) {
        //? if fabric {
        ClientPlayNetworking.send(CONFIG_UPDATE_CLIENT.prepUpdatePacket(config));
        //?} else {
        /*ClientPacketDistributor.sendToServer(CONFIG_UPDATE_CLIENT.prepUpdatePacket(config));
        *///?}
    }
}
