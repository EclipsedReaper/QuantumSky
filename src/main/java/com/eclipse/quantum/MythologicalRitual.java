package com.eclipse.quantum;

import com.eclipse.quantum.config.Config;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.floor;
import static java.lang.Math.round;

public class MythologicalRitual {
    private static List<List<Vec3d>> vectors = null;
    private static int currentPhase = 0;
    private static long lastParticleTime = 0;
    private static boolean tempDisable = false;
    private static boolean foundFirstBurrow = false;

    private static Vec3d lastFoundEnchantParticle = new Vec3d(0, 0, 0);
    private static Vec3d lastFoundEnchantedHitParticle = new Vec3d(0, 0, 0);
    private static Vec3d lastFoundCritParticle = new Vec3d(0, 0, 0);
    private static Vec3d lastFoundFireworkParticle = new Vec3d(0, 0, 0);
    private static Vec3d lastFoundDrippingLavaParticle = new Vec3d(0, 0, 0);

    private static Map<Vec3d, String> warpMap = new HashMap<>();

    static {
        warpMap.put(new Vec3d(-250, 130, 45), "castle");
        warpMap.put(new Vec3d(42, 122, 69), "tower");
        warpMap.put(new Vec3d(-2, 70, -69), "hub");
        warpMap.put(new Vec3d(-161, 61, -99), "crypt");
        warpMap.put(new Vec3d(-75, 76, 80), "museum");
    }

    public static void init() {
        ClientReceiveMessageEvents.GAME.register(MythologicalRitual::onChatMessage);
    }

    public static void reset() {
        vectors = null;
        currentPhase = 0;
        lastParticleTime = 0;
        QuantumSky.burrowWaypoint = null;
        tempDisable = false;
        resetParticleLocations();
    }

    private static void resetParticleLocations() {
        lastFoundEnchantParticle = new Vec3d(0, 0, 0);
        lastFoundEnchantedHitParticle = new Vec3d(0, 0, 0);
        lastFoundCritParticle = new Vec3d(0, 0, 0);
        lastFoundFireworkParticle = new Vec3d(0, 0, 0);
        lastFoundDrippingLavaParticle = new Vec3d(0, 0, 0);
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (vectors == null) {
            vectors = new ArrayList<>();
            vectors.add(new ArrayList<>());
        }
        if (!Config.isDianaHelperActive() || tempDisable) return;
        switch (packet.getParameters().getType()) {
            case ParticleType<?> type when type.equals(ParticleTypes.FIREWORK):
                lastFoundFireworkParticle = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                evaluateParticles(packet);
                break;
            case ParticleType<?> type when type.equals(ParticleTypes.ENCHANTED_HIT):
                lastFoundEnchantedHitParticle = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                evaluateParticles(packet);
                break;
            case ParticleType<?> type when type.equals(ParticleTypes.ENCHANT):
                lastFoundEnchantParticle = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                evaluateParticles(packet);
                break;
            case ParticleType<?> type when type.equals(ParticleTypes.CRIT):
                lastFoundCritParticle = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                evaluateParticles(packet);
                break;
            case ParticleType<?> type when type.equals(ParticleTypes.DRIPPING_LAVA):
                lastFoundDrippingLavaParticle = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                evaluateParticles(packet);
                break;
            default:
                break;
        }
    }

    private static void evaluateParticles(ParticleS2CPacket packet) {
        if (lastFoundFireworkParticle.isInRange(lastFoundDrippingLavaParticle, 10)
                && lastFoundFireworkParticle.isInRange(lastFoundEnchantParticle, 10)
                && packet.getParameters().getType().equals(ParticleTypes.FIREWORK)) {
            echoParticle(packet);
        } else if (foundFirstBurrow) {
            if (lastFoundCritParticle.isInRange(lastFoundEnchantParticle, 2)
                    && !lastFoundCritParticle.isInRange(lastFoundEnchantedHitParticle, 2)
                    && packet.getParameters().getType().equals(ParticleTypes.CRIT)) {
                burrowParticle(packet);
            }
        } else {
            if (lastFoundCritParticle.isInRange(lastFoundEnchantParticle, 2)
                    && lastFoundCritParticle.isInRange(lastFoundEnchantedHitParticle, 2)
                    && packet.getParameters().getType().equals(ParticleTypes.CRIT)) {
                burrowParticle(packet);
            }
        }
    }

