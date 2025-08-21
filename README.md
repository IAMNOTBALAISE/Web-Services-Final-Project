Watch Store – Microservices Architecture

**Project Overview**

This project implements a Watch Store Management System using a Spring Boot microservices architecture. It is designed to demonstrate Domain-Driven Design (DDD), modularity, and containerized deployment.

The system manages the core domains of a luxury watch business: Customers, Products, Orders, and Service Plans, all accessible through an API Gateway. Each service is independently developed, deployed, and connected to its own database for scalability and fault isolation.
 
**Architecture**

The system follows a microservices architecture with the following core services:

**API Gateway Service** – Central entry point that routes requests to the appropriate microservice.

**Customer Service** – Manages customer accounts, profiles, and contact information.

**Product Service** – Handles the watch catalog, including inventory, brands, and details.

**Order Service** – Processes customer purchases, payments, and order tracking.

**Service Plan Service** – Manages warranty plans, extended services, and after-sales coverage.


**Supporting components**:

Docker & docker-compose for service orchestration

Gradle for build and dependency management

PlantUML diagrams (C4L1.puml, C4L2.puml, DDD.puml) for visualizing architecture and domain design

**Databases**

Each microservice has its own database, ensuring loose coupling and scalability:

Customer Service → PostgreSQL

Product Service → MySQL

Order Service → MySQL

Service Plan Service → MongoDB

API Gateway → stateless, routes requests only

This separation allows each service to evolve independently and use the most suitable database technology.


**Diagrams**

C4L1.puml – System context (watch store overview)

C4L2.puml – Container/Component architecture

DDD.puml – Domain model for the Watch Store

All diagrams can be rendered with PlantUML
.

**Features**

Microservices-based Watch Store system

API Gateway pattern for request routing

Domain-Driven Design (DDD) principles

Separate databases per microservice (PostgreSQL, MySQL, MongoDB)

Containerized deployment with Docker Compose

Scalable & modular structure

UML architecture documentation with PlantUML

**License**

This project is for academic purposes. You are welcome to extend and adapt it further.
