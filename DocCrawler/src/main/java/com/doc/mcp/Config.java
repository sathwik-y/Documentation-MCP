package com.doc.mcp;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.client.RestClient;

import com.mongodb.client.MongoClients;

import io.micrometer.observation.ObservationRegistry;


@Configuration
public class Config {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;

    @Value("spring.ai.ollama.embedding.options.model")
    private String ollamaEmbeddingModel;


    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(MongoClients.create(mongoUri), mongoDatabase);
    }


    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi();
    }

    @Bean
    public EmbeddingModel embeddingModel(OllamaApi ollamaApi) {
        OllamaOptions options = OllamaOptions.builder()
                .model(ollamaEmbeddingModel)
                .build();

        ModelManagementOptions managementOptions = ModelManagementOptions.builder()
                .pullModelStrategy(PullModelStrategy.WHEN_MISSING)
                .build();

        return new OllamaEmbeddingModel(
                ollamaApi,
                options,
                ObservationRegistry.create(),
                managementOptions
        );
    }
    @Bean
    public VectorStore vectorStore(MongoTemplate mongoTemplate, EmbeddingModel embeddingModel) {
    return MongoDBAtlasVectorStore.builder(mongoTemplate, embeddingModel)
        .collectionName("vector_store")          
        .vectorIndexName("default")         
        .initializeSchema(true)                         
        .build();
}


}
