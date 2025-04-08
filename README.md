# Abur
- This project is a clone of the [Steam platform](https://store.steampowered.com/), built to replicate its core features and functionality.

## Features

This project replicates the core features of the Steam platform with the following functionalities:

### General Features:
- User Authentication/Registration: New users can register and authenticate using JWT.
- Two Types of Users:
    - Customer: Regular users who can browse and purchase games.
    - Developer: Users who can manage their games and DLCs.
- Store: A store section where all available games and DLCs are displayed.

### Customer Features:
- CRUD for Customer Details: Customers can view, update, and manage their personal information.
- Wishlist: Customers can create and manage their personal wishlists of games and DLCs.
- Shopping Cart: Customers can add items to their cart for later purchase.
- Game Purchases: Customers can purchase games and DLCs from the store.
- Library: Each customer has their own personal library containing all the games they have purchased.

### Developer Features:
- CRUD for Games/DLC: Developers can create, read, update, and delete their games and DLCs.
- CRUD for Developer Details: Developers can manage their personal information on the platform.
- Stock Management: Developers can change the available stock for each of their games.

## Architecture & Structure
### Application
- <kbd>[src/main/annotation](src/main/java/ro/unibuc/hello/annotation) - DeveloperOnly and CustomerOnly annotations
- <kbd>[src/main/aspect](src/main/java/ro/unibuc/hello/aspect) - The aspect handler for the annotations
- <kbd>[src/main/config](src/main/java/ro/unibuc/hello/config) - Global app configurations
- <kbd>[src/main/controller](src/main/java/ro/unibuc/hello/controller) - Controllers for handling the endpoints
- <kbd>[src/main/data/entity](src/main/java/ro/unibuc/hello/data/entity) - MongoDB Entities
- <kbd>[src/main/data/repository](src/main/java/ro/unibuc/hello/data/repository) - MongoDB Repositories
- <kbd>[src/main/dto](src/main/java/ro/unibuc/hello/dto) - DTOs
- <kbd>[src/main/exception](src/main/java/ro/unibuc/hello/exception) - Several custom exceptions and their handler
- <kbd>[src/main/security](src/main/java/ro/unibuc/hello/security) - Authentication and JWT
- <kbd>[src/main/service](src/main/java/ro/unibuc/hello/service) - Controllers' business logic
- <kbd>[src/main/utils](src/main/java/ro/unibuc/hello/utils) - Utility classes
- <kbd>[src/main/Application](src/main/java/ro/unibuc/hello/Application.java) - Entrypoint

### Testing
- <kbd>[bruno](bruno)</kbd> - For testing API endpoints
- <kbd>[src/test/controller](src/test/java/ro/unibuc/hello/controller)</kbd> - Controller tests
- <kbd>[src/test/e2e](src/test/java/ro/unibuc/hello/e2e)</kbd> - E2E tests
- <kbd>[src/test/service](src/test/java/ro/unibuc/hello/service)</kbd> - Service tests

### Docker compose for Jenkins
- If docker-compose doesn't work, do the following
  - Download the [latest version](https://github.com/docker/compose)
  - Move the downloaded file to <kbd>$HOME/.docker/cli-plugins</kbd> as <kbd>docker-compose</kbd>
  - Create the symlink with ```sh sudo ln -s $HOME/.docker/cli-plugins/docker-compose /usr/local/bin/docker-compose```
  - Both paths are mounted in the jenkins container in <kbd>[docker-compose.yaml](docker-compose.yml)</kbd>