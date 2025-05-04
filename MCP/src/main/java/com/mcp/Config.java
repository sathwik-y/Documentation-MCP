package com.mcp;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.client.RestClient;

import com.mongodb.client.MongoClients;

import io.micrometer.observation.ObservationRegistry;


@Configuration
public class Config {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(MongoClients.create(""), "spring");
    }


    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi.Builder().build();
    }

    @Bean
    public EmbeddingModel embeddingModel(OllamaApi ollamaApi) {
        OllamaOptions options = OllamaOptions.builder()
                .model("nomic-embed-text")
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

    // private final String projectId = "mcp-documentation";
    // private final String location = "asia-south1";

        // @Bean
    // public VertexAiEmbeddingConnectionDetails vertexAiEmbeddingConnectionDetails(){
    //     return  VertexAiEmbeddingConnectionDetails.builder()
    //             .projectId(projectId)
    //             .location(location)
    //             .build();
    // }

    // @Bean
    // public EmbeddingModel embeddingModel(VertexAiEmbeddingConnectionDetails connectionDetails) throws IOException {
    //     VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
    //             .model(VertexAiTextEmbeddingOptions.DEFAULT_MODEL_NAME) // or "text-embedding-004"
    //             .build();

    //     return new VertexAiTextEmbeddingModel(connectionDetails, options);
    // }