package models

import java.time.Instant

case class Todo(
  id: Option[Long] = None,
  title: String,
  description: Option[String] = None,
  completed: Boolean = false,
  createdAt: Instant = Instant.now()
)

