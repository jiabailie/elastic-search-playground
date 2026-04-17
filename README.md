# Elasticsearch Playground

`elastic-search-playground` is a small Java project for trying common Elasticsearch features locally.

It currently includes:

- a Docker Compose stack for Elasticsearch and Kibana
- a simple Java CLI app
- sample book documents
- example commands for:
  - index creation
  - bulk indexing
  - full-text search
  - terms aggregation
  - bool queries
  - fuzzy queries
  - paginated queries
  - filters
  - sorting
  - highlighting
  - range queries
  - document update/delete/upsert

## Requirements

- Java 17
- Maven 3.9+
- Docker and Docker Compose

## Project Layout

```text
.
├── docker-compose.yml
├── pom.xml
├── README.md
└── src
    └── main
        ├── java/com/muyao/elasticsearchplayground
        └── resources/books.json
```

## Start Elasticsearch And Kibana

From the project root:

```bash
cd /Users/yangruiguo/Documents/elastic-search-playground
docker compose up -d
```

Services:

- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`

Check health:

```bash
curl http://localhost:9200
```

## Build The Java Project

```bash
mvn clean package
```

This produces:

```text
target/elastic-search-playground-1.0-SNAPSHOT.jar
```

## Run The Playground Commands

The app defaults to `http://localhost:9200`.

### 1. Create The Index

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar create-index
```

### 2. Seed Sample Documents

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar seed-books
```

### 3. Search Documents

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar search-books elasticsearch
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar search-books analytics
```

### 4. Run A Terms Aggregation

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar aggregate-tags
```

### 5. Filter By Category

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar filter-category search
```

### 6. Sort By Price

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar sort-price asc
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar sort-price desc
```

### 7. Run A Range Query

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar range-year 2021 2023
```

### 8. Highlight Search Terms

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar highlight-description search
```

### 9. Run A Bool Query

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar search-bool query search
```

### 10. Run A Fuzzy Query

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar search-fuzzy Elasticsarch
```

### 11. Run A Paginated Query

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar search-page search 0 2
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar search-page search 2 2
```

### 12. Update A Book Price

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar update-price book-1 88.75
```

### 13. Delete A Book

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar delete-book book-4
```

### 14. Upsert A Book From JSON

Sample input file:

```text
src/main/resources/book-upsert.json
```

Command:

```bash
java -jar target/elastic-search-playground-1.0-SNAPSHOT.jar upsert-book src/main/resources/book-upsert.json
```

## Use Kibana

Open:

```text
http://localhost:5601
```

Then:

1. open Dev Tools
2. inspect the `books` index
3. run your own search and aggregation queries

Example Kibana query:

```json
GET books/_search
{
  "query": {
    "match": {
      "description": "search"
    }
  }
}
```

Example aggregation:

```json
GET books/_search
{
  "size": 0,
  "aggs": {
    "popular_tags": {
      "terms": {
        "field": "tags"
      }
    }
  }
}
```

Example filter:

```json
GET books/_search
{
  "query": {
    "term": {
      "category": "search"
    }
  }
}
```

Example sort:

```json
GET books/_search
{
  "sort": [
    {
      "price": {
        "order": "desc"
      }
    }
  ]
}
```

Example range query:

```json
GET books/_search
{
  "query": {
    "range": {
      "publishedYear": {
        "gte": 2021,
        "lte": 2023
      }
    }
  }
}
```

Example highlighting:

```json
GET books/_search
{
  "query": {
    "match": {
      "description": "search"
    }
  },
  "highlight": {
    "fields": {
      "description": {}
    }
  }
}
```

Example bool query:

```json
GET books/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "multi_match": {
            "query": "query",
            "fields": ["title", "description", "tags"]
          }
        }
      ],
      "filter": [
        {
          "term": {
            "category": "search"
          }
        }
      ]
    }
  }
}
```

Example fuzzy query:

```json
GET books/_search
{
  "query": {
    "match": {
      "title": {
        "query": "Elasticsarch",
        "fuzziness": "AUTO"
      }
    }
  }
}
```

Example partial update:

```json
POST books/_update/book-1
{
  "doc": {
    "price": 88.75
  }
}
```

Example delete:

```json
DELETE books/_doc/book-4
```

## Optional Environment Variable

If Elasticsearch is not running on `localhost:9200`, set:

```bash
export ELASTICSEARCH_URL=http://localhost:9200
```

The Java client will use that value automatically.

## Stop The Stack

```bash
docker compose down
```

To remove persisted Elasticsearch data as well:

```bash
docker compose down -v
```

## Notes

- this is a playground, not a production service
- security is disabled in Docker Compose for simplicity
- the project is meant for local feature exploration and quick experiments

## CI And Tests

The project includes:

- GitHub Actions CI for `verify` and package
- unit tests for the command dispatcher
- Testcontainers-based integration tests for Elasticsearch-backed operations

Run unit and integration tests locally:

```bash
mvn verify
```
