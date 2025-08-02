# Googol – Search Engine with Spring Boot and RMI

## Description

**Googol** is a Java-based search engine that uses **RMI (Remote Method Invocation)** for remote communication and a web interface built with **Spring Boot**. The project allows users to perform searches, index URLs, and view real-time statistics.

---

## Project Architecture

- **RMI Server**  
  Core component of the search engine, responsible for indexing, searching, and tracking statistics.

- **Spring Boot Web Layer**  
  Handles the user interface, REST controllers, and WebSockets for real-time communication.

- **Controllers**  
  Receive client requests and interact with the RMI server to fetch data or send commands.

- **HTML Templates (Thymeleaf)**  
  Display the main menu, search results, URL indexing interface, and real-time stats (e.g., barrel data and top 10 searched terms), updated via WebSockets.

---

## Requirements

- Java 17 or higher  
- Maven 3.8 or higher  
- RMI server running on port `1099` (localhost)

---

## How to Run the Project

1. Navigate to the project directory
2. Build the application (skipping tests):
mvn clean install -DskipTests
3. Run the RMI server and its components:

**Gateway**
mvn exec:java -Dexec.mainClass="com.googol.rmi.Gateway"

**Downloader**  
mvn exec:java -Dexec.mainClass="com.googol.rmi.Downloader" -Dexec.classpathScope=runtime

**Barrel**  
mvn exec:java -Dexec.mainClass="com.googol.rmi.ProcessosBarrel"

**Googol Web Application**  
java -jar target/googol-web-0.0.1-SNAPSHOT.jar

4. Open your browser and go to:  
[http://localhost:8080](http://localhost:8080)
