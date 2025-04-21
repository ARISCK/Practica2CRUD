# CRUD Auth App - Android + Node.js + MySQL

Este proyecto es una aplicación móvil desarrollada en Android (Kotlin) que permite realizar operaciones CRUD sobre tareas, con autenticación de usuarios y roles (Administrador y Usuario). El backend está hecho con Node.js y utiliza una base de datos MySQL.

## 📱 Características principales (Android)

- Inicio de sesión y registro de usuarios
- Envío seguro de contraseñas encriptadas
- Manejo de sesión con JWT
- Menú de navegación dinámico según el rol
- CRUD de tareas para el usuario
- Subida y visualización de foto de perfil
- Panel de administración para:
  - Ver usuarios registrados
  - Cambiar rol (admin/usuario)
  - Eliminar usuarios
  - Ver, agregar, editar y eliminar tareas de otros usuarios
  - Registrar nuevos usuarios desde el panel

## 🌐 Backend (Node.js + Express)

- API RESTful con rutas protegidas por JWT
- Encriptación de contraseñas con bcrypt
- Gestión de usuarios, roles y tareas
- Subida de imagen de perfil con multer
- Base de datos relacional en MySQL

## 🛠 Requisitos

- Android Studio (Arctic Fox o superior)
- Node.js y npm
- MySQL Server (puede usarse XAMPP)
- Git


### 1. Backend

bash

cd backend
npm install
node server.js
Asegúrate de tener tu base de datos MySQL creada (ej. crud_app) y configurada en database.js.



Android

Abrir CRUDAuthApp en Android Studio
Ejecutar en un emulador o dispositivo físico
