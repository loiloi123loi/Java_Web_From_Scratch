---
description: Create a new API feature (Handler, Service, Repository, Model, DTO)
---

To create a new feature, follow these steps:

1. Create the Model class in `com.polime.model`.
2. Create the DTOs in `com.polime.dto`.
3. Create the Repository in `com.polime.repository`.
4. Create the Service in `com.polime.service`.
5. Create the Handler (Controller) in `com.polime.controller`.
6. Register the route in `SocialApp.java`.

Ensure you follow the project rules:

- No Frameworks/DI/ORM/Lombok.
- JDBC with `PreparedStatement`.
- Manual Mapping from `ResultSet` to Model.
- Custom Exceptions for validation.
- Response via `BaseResponseDto`.
