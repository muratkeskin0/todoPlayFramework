package dto

// API Request/Response DTOs
object TodoDTO {

  trait TodoRequest {
    def title: String
    def description: Option[String]
    def completed: Boolean
  }

  // Create request
  case class CreateTodoRequest(title: String, description: Option[String] = None, completed: Boolean = false) extends TodoRequest

  // Update request
  case class UpdateTodoRequest(title: String, description: Option[String] = None, completed: Boolean) extends TodoRequest

  // Response to client
  case class TodoResponse(
    id: Long,
    title: String,
    description: Option[String],
    completed: Boolean,
    createdAt: String  // ISO-8601 format
  )
}

