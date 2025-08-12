# Document RAG
A backend-only Spring Boot application powered by Spring AI that processes uploaded PDFs.
It stores each PDF in local storage, extracts its text using Apache PDFBox, splits the content into semantic chunks, and generates vector embeddings with OpenAI.
These embeddings are stored locally, enabling efficient retrieval-based question answering over the document's content.

## Built with:
* Java 21
* Spring Boot 3.x**
* Spring AI
* Apache PDfBox

## Features
* PDF Upload: Accepts only `.pdf` files via REST API.
* Text Extraction: Uses `Loader` from PDFBox.
* Chunking: Configurable chunk size & overlap.
* Embeddings: Uses OpenAI `text-embedding-3-small` (1536 dims).
* Storage: PDF stored in `/storage`, embeddings stored locally (e.g., H2/pgvector).
* Q&A Endpoint: Ask questions and get AI-generated answers from uploaded PDFs.

## Running the application

```bash
mvn spring-boot:run
```

## API Endpoints

### Upload PDF
``` bash
curl -F "file=@sample.pdf" https://localhost:8080/api/documents/upload
```

### Ask a question
```bash
curl -X POST "https://localhost:8080/api/documents/query" \
-H "Content-Type: application/json" \
-d '{"question": "What is this document about?"}'
```

