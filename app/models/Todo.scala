package models

import java.time.Instant

case class Todo(
  id: Option[Long] = None,
  title: String,
  description: Option[String] = None,
  completed: Boolean = false,
  userId: Option[Long] = None, // Foreign key to users table
  createdAt: Instant = Instant.now()
)

