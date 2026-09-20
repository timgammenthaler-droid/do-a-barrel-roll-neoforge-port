package nl.enjarai.doabarrelroll.net;

//? if fabric {
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
//?} else {
/*import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
*///?}
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import nl.enjarai.doabarrelroll.DoABarrelRoll;
import nl.enjarai.doabarrelroll.api.RollEntity;
import nl.enjarai.doabarrelroll.api.event.ServerEvents;
import nl.enjarai.doabarrelroll.config.ModConfigServer;
import nl.enjarai.doabarrelroll.net.packet.*;

public class ServerNetworking {
    public static final ServerConfigHolder<ConfigUpdateAckS2CPacket> CONFIG_HOLDER = new ServerConfigHolder<>(
            //? if fabric {
            FabricLoader.getInstance().getConfigDir().resolve(DoABarrelRoll.MODID + "-server.json"),
            //?} else {
            /*FMLPaths.CONFIGDIR.get().resolve(DoABarrelRoll.MODID + "-server.json"),
            *///?}
            ModConfigServer.CODEC, ConfigUpdateAckS2CPacket::new, ServerEvents::updateServerConfig
    );
    public static final HandshakeServer<ConfigSyncS2CPacket> HANDSHAKE_SERVER = new HandshakeServer<>(
            ConfigSyncS2CPacket::new, CONFIG_HOLDER, player -> !ModConfigServer.canModify(player));

    public static void init() {
        CONFIG_HOLDER.setHandshakeServer(HANDSHAKE_SERVER);

        //? if fabric {
        PayloadTypeRegistry.playC2S().register(ConfigResponseC2SPacket.PACKET_ID, ConfigResponseC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(ConfigUpdateC2SPacket.PACKET_ID, ConfigUpdateC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RollSyncC2SPacket.PACKET_ID, RollSyncC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ConfigSyncS2CPacket.PACKET_ID, ConfigSyncS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ConfigUpdateAckS2CPacket.PACKET_ID, ConfigUpdateAckS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(RollSyncS2CPacket.PACKET_ID, RollSyncS2CPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConfigResponseC2SPacket.PACKET_ID, (payload, context) -> {
            var reply = HANDSHAKE_SERVER.clientReplied(context.player().networkHandler, payload);
            if (reply == HandshakeServer.HandshakeState.RESEND) {
                sendHandshake(context.player());
            } else if (reply == HandshakeServer.HandshakeState.ACCEPTED) {
                ServerPlayNetworking.registerReceiver(context.player().networkHandler, RollSyncC2SPacket.PACKET_ID, (payload1, context1) -> {
                    var rollPlayer = (RollEntity) context1.player();
                    var isRolling = payload1.rolling();
                    var roll = payload1.roll();
                    rollPlayer.doABarrelRoll$setRolling(isRolling);
                    rollPlayer.doABarrelRoll$setRoll(isRolling ? MathHelper.wrapDegrees(roll) : 0);
                });
                ServerPlayNetworking.registerReceiver(context.player().networkHandler, ConfigUpdateC2SPacket.PACKET_ID, (payload1, context1) -> {
                    context1.responseSender().sendPacket(CONFIG_HOLDER.clientSendsUpdate(context1.player(), payload1));
                });
            }
        });
        //?} else {
        /*// NeoForge: payload registration handled via RegisterPayloadHandlersEvent in DoABarrelRollInitializer.
        *///?}
        // The initial handshake is sent in the CommandManagerMixin.

        ServerEvents.SERVER_CONFIG_UPDATE.register((server, config) -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                sendHandshake(player);
            }
        });

        //? if fabric {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            HANDSHAKE_SERVER.playerDisconnected(handler);
        });
        ServerTickEvents.END_SERVER_TICK.register(HANDSHAKE_SERVER::tick);
        //?} else {
        /*NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayerEntity player) {
                HANDSHAKE_SERVER.playerDisconnected(player.networkHandler);
            }
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> HANDSHAKE_SERVER.tick(event.getServer()));
        *///?}
    }

    //? if !fabric {
    /*// Register only server-received (C2S) payloads here.
    // S2C payloads are registered in ClientNetworking.registerClientPayloads() to avoid double registration.
    public static void registerPayloads(PayloadRegistrar registrar) {
        registrar.playToServer(
                ConfigResponseC2SPacket.PACKET_ID, ConfigResponseC2SPacket.PACKET_CODEC,
                (payload, context) -> {
                    var player = (ServerPlayerEntity) context.player();
                    var reply = HANDSHAKE_SERVER.clientReplied(player.networkHandler, payload);
                    if (reply == HandshakeServer.HandshakeState.RESEND) {
                        sendHandshake(player);
                    }
                }
        );
        registrar.playToServer(
                RollSyncC2SPacket.PACKET_ID, RollSyncC2SPacket.PACKET_CODEC,
                (payload, context) -> {
                    var rollPlayer = (RollEntity) context.player();
                    var isRolling = payload.rolling();
                    var roll = payload.roll();
                    rollPlayer.doABarrelRoll$setRolling(isRolling);
                    rollPlayer.doABarrelRoll$setRoll(isRolling ? MathHelper.wrapDegrees(roll) : 0);
                }
        );
        registrar.playToServer(
                ConfigUpdateC2SPacket.PACKET_ID, ConfigUpdateC2SPacket.PACKET_CODEC,
                (payload, context) -> {
                    var player = (ServerPlayerEntity) context.player();
                    var response = CONFIG_HOLDER.clientSendsUpdate(player, payload);
                    PacketDistributor.sendToPlayer(player, response);
                }
        );
    }
    *///?}

    public static void sendHandshake(ServerPlayerEntity player) {
        //? if fabric {
        ServerPlayNetworking.send(player, HANDSHAKE_SERVER.initiateConfigSync(player.networkHandler));
        //?} else {
        /*PacketDistributor.sendToPlayer(player, HANDSHAKE_SERVER.initiateConfigSync(player.networkHandler));
        *///?}
        HANDSHAKE_SERVER.configSentToClient(player.networkHandler);
    }

    public static void sendRollUpdates(Entity entity) {
        var rollEntity = (RollEntity) entity;
        var isRolling = rollEntity.doABarrelRoll$isRolling();
        var roll = rollEntity.doABarrelRoll$getRoll();
        var payload = new RollSyncS2CPacket(entity.getId(), isRolling, roll);

        //? if fabric {
        PlayerLookup.tracking(entity).stream()
                .filter(player -> player != entity)
                .filter(player -> HANDSHAKE_SERVER.getHandshakeState(player).state == HandshakeServer.HandshakeState.ACCEPTED)
                .forEach(player -> ServerPlayNetworking.send(player, payload));
        //?} else {
        /*entity.getEntityWorld().getPlayers().stream()
                .filter(player -> player != entity)
                .filter(player -> player instanceof ServerPlayerEntity)
                .map(player -> (ServerPlayerEntity) player)
                .filter(player -> player.getEntityPos().isInRange(entity.getEntityPos(), 128))
                .filter(player -> HANDSHAKE_SERVER.getHandshakeState(player).state == HandshakeServer.HandshakeState.ACCEPTED)
                .forEach(player -> PacketDistributor.sendToPlayer(player, payload));
        *///?}
    }
}
