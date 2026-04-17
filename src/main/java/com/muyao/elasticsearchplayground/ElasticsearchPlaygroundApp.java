package com.muyao.elasticsearchplayground;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

public class ElasticsearchPlaygroundApp {
    public static void main(String[] args) throws Exception {
        ElasticsearchClient client = ElasticsearchClientFactory.createClient();
        CommandDispatcher dispatcher = new CommandDispatcher(new BookIndexService(client));

        try {
            dispatcher.dispatch(args);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
