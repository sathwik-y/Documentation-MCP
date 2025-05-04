package com.mcp;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
@Service
public class MCP {

    private final VectorStore vectorStore;

    
    public MCP(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(name="Spring_Documentation", description="Contains the latest documentation for Spring AI.")
    public List<Document> getDoc(String query){
        List<Document> results = vectorStore.similaritySearch(SearchRequest
                                                            .builder()
                                                            .query(query).
                                                            topK(15).
                                                            build());
        return results;
    }
}
