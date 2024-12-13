# E-Bike Microservices

## English Version

### Overview
The E-Bike Sharing System is a microservices-based project designed following the principles of Domain-Driven Design (DDD). It allows users to rent and manage e-bikes through a scalable, modular architecture. The project integrates features like user management, ride tracking, and e-bike inventory management, ensuring high scalability, extensibility, and real-time updates.

---

### Features
- **User Features**:
  - Sign up and login functionality.
  - View available e-bikes.
  - Start and stop rides.
  - Recharge user credit.
  - Monitor ongoing rides.
- **Admin Features**:
  - Add new e-bikes.
  - Recharge e-bikes.
  - View all users and their credit balance.
  - Monitor all e-bikes and ongoing rides.
- **System Features**:
  - Real-time updates using WebSocket.
  - REST API for user, ride, e-bike, and map management.
  - Scalable microservices architecture with Docker containerization.

---

### Architecture
The project is built with the following components:

1. **Microservices**:
   - **User Service**: Handles user authentication, credit management, and user details.
   - **E-Bike Service**: Manages the inventory and status of e-bikes.
   - **Ride Service**: Manages the lifecycle of user rides.
   - **Map Service**: Tracks e-bike positions and updates in real time.

2. **Tools and Frameworks**:
   - **Spring Boot**: Core framework for building REST APIs.
   - **Prometheus & Micrometer**: Monitoring and metrics collection.
   - **Eureka**: Service discovery.
   - **Docker & Docker Compose**: Containerization and orchestration.

3. **Patterns**:
   - Domain-Driven Design.
   - Hexagonal Architecture.
   - Anti-Corruption Layer (ACL) for inter-service communication.

---

### Getting Started
#### Prerequisites
- Docker
- Docker Compose
- Java 17+
- Gradle

#### Setup and Run
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/e-bike-sharing-system.git
   cd e-bike-sharing-system
   ```
2. Build the project:
   ```bash
   ./gradlew build
   ```
3. Start the services:
   ```bash
   docker-compose up
   ```
4. Access the services:
   - API Gateway: `http://localhost:8081`
   - Prometheus: `http://localhost:9090`
   - Eureka Dashboard: `http://localhost:8761`

---

### Contributing
1. Fork the repository.
2. Create a new branch:
   ```bash
   git checkout -b feature/your-feature
   ```
3. Commit your changes and push to your branch.
4. Open a Pull Request.

---

## Versione Italiana

### Panoramica
Il sistema di bike sharing è un progetto basato su microservizi, progettato seguendo i principi del Domain-Driven Design (DDD). Consente agli utenti di noleggiare e gestire e-bike tramite un'architettura scalabile e modulare. Il progetto include funzionalità come gestione degli utenti, tracciamento delle corse e gestione dell'inventario delle e-bike, garantendo alta scalabilità, estendibilità e aggiornamenti in tempo reale.

---

### Funzionalità
- **Funzionalità per l'utente**:
  - Registrazione e accesso.
  - Visualizzazione delle e-bike disponibili.
  - Avvio e conclusione delle corse.
  - Ricarica del credito.
  - Monitoraggio delle corse in corso.
- **Funzionalità per l'amministratore**:
  - Aggiunta di nuove e-bike.
  - Ricarica delle e-bike.
  - Visualizzazione di tutti gli utenti e dei loro saldi di credito.
  - Monitoraggio di tutte le e-bike e le corse in corso.
- **Funzionalità di sistema**:
  - Aggiornamenti in tempo reale tramite WebSocket.
  - API REST per la gestione di utenti, corse, e-bike e mappa.
  - Architettura microservizi scalabile con containerizzazione Docker.

---

### Architettura
Il progetto è composto dai seguenti componenti:

1. **Microservizi**:
   - **User Service**: Gestisce autenticazione, gestione del credito e dettagli degli utenti.
   - **E-Bike Service**: Gestisce l'inventario e lo stato delle e-bike.
   - **Ride Service**: Gestisce il ciclo di vita delle corse degli utenti.
   - **Map Service**: Traccia le posizioni delle e-bike e aggiorna in tempo reale.

2. **Strumenti e Framework**:
   - **Spring Boot**: Framework principale per la costruzione delle API REST.
   - **Prometheus & Micrometer**: Monitoraggio e raccolta delle metriche.
   - **Eureka**: Service discovery.
   - **Docker & Docker Compose**: Containerizzazione e orchestrazione.

3. **Pattern**:
   - Domain-Driven Design.
   - Architettura esagonale.
   - Anti-Corruption Layer (ACL) per la comunicazione tra servizi.

---

### Iniziare
#### Prerequisiti
- Docker
- Docker Compose
- Java 17+
- Gradle

#### Installazione e Esecuzione
1. Clonare il repository:
   ```bash
   git clone https://github.com/tuo-username/e-bike-sharing-system.git
   cd e-bike-sharing-system
   ```
2. Compilare il progetto:
   ```bash
   ./gradlew build
   ```
3. Avviare i servizi:
   ```bash
   docker-compose up
   ```
4. Accedere ai servizi:
   - API Gateway: `http://localhost:8081`
   - Prometheus: `http://localhost:9090`
   - Dashboard Eureka: `http://localhost:8761`

---

### Contributi
1. Forkare il repository.
2. Creare un nuovo branch:
   ```bash
   git checkout -b feature/la-tua-funzionalita
   ```
3. Commit delle modifiche e push al branch.
4. Aprire una Pull Request.

---
