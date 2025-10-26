package models

case class EmailMessage(
  to: String,
  subject: String,
  bodyText: Option[String] = None,
  bodyHtml: Option[String] = None,
  from: Option[String] = None,
  fromName: Option[String] = None
)




