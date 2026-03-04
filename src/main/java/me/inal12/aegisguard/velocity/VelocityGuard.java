package me.inal12.aegisguard.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Plugin(id = "aegisguard", name = "AegisGuard", version = "1.0.0", description = "Advanced Velocity Proxy Protection", authors = {
        "inal12" })
public class VelocityGuard {

    private final ProxyServer server;
    private final Logger logger;
    private final Map<String, Integer> ipJoins = new HashMap<>();

    // CONFIG AYARLARI (Velocity için basitçe)
    private final int IP_LIMIT = 1;

    @Inject
    public VelocityGuard(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("========================================");
        logger.info("AegisGuard Velocity Modu Aktif!");
        logger.info("Yazar: inal12");
        logger.info("Sunucu Proxy seviyesinde korunuyor.");
        logger.info("========================================");
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String ip = event.getConnection().getRemoteAddress().getAddress().getHostAddress();

        // Aktif oyuncu sayısını kontrol et
        long count = server.getAllPlayers().stream()
                .filter(p -> p.getRemoteAddress().getAddress().getHostAddress().equals(ip))
                .count();

        if (count >= IP_LIMIT) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text("Sunucumuzda bir IP adresi üzerinden en fazla " + IP_LIMIT + " hesap açılabilir!",
                            NamedTextColor.RED)));
            logger.warn("[ANTI-BOT] Bot saldırısı önlendi! IP: " + ip);
        }
    }
}
