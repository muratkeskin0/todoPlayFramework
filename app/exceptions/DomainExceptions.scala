package exceptions

// Custom domain exceptions
sealed trait DomainException extends Throwable {
  def message: String
  override def getMessage: String = message
}

case class TodoNotFoundException(id: Long) extends DomainException {
  override val message: String = s"Todo with id $id not found"
}

case class UserNotFoundException(id: Long) extends DomainException {
  override val message: String = s"User with id $id not found"
}

case class UserEmailNotFoundException(email: String) extends DomainException {
  override val message: String = s"User with email $email not found"
}

case class ValidationException(message: String) extends DomainException

