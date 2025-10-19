package exceptions

// Custom domain exceptions
sealed trait DomainException extends Exception {
  def message: String
  override def getMessage: String = message
}

case class TodoNotFoundException(id: Long) extends DomainException {
  override val message: String = s"Todo with id $id not found"
}

case class ValidationException(message: String) extends DomainException

