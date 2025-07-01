# Market System - E-Commerce Platform

A modern Spring Boot-based e-commerce platform with advanced state-based initialization system and interactive startup capabilities.

## 🚀 System Overview

The Market System uses a sophisticated **state-based initialization system** that provides multiple ways to configure and start your application:

- **Interactive Startup Menu** - Visual menu system for easy configuration
- **YAML-based Configuration** - Modern component-based setup
- **Multiple Initialization Modes** - Flexible startup options
- **State Tracking** - Resume capability and rollback on failure
- **Component Dependencies** - Ordered initialization with dependency management

## 📋 Configuration Files

The system uses Spring Boot properties files for configuration. Configuration files are located in `src/main/resources/`:

- `application.properties` - Main configuration
- `application-dev.properties` - Development profile  
- `application-test.properties` - Test profile
- `system-config.yml` - System initialization configuration

## ⚙️ Configuration Properties

### Core System Properties

| Property | Description | Default | Required |
|----------|-------------|---------|----------|
| `system.startup.menu.enabled` | Enable/disable interactive startup menu | `true` | No |
| `system.init.mode` | Initialization mode (see modes below) | `selective` | No |
| `system.init.interactive` | Enable interactive mode prompts | `false` | No |
| `system.init.config-file` | Path to YAML configuration file | `src/main/resources/system-config.yml` | No |

### Database Configuration

| Property | Description | Required |
|----------|-------------|----------|
| `spring.datasource.url` | Database connection URL | **Yes** |
| `spring.datasource.username` | Database username | **Yes** |
| `spring.datasource.password` | Database password | **Yes** |
| `spring.datasource.driver-class-name` | Database driver | **Yes** |

### Additional Properties

- `spring.jpa.hibernate.ddl-auto` - Database schema management
- `external.api.enabled` - Enable/disable external API integration
- `external.api.url` - External API endpoint URL
- `market.jwt.expiration` - JWT token expiration time
- `market.jwt.secret` - JWT signing secret

## 🎛️ Interactive Startup Menu

When `system.startup.menu.enabled=true`, the system displays an interactive menu on startup:

```
🏪 SADNA MARKET - STARTUP MENU
============================================================

📋 Choose startup option:
[1] 🔄 Fresh Start (Clear DB + Initialize)
[2] 📊 Load from Database  
[3] 🔍 Check Current State
[4] ⚡ Smart Init (Add missing only)
[5] 🚪 Exit

Your choice (1-5):
```

### Menu Options

- **Fresh Start**: Clears all data and reinitializes from scratch
- **Load from Database**: Uses existing database without initialization
- **Check Current State**: Analyzes what components are already set up
- **Smart Init**: Only initializes missing components (selective mode)
- **Exit**: Stops the application

## 🔧 Initialization Modes

### Available Modes

| Mode | Description | Use Case |
|------|-------------|----------|
| `check_only` | Only check current state, no changes | System diagnostics |
| `selective` | Initialize only missing components | **Default**, safe startup |
| `force_full` | Force reinitialize all components | Complete refresh |
| `reset_and_init` | Clear all data and start fresh | Clean slate setup |

### Setting Modes

**Via Properties:**
```properties
system.init.mode=selective
```

**Via Interactive Menu:**
The startup menu allows you to choose the mode dynamically.

**Via Environment Variables:**
```bash
export SYSTEM_INIT_MODE=check_only
```

## 📄 YAML Configuration Format

The system uses `system-config.yml` for component-based initialization:

```yaml
initialization:
  mode: "selective"
  on_failure: "stop"
  rollback_on_error: true
  
  components:
    admin_setup:
      enabled: true
      force: false
      config:
        username: "u1"
        password: "Password123!"
        email: "u1@market.com"
        firstName: "System"
        lastName: "Administrator"
    
    user_registration:
      enabled: true
      force: false
      depends_on: ["admin_setup"]
      config:
        users:
          - username: "u2"
            password: "Password123!"
            email: "u2@market.com"
            firstName: "User"
            lastName: "Two"
```

