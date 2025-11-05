# Build stage
FROM rust:1.89-alpine AS build
WORKDIR /app

# Install required packages for Alpine
RUN apk add --no-cache musl-dev pkgconfig openssl-dev

# Copy dependency files
COPY Cargo.toml Cargo.lock ./
COPY diesel.toml ./

# Create dummy source and build dependencies
RUN mkdir src && echo "fn main() {}" > src/main.rs
RUN cargo build --release
RUN rm -rf src

# Copy source and build
COPY ./src ./src
COPY ./migrations ./migrations
RUN touch src/main.rs
RUN cargo build --release

# Runtime stage
FROM alpine:latest
RUN apk add --no-cache ca-certificates
WORKDIR /app

# Copy the binary
COPY --from=build /app/target/release/router /app/router

EXPOSE 8080
CMD ["./router"]