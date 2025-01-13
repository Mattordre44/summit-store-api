# Summit Store API

Summit Store API is a RESTful backend service designed to power a mobile application for a store specializing in hiking gear. This API provides robust functionality for managing product catalogs, brands, and image uploads, making it a comprehensive solution for e-commerce applications.

## Features

- **Product Management:**
  - Manage products, their types, and variants.
  - Handle product attributes such as name, description, price, and associated brand.

- **Image Upload:**
  - Upload and retrieve images using an object storage system (MinIO).
  - Support for multiple image types including product images and brand logos.

- **Database Integration:**
  - PostgreSQL database for reliable and scalable data storage.

- **Modular Design:**
  - Clean and modular codebase for easy scalability and maintainability.

## Technologies Used

- **Backend Framework:** Spring Boot (Java)
- **Database:** PostgreSQL
- **Object Storage:** MinIO (compatible with AWS S3 for production as it use the S3 sdk)
- **Testing Frameworks:** Testcontainers and JUnit
- **Build Tool:** Maven
- **API Documentation:** Swagger (optional)

## Getting Started

### Prerequisites

Ensure you have the following installed on your system:

- Java 23 or higher
- Docker and Docker Compose
- Maven

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/summit-store-api.git
   cd summit-store-api
   ```

2. Start the services using Docker Compose:
   ```bash
   docker-compose up
   ```
   This will set up:
   - PostgreSQL database
   - MinIO object storage

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Environment Variables

The following environment variables can be set to configure the application:

- `POSTGRES_DB`: Name of the database (default: `summit-store`)
- `POSTGRES_USER`: PostgreSQL username (default: `postgres`)
- `POSTGRES_PASSWORD`: PostgreSQL password (default: `password`)
- `MINIO_ROOT_USER`: MinIO root username (default: `minio`)
- `MINIO_ROOT_PASSWORD`: MinIO root password (default: `password`)
- `MINIO_DEFAULT_BUCKET`: Default bucket for storing images (default: `products-images`)

### Testing

Run tests using Maven:
```bash
mvn test
```
Integration tests are configured using Testcontainers to spin up a PostgreSQL and a MinIO container.

### Project Structure

- **`/src/main/java`** - Application source code.
- **`/src/test/java`** - Unit and integration tests.
- **`./compose.yml`** - Docker Compose configuration for PostgreSQL and MinIO.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

Special thanks to all open-source contributors whose libraries and tools made this project possible.
