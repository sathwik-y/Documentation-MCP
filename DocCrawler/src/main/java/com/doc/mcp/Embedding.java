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
            
            if (articleContent.length() > 1_000_000) { 
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
