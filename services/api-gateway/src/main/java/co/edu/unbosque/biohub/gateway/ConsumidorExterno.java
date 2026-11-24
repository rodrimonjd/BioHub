package co.edu.unbosque.biohub.gateway;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

public class ConsumidorExterno {

    private final String id;
    private final String organizacion;
    private final String hashApiKey;
    private int limitePorMinuto;

    private long minutoActual = -1;
    private int solicitudesMinutoActual = 0;
    private int rechazadasMinutoActual = 0;

    public ConsumidorExterno(String id, String organizacion, String hashApiKey, int limitePorMinuto) {
        this.id = id;
        this.organizacion = organizacion;
        this.hashApiKey = hashApiKey;
        this.limitePorMinuto = limitePorMinuto;
    }

    public boolean autenticar(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        return hashApiKey.equalsIgnoreCase(hash(apiKey));
    }

    public boolean registrarSolicitud() {
        return registrarSolicitud(Instant.now());
    }

    synchronized boolean registrarSolicitud(Instant ahora) {
        long minuto = ahora.getEpochSecond() / 60;
        if (minuto != minutoActual) {
            minutoActual = minuto;
            solicitudesMinutoActual = 0;
            rechazadasMinutoActual = 0;
        }
        if (solicitudesMinutoActual >= limitePorMinuto) {
            rechazadasMinutoActual++;
            return false;
        }
        solicitudesMinutoActual++;
        return true;
    }

    public static long segundosParaSiguienteMinuto(Instant ahora) {
        return 60 - (ahora.getEpochSecond() % 60);
    }

    public static String hash(String texto) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public String getId() { return id; }
    public String getOrganizacion() { return organizacion; }
    public synchronized int getLimitePorMinuto() { return limitePorMinuto; }

    public synchronized void cambiarLimite(int limitePorMinuto) {
        this.limitePorMinuto = limitePorMinuto;
    }

    public synchronized int getSolicitudesMinutoActual() { return solicitudesMinutoActual; }

    synchronized int solicitudesEn(Instant ahora) {
        return ahora.getEpochSecond() / 60 == minutoActual ? solicitudesMinutoActual : 0;
    }

    synchronized int rechazadasEn(Instant ahora) {
        return ahora.getEpochSecond() / 60 == minutoActual ? rechazadasMinutoActual : 0;
    }
}
