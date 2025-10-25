#!/bin/bash

# ThreadScope Build Script
echo "Building ThreadScope..."

# Clean and compile
mvn clean compile

# Package all modules
mvn package -DskipTests

echo "Build complete!"
echo ""
echo "To run the example:"
echo "cd examples/spring-boot-example"
echo "mvn spring-boot:run"
echo ""
echo "Then visit:"
echo "- Application: http://localhost:8080"
echo "- ThreadScope Dashboard: http://localhost:9090"