### Component Structure

Each component can have:

- `enabled`: Whether to run this component
- `force`: Force execution even if already completed
- `depends_on`: List of component dependencies
- `config`: Component-specific configuration

### Failure Handling

- `on_failure`: `stop`, `continue`, or `rollback`
- `rollback_on_error`: Automatically rollback changes on any failure

## 📚 Configuration Examples

### Example 1: Development Setup

```properties
# application-dev.properties
spring.profiles.active=dev
system.startup.menu.enabled=true
system.init.mode=selective

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/sadna_market_db
spring.datasource.username=sadna_market_db_owner
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# External API
external.api.enabled=true
external.api.url=https://api.example.com/
```

### Example 2: Production Setup

```properties
# application.properties
spring.profiles.active=prod
system.startup.menu.enabled=false
system.init.mode=check_only

# Database
spring.datasource.url=jdbc:postgresql://prod-server:5432/market_prod
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Example 3: Test Environment

```properties
# application-test.properties
spring.profiles.active=test

# H2 In-Memory Database for tests
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Disable external APIs for testing
external.api.enabled=false
```

### Example 4: Fresh Installation

```properties
# Complete reset and setup
system.startup.menu.enabled=true
system.init.mode=reset_and_init
system.init.interactive=true

# Database
spring.datasource.url=jdbc:h2:mem:marketdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

## 🔄 Component-Based Initialization

### Available Components

1. **admin_setup** - Creates system administrator
2. **user_registration** - Creates regular users  
3. **user_login** - Handles user authentication
4. **store_creation** - Sets up stores
5. **product_management** - Manages product catalog
6. **permissions_setup** - Configures user permissions

### Dependency Management

Components can depend on others:

```yaml
store_creation:
  enabled: true
  depends_on: ["admin_setup", "user_registration"]
  config:
    # Store configuration
```

The system automatically handles execution order based on dependencies.

## 🎯 Quick Start Guide

### 1. First Time Setup

1. Configure your database in `application-dev.properties`
2. Start the application
3. Choose "Fresh Start" from the interactive menu
4. Confirm data deletion when prompted
5. System will initialize with default configuration

### 2. Regular Development

1. Set `system.init.mode=selective` 
2. Use "Smart Init" to add only missing components
3. System preserves existing data

### 3. Production Deployment

1. Set `system.startup.menu.enabled=false`
2. Set `system.init.mode=check_only`
3. System will verify configuration without changes

## ⚡ Advanced Features

### State Tracking

The system tracks initialization state and can resume interrupted processes.

### Rollback Capability

On failure, the system can automatically rollback changes:

```yaml
initialization:
  rollback_on_error: true
  on_failure: "rollback"
```

### Interactive Mode

Enable interactive prompts for dynamic configuration:

```properties
system.init.interactive=true
```

### Frontend Integration

The startup menu can automatically launch the frontend:

```
🌐 Open frontend in browser? (Y/n): Y
```

## 🔍 Troubleshooting

### Common Issues

**Issue**: "DatabaseCleaner bean not found"
- **Solution**: This is normal in test profile. Database operations are disabled for in-memory testing.

**Issue**: "Interactive menu not showing"
- **Solution**: Check `system.startup.menu.enabled=true` and ensure you're not in test profile.

**Issue**: "Initialization hangs"
- **Solution**: Check component dependencies for circular references in `system-config.yml`.

### Debug Mode

Enable detailed logging:

```properties
logging.level.com.sadna_market=DEBUG
logging.level.com.sadna_market.market.InfrastructureLayer.Initialization=TRACE
```

## 📞 Support

For issues related to:
- Configuration: Check your properties files
- Database: Verify connection settings
- Initialization: Review component dependencies in YAML
- Interactive menu: Ensure correct profile and properties

The system provides detailed error messages and suggestions for most common configuration issues.