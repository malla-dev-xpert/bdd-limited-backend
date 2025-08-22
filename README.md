# BBD Limited Backend

## 📋 Description

BBD Limited Backend est une application Spring Boot qui gère un système de logistique et de commerce international. L'application permet de gérer les conteneurs, les packages, les achats, les versements, les partenaires, et bien d'autres entités liées au commerce international.

## 🚀 Technologies Utilisées

- **Java 17**
- **Spring Boot 3.4.3**
- **Spring Security** avec JWT
- **Spring Data JPA**
- **MySQL 8.0**
- **Maven**
- **Lombok**
- **Thymeleaf** (pour les templates email)
- **Spring Mail** (pour l'envoi d'emails)

## 📁 Structure du Projet

```
src/main/java/com/xpertpro/bbd_project/
├── BbdProjectApplication.java     # Classe principale de l'application
├── controllers/                   # Contrôleurs REST API
│   ├── UserController.java
│   ├── PackageController.java
│   ├── ContainersController.java
│   ├── AchatController.java
│   ├── VersementController.java
│   └── ...
├── entity/                       # Entités JPA
│   ├── UserEntity.java
│   ├── Containers.java
│   ├── Packages.java
│   ├── Partners.java
│   └── ...
├── repository/                   # Repositories Spring Data
├── services/                     # Services métier
├── dto/                         # Data Transfer Objects
├── dtoMapper/                   # Mappers DTO
├── config/                      # Configuration Spring
│   ├── SecurityConfig.java
│   ├── JwtUtil.java
│   └── JwtFilter.java
├── enums/                       # Énumérations
└── logs/                        # Gestion des logs
```

## 🛠️ Prérequis

Avant de démarrer le projet, assurez-vous d'avoir installé :

- **Java 17** ou supérieur
- **Maven 3.6** ou supérieur
- **MySQL 8.0** ou supérieur
- **Git**

## ⚙️ Configuration

### 1. Base de Données

Créez une base de données MySQL nommée `db_bdd_limited` :

```sql
CREATE DATABASE db_bdd_limited CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configuration de l'Application

Le fichier `src/main/resources/application.properties` contient la configuration suivante :

```properties
# Base de données
spring.datasource.url=jdbc:mysql://localhost:3306/db_bdd_limited?useSSL=false&serverTimezone=UTC
spring.datasource.username=your-database-username
spring.datasource.password=your-database-password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Serveur
server.port=8080
server.address=0.0.0.0

# JWT
jwt.secret=your-jwt-secret

# Email (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-emai-application@gmail.com
spring.mail.password=your-email-application-password
```

⚠️ **Important** : Modifiez les paramètres de base de données et d'email selon votre configuration locale.

## 🚀 Installation et Démarrage

### 1. Cloner le Projet

```bash
git clone <url-du-repo>
cd bbd-limited-backend
```

### 2. Compiler le Projet

```bash
mvn clean compile
```

### 3. Démarrer l'Application

```bash
mvn spring-boot:run
```

### 4. Vérifier le Démarrage

L'application sera accessible à l'adresse : `http://localhost:8080`

## 👤 Compte Administrateur par Défaut

L'application crée automatiquement un compte administrateur lors du premier démarrage :

- **Email** : `admin@bbdproject.com`
- **Mot de passe** : `admin123`
- **Nom d'utilisateur** : `admin`

⚠️ **Sécurité** : Changez immédiatement le mot de passe après la première connexion !

## 🔐 Authentification

L'application utilise JWT (JSON Web Tokens) pour l'authentification. Les endpoints protégés nécessitent un token JWT dans l'en-tête :

```
Authorization: Bearer <votre-token-jwt>
```

## 📦 Build

Pour créer un fichier JAR exécutable :

```bash
mvn clean package
```

Le fichier JAR sera généré dans le dossier `target/`.

## 🔧 Développement

### Variables d'Environnement Recommandées

Pour la production, utilisez des variables d'environnement :

```bash
export DB_URL=jdbc:mysql://localhost:3306/db_bdd_limited
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret
export MAIL_USERNAME=your_email
export MAIL_PASSWORD=your_email_password
```

### Mode Développement

Pour le développement, vous pouvez utiliser le profil `dev` :

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Logs

Les logs de l'application sont affichés dans la console. Pour plus de détails, configurez le logging dans `application.properties`.