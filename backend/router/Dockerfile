FROM rust:1.89

WORKDIR /app

# Copy dependency files
COPY Cargo.toml Cargo.lock ./
COPY diesel.toml ./

# Create dummy source and build dependencies
RUN mkdir src && echo "fn main() {}" > src/main.rs
RUN cargo build
RUN rm -rf src

COPY ./src ./

EXPOSE 8080

# Use cargo watch for hot reload
CMD ["cargo", "run"]