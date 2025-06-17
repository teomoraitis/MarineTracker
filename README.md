<h1>
    <img src="https://github.com/sdi2000150/MarineTracker/blob/main/1st%20Deliverable%20-%20SRS%20&%20UI%20Wireframes/UI_Wireframes/Logo.png" alt="Logo" width="35" style="vertical-align: middle;"/> MarineTracker
</h1>
<strong>Full-stack web platform</strong> featuring real-time <strong>vessel monitoring</strong> through <strong>AIS</strong> data stream processing. <br>
Built with <strong>Spring Boot</strong> and <strong>React</strong>. Using Apache <strong>Kafka</strong> and <strong>PostgreSQL</strong>.


---

## The Team 
**Team name:** "Ομάδα Χρηστών 4" <br>
**Number of contributors:** 6
- sdi2000105 - [Christos Kypraios (ChristosKypraios)](https://github.com/ChristosKypraios)
- sdi1700254 - [Pierro Zachareas (plerros)](https://github.com/plerros)
- sdi2000150 - [Theodoros Moraitis (sdi2000150)](https://github.com/sdi2000150)
- sdi2000006 - [Evgenios Paraskevas Mavroudakos (EugeneM02)](https://github.com/EugeneM02)
- sdi1900048 - [Theodoros Dimakopoulos (TheodoreAlenas)](https://github.com/TheodoreAlenas)


---

## Project Deliverables

The project was developed in three phases:

1. **1st Deliverable – SRS & UI Wireframes**  
   📁 [`1st Deliverable - SRS & UI Wireframes`](./1st%20Deliverable%20-%20SRS%20&%20UI%20Wireframes)  
   Includes:  
   - Software Requirements Specification (SRS)  
   - UI mockup wireframes for the web application  

2. **2nd Deliverable – System Modeling & UML**  
   📁 [`2nd Deliverable - System Modeling & UML`](./2nd%20Deliverable%20-%20System%20Modeling%20&%20UML)  
   Includes a presentation of:  
   - System modeling of the implementation
   - UML and architecture diagrams 

3. **Final Deliverable – Full-Stack Implementation**  
   📁 [`Backend-SpringBoot`](./Backend-SpringBoot)  
   📁 [`Frontend-React`](./Frontend-React)  
   📁 [`Kafka`](./Kafka)  
   Complete implementation of the full-stack platform, integrating Backend, Frontend, and real-time Kafka-based data ingestion.

---

## Technical Details

<table>
  <tr>
    <th align="left">🌱 Backend</th>
    <th align="left">⚛️ Frontend</th>
    <th align="left">🦑 Data Stream</th>
  </tr>
  <tr>
    <td>
      <ul>
        <li>Spring Boot v...</li>
        <li>Java 17 (JDK ...)</li>
        <li>Maven v...</li>
        <li>RESTful API</li>
        <li>WebSockets</li>
        <li>SSL/TLS (Self-Signed Certificate)</li>
        <li>JWT Auth</li>
        <li>PostgreSQL v... via Docker...</li>
        <li>Testing & Automation ...</li>
        <li>...</li>
      </ul>
    </td>
    <td>
      <ul>
        <li>React v...</li>
        <li>JavaScript v...</li>
        <li>HTML... / CSS...</li>
        <li>Leaflet.js ...</li>
        <li>OpenStreetMap API (?) ...</li>
        <li>SPA Architecture</li>
        <li>Testing & Automation ...</li>
        <li>...</li>
      </ul>
    </td>
    <td>
      <ul>
        <li>Apache Kafka v...</li>
        <li>AIS Dataset from Zenodo (6-month period)</li>
        <li>Python ... producer</li>
        <li>...</li>
      </ul>
    </td>
  </tr>
</table>

---

## The Platform
MarineTracker is a web-based platform for real-time vessel tracking using AIS (Automatic Identification System) data. 

Key features include:
- **Real-time visualization of vessels** on a map with details like location, status, type, and course.
- **Historical playback** of vessel trajectories for the past 12 hours.
- **Fleet management**, allowing users to follow specific ships and apply filters.
- **Zone of Interest** functionality, where users can define regions on the map and set movement restrictions.
- **Admin privileges** to edit static ship data.
...

### Home Page - Guest

### Home Page - Registered User

### Home Page - Admin
