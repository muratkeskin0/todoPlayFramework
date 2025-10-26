package models

case class EmailResult(
  messageId: String,
  success: Boolean,
  errorMessage: Option[String] = None
)