    private static void echoParticle(ParticleS2CPacket packet) {
        if (lastParticleTime + 1000 < System.currentTimeMillis()) {
            currentPhase++;
            if (currentPhase > 1) {
                QuantumSky.scheduler.schedule(MythologicalRitual::calculateWaypoint, 3, TimeUnit.SECONDS);
            } else {
                vectors.add(currentPhase, new ArrayList<>());
            }
            MinecraftClient.getInstance().player.sendMessage(Text.of("[§dQuantum§bSky§r] §a" + currentPhase + "/2 echoes processed."), false);
        }
        lastParticleTime = System.currentTimeMillis();
        if (!vectors.isEmpty() && currentPhase > 0) {
            if (vectors.get(currentPhase - 1).size() == 2) {
                vectors.get(currentPhase - 1).set(1, new Vec3d(packet.getX(), packet.getY(), packet.getZ()));
            } else {
                vectors.get(currentPhase - 1).add(new Vec3d(packet.getX(), packet.getY(), packet.getZ()));
            }
        }
    }

    private static void burrowParticle(ParticleS2CPacket packet) {
        // Center on the block: block center is at (floor + 0.5)
        Vec3d pos = new Vec3d(floor(packet.getX()), floor(packet.getY()), floor(packet.getZ()));
        QuantumSky.burrowWaypoint = pos;
    }

    private static void calculateWaypoint() {
        tempDisable = false;
        currentPhase = 0;

        List<Vec3d> lineStarts = new ArrayList<>();
        List<Vec3d> lineDirs = new ArrayList<>();

        for (List<Vec3d> vectorList : vectors) {
            if (vectorList.size() != 2) {
                continue;
            }
            Vec3d A = vectorList.get(0);
            Vec3d B = vectorList.get(1);

            double dx = B.x - A.x;
            double dz = B.z - A.z;
            double magnitude = Math.sqrt(dx * dx + dz * dz);

            if (magnitude == 0) {
                throw new IllegalArgumentException("[QuantumSky] Failed to calculate waypoint: Input points are the same.");
            }

            Vec3d dir = new Vec3d(dx / magnitude, 0, dz / magnitude);
            lineStarts.add(A);
            lineDirs.add(dir);
        }

        if (lineStarts.size() < 2) {
            System.err.println("[QuantumSky] Error: Not enough lines to triangulate waypoint.");
            return;
        }

        Vec3d intersection = findIntersectionXZ(lineStarts.get(0), lineDirs.get(0), lineStarts.get(1), lineDirs.get(1));

        Vec3d firstA = vectors.get(0).get(0);
        Vec3d firstB = vectors.get(0).get(1);
        Vec3d secondA = vectors.get(1).get(0);
        Vec3d secondB = vectors.get(1).get(1);

        if (intersection.distanceTo(firstB) >= intersection.distanceTo(firstA) ||
                intersection.distanceTo(secondB) >= intersection.distanceTo(secondA)) {
            System.err.println("[QuantumSky] Error: Incorrect burrow triangulation. Intersection is not closer to second echo than first.");
            return;
        }

        QuantumSky.burrowWaypoint = intersection;

        if (Config.isDianaAutoWarpActive()) {
            warpIfNeeded(intersection);
        }
    }

    private static Vec3d findIntersectionXZ(Vec3d pos1, Vec3d dir1, Vec3d pos2, Vec3d dir2) {
        double denominator = (dir1.x * dir2.z - dir1.z * dir2.x);
        if (Math.abs(denominator) < 1e-6) {
            throw new IllegalArgumentException("[QuantumSky] Failed to calculate line intersection for waypoint: Input lines are parallel.");
        }

        double t1 = ((pos2.x - pos1.x) * dir2.z - (pos2.z - pos1.z) * dir2.x) / denominator;
        double intersectX = pos1.x + t1 * dir1.x;
        double intersectZ = pos1.z + t1 * dir1.z;
        return new Vec3d(intersectX, 0, intersectZ);
    }

    private static void warpIfNeeded(Vec3d waypoint) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        Vec3d playerPos = client.player.getPos();
        double playerDistance = playerPos.distanceTo(waypoint);

        double minWarpDistance = Double.MAX_VALUE;
        String warpName = null;
        for (Map.Entry<Vec3d, String> entry : warpMap.entrySet()) {
            double warpDistance = entry.getKey().distanceTo(waypoint);
            if (warpDistance < minWarpDistance) {
                minWarpDistance = warpDistance;
                warpName = entry.getValue();
            }
        }

        if (minWarpDistance < playerDistance) {
            Random random = new Random();
            client.player.networkHandler.sendPacket(
                    new ChatMessageC2SPacket(
                            "/warp " + warpName,
                            Instant.now(),
                            random.nextLong(),
                            null,
                            new LastSeenMessageList.Acknowledgment(0, new BitSet())
                    )
            );
            System.out.println("[QuantumSky] Auto-warping to " + warpName);
        }
    }

    private static void onChatMessage(Text message, boolean overlay) {
        if (message.getString().contains("You dug out a Griffin Burrow!") && Config.isDianaHelperActive()) {
            foundFirstBurrow = true;
            reset();
        }
        if (message.getString().contains("You finished the Griffin burrow chain!") && Config.isDianaHelperActive()) {
            reset();
            foundFirstBurrow = false;
        }
    }
}
