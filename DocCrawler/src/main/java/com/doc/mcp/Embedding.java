package com.doc.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.springframework.ai.vectorstore.VectorStore;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class Embedding extends WebCrawler {

    private final VectorStore vectorStore;
    private static final AtomicInteger count = new AtomicInteger(0);

    public Embedding(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        return url.getURL().startsWith("https://docs.spring.io");
    }
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        try {
            String htmlContent = new String(page.getContentData());
            org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
            String articleContent = doc.select("article.doc").text();
            String title = doc.title();
    
            htmlContent = null;
            doc = null;
            
            if (articleContent.length() > 1_000_000) { // 1MB limit
                System.out.println("Content too large, truncating: " + url + " (" + articleContent.length() + " chars)");
                articleContent = articleContent.substring(0, 1_000_000);
            }
    
            if (articleContent.isEmpty()) {
                System.out.println("Skipping page with empty content: " + url);
                return;
            }
    
            String urlPath = url.replaceFirst("https://docs.spring.io/", "");
            
            List<String> contentChunks = splitContentIntoChunks(articleContent, 2000);
            
            for (int i = 0; i < contentChunks.size(); i++) {
                String chunk = contentChunks.get(i);
                
                Map<String, Object> metadata = Map.of(
                    "url", url,
                    "title", title, 
                    "chunk", i + 1,
                    "total_chunks", contentChunks.size(),
                    "path", urlPath
                );
                
                vectorStore.add(List.of(
                    new org.springframework.ai.document.Document(chunk, metadata)
                ));
                
                contentChunks.set(i, null);
            }
    
            System.out.println("Successfully added page in " + contentChunks.size() + 
                             " chunks: " + url + " (" + count.addAndGet(contentChunks.size()) + 
                             " total documents stored)");
    
        } catch (OutOfMemoryError oom) {
            System.err.println("Out of memory processing: " + url + " - skipping");
            System.gc(); 
        } catch (Exception e) {
            System.err.println("Failed to add page " + url + ": " + e.getMessage());
        }
    }
    

    private List<String> splitContentIntoChunks(String content, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();
        
        if (content == null) {
            return chunks;
        }
                if (content.length() <= maxChunkSize) {
            chunks.add(content);
            return chunks;
        }
        
        int position = 0;
        int contentLength = content.length();
        
        while (position < contentLength) {
            int endPosition = Math.min(position + maxChunkSize, contentLength);
            chunks.add(content.substring(position, endPosition));
            position = endPosition;
            
            if (chunks.size() % 50 == 0) {
                System.gc();
            }
        }
        
        return chunks;
    }


    
}
// @Override
// public void visit(Page page) {
//     String url = page.getWebURL().getURL();
//     String htmlContent = new String(page.getContentData());
//     org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
//     String articleContent = doc.select("article.doc").text();

//     if (articleContent.isEmpty()) {
//         System.out.println("Skipping page with empty content: " + url);
//         return;
//     }

//     try {
//         // Creating a Spring AI Document with content
//         org.springframework.ai.document.Document aiDocument = new org.springframework.ai.document.Document(articleContent, Map.of("meta1", "meta1"));

//         // Add the document to the vectorStore
//         vectorStore.add(List.of(aiDocument));

//         System.out.println("Successfully added page: " + url + " (" + count.incrementAndGet() + " documents stored)");

//     } catch (Exception e) {
//         System.err.println("Failed to add page " + url + ": " + e.getMessage());
//     }
// }

/*
    
package com.doc.mcp;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;


public class SpringCrawler extends WebCrawler {
    public static int count = 0;
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        return url.getURL().startsWith("https://docs.spring.io/spring-ai/reference/");
    }

@Override
public void visit(Page page) {
    String url = page.getWebURL().getURL();
    // Get the HTML content of the page
    String htmlContent = new String(page.getContentData());

    // Use Jsoup to parse the HTML
    Document doc = Jsoup.parse(htmlContent);

    // Extract content from <article class="doc">
    String articleContent = doc.select("article.doc").text(); // Use .html() to get full HTML inside the article

    // Print out the extracted content
    System.out.println("\nPage URL: " + url);
    // System.out.println("Article Content: \n" + articleContent);

    // Optional: Save the content to a file for easier inspection
    try {
        count++;
        Files.writeString(Paths.get("crawled.html"), articleContent);
        System.out.println("Saved it there");
    } catch (IOException e) {
        e.printStackTrace();
    }
    System.out.println("saved a total of: " + count);
}
}
 */