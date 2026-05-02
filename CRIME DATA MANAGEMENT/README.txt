================================================================
  CRIME DATA MANAGEMENT SYSTEM
  Kumaraguru College of Technology — Java Programming Project
================================================================

TEAM
----
  Yasvanth K            24BCS324
  Venkatesh Prasanth A  24BCS307
  Surjith A             24BCS289
  Visakan T             24BCS313

----------------------------------------------------------------
  REQUIREMENTS
----------------------------------------------------------------
  - Java JDK 11 or later
  - MySQL 8.x
  - MySQL Connector/J  (mysql-connector-j-8.x.jar)
    Download: https://dev.mysql.com/downloads/connector/j/

----------------------------------------------------------------
  SETUP (Step by Step)
----------------------------------------------------------------

STEP 1 — Create the database
  Open MySQL Workbench or terminal and run:
    mysql -u root -p < database.sql

STEP 2 — Update DB credentials
  Open backend/DB.java and change:
    private static final String PASSWORD = "your_password";
  to your actual MySQL root password.

STEP 3 — Compile the Java files
  Place mysql-connector-j-8.x.jar in the backend/ folder.
  Then from the backend/ directory, run:

    Windows:
      javac -cp .;mysql-connector-j-8.x.jar DB.java Server.java

    Mac / Linux:
      javac -cp .:mysql-connector-j-8.x.jar DB.java Server.java

STEP 4 — Run the server
  From the CRIME-DATA-MANAGEMENT/ root folder (important!), run:

    Windows:
      java -cp backend;backend/mysql-connector-j-8.x.jar Server

    

  You should see:
    ==============================================
      Crime Data Management Server started!
      Open: http://localhost:8080
    ==============================================

STEP 5 — Open in Chrome
  Go to: http://localhost:8080

----------------------------------------------------------------
  FEATURES
----------------------------------------------------------------
  + Add Case     — Register new crime with type, location,
                   description, and status (dropdown)
  + View Cases   — See all records in a table with status badges
                   Red=Open, Orange=Under Investigation, Green=Closed
  + Update Case  — Load any case by ID and edit all fields
  + Delete Case  — Delete with confirmation popup
  + Edit / Del buttons in the View table for quick access

----------------------------------------------------------------
  PROJECT STRUCTURE
----------------------------------------------------------------
  CRIME-DATA-MANAGEMENT/
  ├── backend/
  │   ├── Server.java         Java HTTP server (port 8080)
  │   └── DB.java             JDBC database operations
  ├── frontend/
  │   ├── index.html          Main UI (tabbed layout)
  │   ├── script.js           CRUD logic & API calls
  │   └── style.css           Dark professional theme
  ├── database.sql            DB + table creation script
  └── README.txt              This file

----------------------------------------------------------------
  API ENDPOINTS
----------------------------------------------------------------
  GET    /api/crimes           — Get all cases
  GET    /api/crimes?id=N      — Get one case by ID
  POST   /api/crimes           — Add new case
  PUT    /api/crimes           — Update existing case
  DELETE /api/crimes?id=N      — Delete case by ID

================================================================
