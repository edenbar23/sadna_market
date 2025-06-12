package com.sadna_market.market.InfrastructureLayer.Initialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SimpleFrontendOpener {
    private static final Logger logger = LoggerFactory.getLogger(SimpleFrontendOpener.class);

    private static final String FRONTEND_URL = "http://localhost:5173";

    public void openFrontend() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + FRONTEND_URL);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + FRONTEND_URL);
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + FRONTEND_URL);
            }

            logger.info("🌐 Opening frontend at: {}", FRONTEND_URL);
        } catch (Exception e) {
            logger.warn("❌ Could not open browser automatically: {}", e.getMessage());
            logger.info("💡 Please manually open: {}", FRONTEND_URL);
        }
    }
}