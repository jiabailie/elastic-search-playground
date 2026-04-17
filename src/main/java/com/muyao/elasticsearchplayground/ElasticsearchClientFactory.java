package com.muyao.elasticsearchplayground;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public final class ElasticsearchClientFactory {
    private ElasticsearchClientFactory() {
    }

    public static ElasticsearchClient createClient() {
        String host = System.getenv().getOrDefault("ELASTICSEARCH_URL", "http://localhost:9200");
        return createClient(host);
    }

    public static ElasticsearchClient createClient(String host) {
        RestClient restClient = RestClient.builder(HttpHost.create(host)).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
