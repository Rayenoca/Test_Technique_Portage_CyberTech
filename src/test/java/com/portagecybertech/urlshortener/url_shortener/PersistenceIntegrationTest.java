package com.portagecybertech.urlshortener.url_shortener;

import com.portagecybertech.urlshortener.url_shortener.service.UrlService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

class PersistenceIntegrationTest {

    @Test
    void shortenedUrlPersistsAcrossContextRestart() throws IOException {
        Path tempDir = Files.createTempDirectory("urlshortener-db");
        Path dbPath = tempDir.resolve("db/urlshortener");
        Files.createDirectories(dbPath.getParent());

        String dbUrl = "jdbc:h2:file:" + dbPath.toString().replace('\\', '/')
                + ";DB_CLOSE_ON_EXIT=FALSE";

        String originalUrl = "https://example.com/" + UUID.randomUUID();
        String shortUrl;

        try (ConfigurableApplicationContext context = buildContext(dbUrl)) {
            UrlService service = context.getBean(UrlService.class);
            shortUrl = service.shorten(originalUrl).shortUrl();
        }

        String shortCode = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);

        try (ConfigurableApplicationContext context = buildContext(dbUrl)) {
            UrlService service = context.getBean(UrlService.class);
            Assertions.assertThat(service.expand(shortCode)).isEqualTo(originalUrl);
        }
    }

    private ConfigurableApplicationContext buildContext(String dbUrl) {
        return new SpringApplicationBuilder(UrlShortenerApplication.class)
                .properties(
                        "spring.main.web-application-type=none",
                        "spring.datasource.url=" + dbUrl,
                        "spring.datasource.username=sa",
                        "spring.datasource.password=",
                        "spring.datasource.driverClassName=org.h2.Driver",
                        "spring.jpa.hibernate.ddl-auto=update",
                        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
                )
                .run();
    }
}
