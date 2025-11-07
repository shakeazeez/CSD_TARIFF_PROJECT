# Build stage
FROM rust:1.89-bookworm AS build
WORKDIR /app

# Install required packages for Debian
RUN apt-get update && apt-get install -y \
    pkg-config \
    libssl-dev \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

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
FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y \
    ca-certificates \
    libpq5 \
    libssl3 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the binary
COPY --from=build /app/target/release/router /app/router

EXPOSE 8080
CMD ["./router"]