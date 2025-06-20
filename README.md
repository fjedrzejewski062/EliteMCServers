# EliteMCServers 🧱🎮

**EliteMCServers** is a full-featured Java Spring Boot + Thymeleaf web application for discovering and managing Minecraft servers.  
The platform includes user authentication (with OAuth2), server listing and moderation, user comments, Stripe integration for donations, and an admin panel.

## Features 🚀

### Server Management
- 🔍 **Browse Servers**: Explore public Minecraft servers with filtering and search.
- ➕ **Add/Edit Server**: Authenticated users can create and manage their own server listings.
- 📄 **Server Details**: Each server includes name, IP address, version, game mode, status, description, and comments.
- ✅ **Moderation Status**: Servers can be marked as pending, approved, or rejected.

### Comments
- 💬 **Commenting System**: Users can leave comments under server listings.
- 🧹 **Admin Moderation**: Admins can manage and delete inappropriate comments.

### User Account
- 👤 **Register/Login**: Standard login with optional Google OAuth2 authentication.
- 🛠️ **User Dashboard**: Manage your profile and servers. Admins can ban or delete users.

### Admin Panel
- 🧠 **Admin Dashboard**: View and manage all users, servers, and comments.
- 🚫 **User Moderation**: Admins can ban or delete users and moderate server content.

### Donations
- 💳 **Stripe Integration**: Support the app development through secure Stripe payments.

## Design Pattern 📐

- ✅ **Facade Pattern**: Clean separation of concerns by delegating business logic to facade services.

## How it works 🔍

1. **Browse or submit servers** — Users can explore and submit Minecraft server listings.
2. **Interact with the community** — Leave and manage comments on server pages.
3. **Moderation system** — All content is validated and moderated by admins.
4. **Support the project** — Optional donations via Stripe.
5. **Admin access** — Admins have full control over the platform through a secure dashboard.

## Tech Stack 🛠

- **Java 21**
- **Maven** – Build and dependency management
- **Spring Boot 3.x** – Application framework
- **Spring MVC** – Web layer
- **Spring Security + OAuth2 Client** – Authentication and authorization
- **Spring Data JPA** – Data persistence
- **Hibernate** – ORM implementation
- **Spring Boot Starter Validation** – Input validation
- **Thymeleaf** – Server-side templating engine
- **Thymeleaf Extras Spring Security 6** – Security integration in views
- **Stripe Java SDK** – Stripe payment integration
- **H2 Database** – In-memory database for development and testing
- **Lombok** – Reduces boilerplate code
- **JUnit 5** – Unit testing
- **Mockito** – Mocking for unit tests
- **Spring Boot Starter Test** – Testing support
- **Spring Security Test** – Tools for testing Spring Security
- **Testcontainers** – Integration testing using Docker containers

## How to Run ⚙️

1. Clone the repository:
    ```bash
    git clone https://github.com/your-user/elite-mc-servers.git
    ```

2. Navigate to the project directory:
    ```bash
    cd elite-mc-servers
    ```

3. Run the application:
    ```bash
    ./mvnw spring-boot:run
    ```

4. Open in your browser:
    ```
    http://localhost:8080
    ```

## Stripe Configuration 💳

Make sure to define your Stripe keys in `application.properties` or `.yml`:

```properties
stripe.secretKey=sk_test_...
stripe.publishableKey=pk_test_...
