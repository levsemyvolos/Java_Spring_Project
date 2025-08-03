# LearnLingua - Language Learning Platform

A comprehensive backend application for language learning using spaced repetition system (SRS), built with Spring Boot and modern Java technologies.

## 🏗️ Architecture & Design Patterns

### **Layered Architecture**

- **Presentation Layer**: REST Controllers + Thymeleaf Views
- **Business Logic Layer**: Service Components with Transaction Management
- **Data Access Layer**: Spring Data JPA Repositories
- **Cross-cutting Concerns**: AOP for logging, Security filters

### **Key Design Patterns**

- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Data transfer and transformation
- **Builder Pattern**: Complex object construction
- **Strategy Pattern**: Different learning algorithms
- **Aspect-Oriented Programming**: Cross-cutting concerns (logging)

## 🛠️ Technology Stack

### **Core Framework**

- **Spring Boot 3.3.0** - Main application framework
- **Spring Security 6** - Authentication & authorization
- **Spring Data JPA** - Data persistence layer
- **Spring AOP** - Cross-cutting concerns

### **Database & Persistence**

- **PostgreSQL** - Primary database
- **Hibernate** - ORM implementation
- **Connection Pooling** - Database connection management

### **Additional Technologies**

- **Lombok** - Boilerplate code reduction
- **Jackson** - JSON serialization/deserialization
- **Thymeleaf** - Server-side templating
- **BCrypt** - Password hashing
- **SLF4J + Logback** - Logging framework

## 📊 Database Schema

### **Core Entities**

```sql
-- Users table with role-based access
users (id, username, password)
roles (id, name)
users_roles (user_id, role_id)

-- Learning cards system
cards (id, word, sentence, translation, synonyms, type)

-- User progress tracking with spaced repetition
users_progress (user_id, card_id, learned_level, last_answered, ease, due, interval, reps, status)

-- Statistics and analytics
user_stats (user_id, total_cards, learned_cards, streak_days, study_time)
```

## 🧠 Spaced Repetition Algorithm

### **Implementation Details**

The system implements a sophisticated SRS algorithm with the following parameters:

```java
// Algorithm Constants
MAX_WORDS_IN_DECK = 5           // Active learning deck size
EASE_INCREMENT = 0.15           // Ease factor increase for correct answers
EASE_DECREMENT = 0.2            // Ease factor decrease for incorrect answers
MIN_EASE = 1.3                  // Minimum ease factor
MIN_INTERVAL = 1 day            // Minimum review interval
MAX_INTERVAL = 365 days         // Maximum review interval
```

### **Learning States**

- **NEW**: Never studied
- **IN_DECK**: Currently being learned
- **READY**: Waiting for review based on interval
- **GRADUATED**: Long-term retention achieved

### **Interval Calculation**

```java
// Correct answer progression
1st repetition: 10 minutes
2nd repetition: 30 minutes
3rd+ repetition: interval * ease_factor

// Incorrect answer: Reset to 1 minute
```

## 🔐 Security Implementation

### **Authentication & Authorization**

- **Form-based authentication** with custom login page
- **Role-based access control** (USER, ADMIN)
- **BCrypt password encoding** for secure storage
- **Method-level security** on sensitive operations

### **Security Configuration**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    // Custom security filter chain
    // Password encoding configuration
    // Authentication provider setup
}
```

### **Access Control**

- **Public endpoints**: Registration, login, static resources
- **Admin-only**: Card management (`/api/cards/**`)
- **Authenticated users**: Learning system (`/api/learn/**`, `/stats`)

## 📈 Logging & Monitoring

### **Aspect-Oriented Logging**

Custom `@Loggable` annotation with AOP implementation:

```java
@Around("@within(com.example.coursework.annotations.Loggable)")
public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
    // Pre-execution logging
    // Method execution
    // Post-execution logging with results
    // Exception handling and logging
}
```

### **Comprehensive Logging**

- **Request/Response logging** with parameters
- **User activity tracking** with roles and IDs
- **Performance monitoring** with execution times
- **Error tracking** with detailed stack traces
- **JSON payload logging** for API requests

## 🌐 REST API Design

### **Learning Endpoints**

```http
GET    /api/learn/get-cards     # Retrieve cards for current learning session
POST   /api/learn/answer        # Submit answers and update progress
```

### **Statistics Endpoints**

```http
GET    /api/stats               # Get user learning statistics
```

### **Admin Endpoints**

```http
GET    /api/cards               # List all cards
POST   /api/cards               # Create new card
PUT    /api/cards/{id}          # Update existing card
DELETE /api/cards/{id}          # Delete card
```

## 🔄 Data Flow Architecture

### **Learning Session Flow**

1. **Card Selection**: Algorithm selects optimal cards based on SRS
2. **Progress Tracking**: Real-time updates to user progress
3. **Statistics Update**: Automated calculation of learning metrics
4. **Deck Management**: Dynamic deck composition for optimal learning

### **Transaction Management**

- **@Transactional** annotations for data consistency
- **Rollback mechanisms** for error scenarios
- **Optimistic locking** for concurrent access

## 📋 Key Features

### **Intelligent Learning System**

- ✅ **Spaced Repetition Algorithm** - Optimized review scheduling
- ✅ **Adaptive Difficulty** - Dynamic ease factor adjustment
- ✅ **Progress Tracking** - Comprehensive learning analytics
- ✅ **Deck Management** - Intelligent card selection and rotation

### **User Management**

- ✅ **Secure Authentication** - BCrypt password hashing
- ✅ **Role-based Authorization** - Admin and user privileges
- ✅ **Session Management** - Secure user sessions
- ✅ **User Progress Persistence** - Long-term learning data

### **Administrative Features**

- ✅ **Card Management System** - CRUD operations for learning content
- ✅ **User Statistics** - Learning progress and performance metrics
- ✅ **Comprehensive Logging** - Detailed audit trail
- ✅ **Data Validation** - Input validation and sanitization

### **Performance & Scalability**

- ✅ **Connection Pooling** - Efficient database connections
- ✅ **Lazy Loading** - Optimized entity relationships
- ✅ **Caching Strategy** - Performance optimization
- ✅ **Transaction Optimization** - Minimal database locks

## 📖 API Documentation

### **Learning API**

The learning system provides RESTful endpoints for managing the spaced repetition learning process:

- **Card Retrieval**: Intelligent selection of cards based on SRS algorithm
- **Answer Processing**: Batch processing of user answers with progress updates
- **Progress Tracking**: Real-time updates to learning statistics

### **Response Examples**

```json
{
  "cardId": 1,
  "word": "hello",
  "translation": "привіт",
  "learnedLevel": 3,
  "dueFormattedTrue": "in 2 hours",
  "dueFormattedFalse": "in 1 minute"
}
```

## 🔧 Development Highlights

### **Code Quality**

- **Clean Architecture** principles
- **SOLID** design principles adherence
- **Comprehensive error handling**
- **Input validation and sanitization**
- **Consistent code formatting** with Lombok

## 🏆 Technical Achievements

This project demonstrates proficiency in:

- **Enterprise Java Development** with Spring ecosystem
- **Database Design** and optimization techniques
- **Security Implementation** with industry best practices
- **Algorithm Implementation** for educational technology
- **RESTful API Design** following OpenAPI standards
- **Aspect-Oriented Programming** for cross-cutting concerns
- **Transaction Management** for data consistency
- **Logging and Monitoring** for production readiness

---

_This project showcases advanced backend development skills using modern Java technologies, demonstrating the ability to build scalable, secure, and maintainable enterprise applications._
