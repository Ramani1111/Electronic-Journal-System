# Electronic Journal System (EJS)

## Overview
The Electronic Journal System (EJS) is a web-based platform designed to facilitate the submission, review, and publication of academic articles. It supports multiple user roles including Authors, Reviewers, and Editors, enabling streamlined manuscript management and peer review processes.

---

## Features
- User registration and login with role-based access
- Article submission and management
- Search and view articles
- Review assignment and review submission
- Article publishing
- User management for admin/editor roles
- Reviewer and author dashboards

---

## System Requirements
- **Operating System:** Windows 10 or above
- **Java Development Kit (JDK):** Version 11 or higher
- **MySQL Database:** Version 8.0.27
- **MySQL JDBC Driver:** `mysql-connector-java-8.0.27.jar`
- **IDE or Java compiler:** IntelliJ IDEA, Eclipse, or command-line
- **Browser:** Google Chrome (latest version recommended)

---

## Setup Instructions

### 1. Set Up the Database
- Start your MySQL server.
- Create the database schema and tables:

SQL
_______
CREATE DATABASE journaldb;
USE journaldb;




-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(50) UNIQUE,
    password VARCHAR(50),
    role VARCHAR(20)
);

-- Articles table
CREATE TABLE articles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100),
    content TEXT,
    author_id INT,
    reviewer_id INT DEFAULT NULL,
    status VARCHAR(20),
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
);



-- Published Articles table
CREATE TABLE published_articles (
    article_id INT PRIMARY KEY,
    publish_date TIMESTAMP,
    publish_details VARCHAR(255),
    FOREIGN KEY (article_id) REFERENCES articles(id)
);



-- Reviews table
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    article_id INT,
    reviewer_id INT,
    comments TEXT,
    FOREIGN KEY (article_id) REFERENCES articles(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
);
Insert initial users and articles as needed for testing.
2. Download JDBC Driver
Download the MySQL JDBC driver from here.
Save mysql-connector-java-8.0.27.jar in your project directory.
3. Compile the Java Program
Open your command line or terminal in the directory containing the source code.

On Windows:
CopyRun
javac -cp .;path/to/mysql-connector-java-8.0.27.jar *.java
On Linux/macOS:
CopyRun
javac -cp .:path/to/mysql-connector-java-8.0.27.jar *.java


4. Run the Application
On Windows:
CopyRun
java -cp .;path/to/mysql-connector-java-8.0.27.jar Journal
On Linux/macOS:
CopyRun
java -cp .:path/to/mysql-connector-java-8.0.27.jar Journal
5. Usage
When the application starts, follow the prompts:
Register a new user or login with existing credentials.
Use roles (Author, Reviewer, Editor/Admin) to access different features.
Submit articles, assign reviews, publish, etc.
Notes & Testing
Ensure your MySQL server is running and credentials are correctly set in Journal.java.
Test with sample users and articles.
Check console logs for errors and troubleshoot accordingly.
Future Improvements
Implement automated testing
Enhance security
Improve user interface
Add features like password recovery
License
This project is for educational purposes and is provided "as-is" without warranty.


Author: Ramani S
