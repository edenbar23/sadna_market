> **Market** **System** **-E-Commerce** **Platform**
>
> The system uses Configuration and Initial State files to set up the
> service. Their syntax is specified in this file.
>
> **Configuration** **Files**
>
> The system uses Spring Boot properties files for configuration. The
> configuration files are saved in src/main/resources. The configuration
> file for the main operational service is called
> application.properties.
>
> The configuration files use standard Spring Boot properties format,
> with fields relating to different aspects of the system's operations.
>
> The different configuration fields are as follows:
>
> • system.init.enabled - enables or disables system initialization. Set to
> true to enable initialization. This field is **MANDATORY** for
> initialization to occur.
>
> • system.init.admin.username - the username of the System Administrator.
> This field is **MANDATORY** when initialization is enabled. Default is
> admin.
>
> • system.init.admin.password - the password of the System Administrator.
> This field is **MANDATORY** when initialization is enabled. Must meet
> strength requirements.
>
> • system.init.admin.email - the email of the System Administrator. This
> field is **MANDATORY** when initialization is enabled.
>
> • system.init.initial-state-file - the path to the Initial State file.
> If the field exists, the system will initialize with the initial state
> described in the file. Otherwise, the system will initialize without
> an initial state.
>
> • system.init.reset.enabled - if the value of this field is true, the
> system will clear all repositories on initialization. This is useful
> to be able to run an initial state without worrying about collisions
> with existing data. If the field is missing it will be false by
> default.
>
> • spring.datasource.url - the URL of the database. This field
> is **MANDATORY** as the system cannot operate without a database.
>
> • spring.datasource.username-database username.
>
> • spring.datasource.password-database password.
>
> **Examples**
>
> \# Enable initialization with reset
>
> system.init.enabled=true
>
> system.init.reset.enabled=true
>
> system.init.initial-state-file=init-state.txt
>
> \# System Administrator
>
> system.init.admin.username=admin
>
> system.init.admin.password=AdminPass123!
>
> system.init.admin.email=admin@market.com
>
> system.init.admin.first-name=System
>
> system.init.admin.last-name=Administrator
>
> \# Database Configuration
>
> spring.datasource.url=jdbc:h2:mem:marketdb
>
> spring.datasource.driver-class-name=org.h2.Driver
>
> spring.datasource.username=sa
>
> spring.datasource.password=password
>
> \# Profile
>
> spring.profiles.active=dev
>
> The above configuration file clears the system and starts it with the
> initial state described in the file init-state.txt. It uses an
> H2in-memory database and creates a system administrator called admin.
>
> \# Enable initialization without reset
>
> system.init.enabled=true
>
> system.init.initial-state-file=store-setup.txt
>
> \# System Administrator
>
> system.init.admin.username=sysadmin
>
> system.init.admin.password=SecurePass123!
>
> system.init.admin.email=sysadmin@company.com
>
> \# Database Configuration
>
> spring.datasource.url=jdbc:mysql://localhost:3306/marketdb
>
> spring.datasource.username=marketuser
>
> spring.datasource.password=marketpass
>
> The above configuration uses a MySQL database, creates a system
> administrator called sysadmin, and initializes
> from store-setup.txt without clearing existing data.
>
> \# Disable initialization
>
> system.init.enabled=false
>
> \# Database only
>
> spring.datasource.url=jdbc:postgresql://localhost:5432/marketdb
>
> spring.datasource.username=postgres
>
> spring.datasource.password=postgres
>
> The above configuration disables initialization and uses an existing
> PostgreSQL database with existing data.
>
> **Initial** **State** **Files**
>
> The Initial State files describe the initial state used for the
> initialization of the system. The files can have any name and be
> located anywhere, as long as they're referenced correctly by the
> configuration file.
>
> An initial state is a collection of service commands performed in a
> given order at system initialization.
>
> An Initial State file is a text format file, with one command per
> line. Each command has the following format:
>
> • command-name(param1, param2, param3, ...)-The name of the command
> followed by parameters in parentheses, separated by commas.
>
> • Parameters can be enclosed in \*asterisks\* if they contain spaces or
> special characters.
>
> • Lines starting with#or//are comments and are ignored.
>
> • Empty lines are ignored.
>
> • Commands can end with an optional semicolon ;
>
> **Supported** **Commands**
>
> • register(username, password, email, firstName, lastName)-Registera
> new user
>
> • login(username, password)-Login a user (required before performing
> user actions)
>
> • open-store(username, storeName, description, address, email,
> phone)-Create a new store
>
> • add-product(username, storeName, productName, category, description,
> price, quantity)-Add a product to a store
>
> • appoint-manager(appointerUsername, storeName, managerUsername,
> permissions)-Appoint a store manager with specific permissions
>
> • appoint-owner(appointerUsername, storeName, ownerUsername)-Appoint a
> store owner
>
> • logout(username)-Logout a user
>
> **Parameter** **Resolving**
>
> All parameters are resolved at runtime. User sessions are
> automatically tracked during initialization, so users remain logged in
> between commands until explicitly logged out.
>
> Store names are resolved to store IDs automatically -you reference
> stores by their name as created in the open-store command.
>
> Permissions for managers are specified as comma-separated values from
> the following list:
>
> • MANAGE_INVENTORY, ADD_PRODUCT, REMOVE_PRODUCT, UPDATE_PRODUCT
>
> • MANAGE_PURCHASE_POLICY, MANAGE_DISCOUNT_POLICY
>
> • RESPOND_TO_USER_INQUIRIES, VIEW_STORE_PURCHASE_HISTORY
>
> **Examples**
>
> \# Basic store setup
>
> register(john_doe, Password123!, john@example.com, John, Doe);
>
> register(jane_smith, SecurePass456!, jane@example.com, Jane, Smith);
>
> login(john_doe, Password123!);
>
> open-store(john_doe, \*Electronics Hub\*, \*Best electronics in
> town\*, \*123 Tech Street\*, \*contact@electronichub.com\*,
> \*555-0123\*);
>
> add-product(john_doe,\*Electronics Hub\*, \*iPhone 15\*,
> \*Smartphones\*, \*Latest iPhone model\*, 999.99,50);
>
> add-product(john_doe,\*Electronics Hub\*, \*MacBook Pro\*,
> \*Laptops\*, \*Professional laptop\*, 2499.99, 20);
>
> login(jane_smith, SecurePass456!);
>
> appoint-manager(john_doe, \*Electronics Hub\*, jane_smith,
> MANAGE_INVENTORY,ADD_PRODUCT,UPDATE_PRODUCT);
>
> logout(john_doe);
>
> logout(jane_smith);
>
> In this example, we register two users, login john_doe, create a store
> called Electronics Hub, add products to it, then appoint jane_smith as
> a manager with inventory management permissions.
>
> \# Multi-store enterprise setup
>
> register(ceo_admin, CEOPass123!, ceo@enterprise.com, Alice, Johnson);
>
> register(store_manager, Manager123!, manager@enterprise.com,
> Bob,Brown);
>
> login(ceo_admin, CEOPass123!);
>
> open-store(ceo_admin, \*Main Store\*, \*Flagship store\*, \*100 Main
> St\*, \*main@enterprise.com\*, \*555-1000\*);
>
> open-store(ceo_admin, \*Outlet Store\*, \*Discount store\*, \*200
> Outlet Rd\*, \*outlet@enterprise.com\*, \*555-2000\*);
>
> add-product(ceo_admin, \*Main Store\*, \*Premium Laptop\*,
> \*Computers\*, \*High-end laptop\*, 1899.99,10);
>
> add-product(ceo_admin, \*Outlet Store\*, \*Budget Laptop\*,
> \*Computers\*, \*Affordable laptop\*, 599.99, 25);
>
> login(store_manager, Manager123!);
>
> appoint-owner(ceo_admin, \*Outlet Store\*, store_manager);
>
> logout(ceo_admin);
>
> logout(store_manager);
>
> In this example, we create an enterprise setup with a CEO who creates
> two stores, adds products to both, and then appoints a store manager
> as owner of the outlet store.
