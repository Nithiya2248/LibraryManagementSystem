# Library Management Website

A beginner-friendly Java Spring Boot + HTML/CSS/JavaScript library management mini-project.

## Features
- Member registration and login. A new member must register before logging in.
- Book catalogue with locally included SVG book-cover illustrations.
- Search books by title, author or category.
- Borrow and return books.
- Librarian/admin demo login and basic book management.

## Technology
- Java 17 or later (tested with Java 21)
- Spring Boot 3.4.1
- Maven
- HTML, CSS and JavaScript

## Run on Windows PowerShell
1. Install JDK 17+ and Apache Maven.
2. Open PowerShell in the folder containing `pom.xml`.
3. Run `mvn spring-boot:run`.
4. Open http://localhost:8080.

## Registration and login
1. Open **Create an account** on the login screen.
2. Enter your name, a unique username and a password (at least 4 characters).
3. Return to login and sign in with the newly registered username and password.

Staff demonstration accounts:
- librarian / 1234
- admin / 1234

## Important limitations
This is an educational prototype. Accounts, books and loans are stored in memory and reset when the server restarts. Passwords are not securely hashed, and role permissions are simplified. Do not use this implementation for real users or real personal data. A production version should use a database, password hashing, session/token authentication and server-side role authorization.


## Registration and login flow

- The website opens on the registration form first.
- A new member must enter a name, unique username, password, and matching confirmation.
- After successful registration, the login form is shown and the new username is filled in.
- The backend rejects login if the username has not been registered or the password is incorrect.
- Usernames must contain 4–20 letters, numbers, or underscores; passwords must be at least 6 characters.

**Important:** This is an educational prototype. Accounts and library data are held in memory and are cleared when the server stops. Passwords are not hashed, so do not use a real or reused password. A production application should use a database and secure password hashing/session management.
