package com.doc.mcp;
import java.io.File;
import java.io.IOException;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

@SpringBootApplication
public class DocCrawler {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DocCrawler.class, args);
    }

@Bean
public CommandLineRunner crawlerRunner(VectorStore vectorStore) {
    return args -> {
        String crawlStorageFolder = System.getProperty("java.io.tmpdir") + File.separator + "springcrawler";
        int numberOfCrawlers = 1;

        File crawlDir = new File(crawlStorageFolder);
        if (crawlDir.exists()) {
            System.out.println("Cleaning up existing crawler directory: " + crawlStorageFolder);
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(crawlDir);
            } catch (IOException e) {
                System.err.println("Failed to delete crawler directory: " + e.getMessage());
                crawlStorageFolder += "_" + System.currentTimeMillis();
            }
        }

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
            config.setPolitenessDelay(1000);
            config.setMaxDepthOfCrawling(2);
            config.setIncludeHttpsPages(true);
            // config.setFollowRedirects(true);
            // config.setResumableCrawling(true); // Enable resumable crawling
            config.setShutdownOnEmptyQueue(true);
            PageFetcher pageFetcher = new PageFetcher(config);
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed("https://docs.spring.io/spring-ai/reference/1.0/index.html");
            controller.addSeed("https://docs.spring.io/spring-boot/index.html");
            controller.addSeed("https://docs.spring.io/spring-framework/reference/index.html");
            controller.addSeed("https://docs.spring.io/spring-data/commons/reference/");
            controller.addSeed("https://docs.spring.io/spring-security/reference/index.html");
            controller.addSeed("https://docs.spring.io/spring-graphql/reference/index.html");
            controller.addSeed("https://docs.spring.io/spring-integration/reference/");
            controller.start(() -> new Embedding(vectorStore), numberOfCrawlers);
        };
    }
}

/*package com.doc.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

@SpringBootApplication
public class McpDocumentationApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(McpDocumentationApplication.class, args);
		String crawlStorageFolder = "/tmp/springcralwer";
        int numberOfCrawlers = 1;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(1);
		config.setMaxPagesToFetch(1);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        controller.addSeed("https://docs.spring.io/spring-ai/reference/api/embeddings.html");
        controller.start(SpringCrawler.class, numberOfCrawlers);
	}

} */