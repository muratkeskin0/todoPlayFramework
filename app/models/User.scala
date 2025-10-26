package models

import java.time.Instant

case class User(
  id: Option[Long] = None,
  email: String,
  password: String, // Encrypted password
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  role: UserRole = UserRole.Basic,
  isActive: Boolean = true, // Back to true by default
  createdAt: Instant = Instant.now(),
  updatedAt: Option[Instant] = None
)
