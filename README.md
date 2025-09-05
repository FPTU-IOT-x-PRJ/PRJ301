# PRJ301 - Java Web Application

## 📌 Introduction
PRJ301 is a Java Web Application project developed as part of coursework at FPT University.  
The project demonstrates the usage of **Java Servlet, JSP, JSTL, and JDBC** to build a dynamic web application with MVC architecture.

## 🚀 Features
- User authentication (login, logout, session management)
- CRUD operations with database (MySQL / SQL Server)
- JSP-based views with JSTL
- Form validation and error handling
- Deployment using Apache Tomcat
- Organized MVC architecture (Model – View – Controller)

## 🛠️ Tech Stack
- **Language:** Java 8+  
- **Backend:** Servlet, JSP, JSTL  
- **Frontend:** HTML, CSS, JavaScript, JSP  
- **Database:** MySQL / SQL Server (via JDBC)  
- **Build Tool:** Apache Ant (`build.xml`)  
- **IDE:** NetBeans  

## 📂 Project Structure
```

PRJ301/
│── nbproject/         # NetBeans project config
│── src/               # Java source code (controllers, models, DAOs)
│── web/               # JSP pages, static resources (CSS, JS, images)
│── build.xml          # Ant build script
│── README.md          # Project documentation

```

## ⚙️ Installation & Run
1. **Clone the repository**
   ```bash
   git clone https://github.com/FPTU-IOT-x-PRJ/PRJ301.git
   cd PRJ301
   ```

2. **Setup Database**

   * Create a database (`EduPlan`).
   * Update database connection in DAO/utility class (`DBContext.java`).
   * Run application for DB automaticly init.

3. **Build & Deploy**

   * Open project in **NetBeans**.
   * Build using Ant (`build.xml`).
   * Deploy to **Apache Tomcat** (version 9 recommended).
   * Access app at: `http://localhost:8080/EduPlan`


This project is for educational purposes at FPT University.
Feel free to use and modify for learning.
