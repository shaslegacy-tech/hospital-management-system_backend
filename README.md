# 🏥 Hospital Management System API

A production-grade REST API built with Spring Boot

## Tech Stack
- Java 21 + Spring Boot 4.0.7
- Spring Security + JWT Authentication
- PostgreSQL Database
- Spring Data JPA + Hibernate
- Lombok
- Maven

## Features
- 4 Role-based Auth (ADMIN/DOCTOR/PATIENT/RECEPTIONIST)
- Department Management
- Doctor Management
- Patient Management
- Appointment Booking System
- Medical Records & Prescriptions
- Billing & Payment System
- Admin Dashboard Stats

## API Endpoints
### Auth
- POST /api/auth/register
- POST /api/auth/login

### Departments
- GET    /api/departments
- POST   /api/departments (ADMIN)
- PUT    /api/departments/{id} (ADMIN)
- DELETE /api/departments/{id} (ADMIN)

### Doctors
- GET /api/doctors
- GET /api/doctors/available
- GET /api/doctors/department/{id}
- POST /api/doctors (ADMIN)

### Appointments
- POST /api/appointments
- GET  /api/appointments/today
- PUT  /api/appointments/{id}/status
- PUT  /api/appointments/{id}/cancel

### Medical Records
- POST /api/records (DOCTOR)
- GET  /api/records/patient/{id}/history

### Bills
- POST /api/bills (ADMIN/RECEPTIONIST)
- PUT  /api/bills/{id}/pay

### Dashboard
- GET /api/dashboard/stats (ADMIN)

## 📚 API Documentation
Once running, visit:
http://localhost:8080/swagger-ui/index.html

## 🚀 Getting Started

1. Create PostgreSQL database:
```sql
   CREATE DATABASE hmsdb;
```
2. Update `application.yml` with your DB credentials
3. Run:
```bash
   ./mvnw spring-boot:run
```
4. Base URL: `http://localhost:8080`

## 🔐 Sample Roles Flow

POST /api/auth/register → get JWT token
POST /api/auth/login → get JWT token
Use token as: Authorization: Bearer <token>

## 📊 Core Modules
| Module | Endpoints |
|---|---|
| Auth | /api/auth/** |
| Departments | /api/departments/** |
| Doctors | /api/doctors/** |
| Patients | /api/patients/** |
| Appointments | /api/appointments/** |
| Medical Records | /api/records/** |
| Prescriptions | /api/prescriptions/** |
| Billing | /api/bills/** |
| Dashboard | /api/dashboard/** |
| Files | /api/files/** |

## 🏗 Architecture Notes
- File storage uses local disk for development;
  production would use S3/Cloudinary for persistence
  across container restarts.
- Async email sending via `@Async` to avoid blocking
  API responses.
- Dynamic search implemented via JPA Criteria API
  Specifications for flexible filtering without
  combinatorial repository methods.