package actors

import org.apache.pekko.actor.{Actor, ActorLogging, Props}
import jakarta.inject._
import play.api.libs.mailer.{Email, MailerClient}
import play.api.Configuration

object EmailActor {
  // Actor messages
  case class SendEmail(
    to: Seq[String],
    subject: String,
    bodyText: Option[String] = None,
    bodyHtml: Option[String] = None,
    cc: Seq[String] = Seq.empty,
    bcc: Seq[String] = Seq.empty,
    from: Option[String] = None
  )
  
  case class EmailSent(messageId: String, to: Seq[String])
  case class EmailFailed(error: String, to: Seq[String])
  
  def props(mailerClient: MailerClient, configuration: Configuration): Props = 
    Props(new EmailActor(mailerClient, configuration))
}

class EmailActor @Inject()(
  mailerClient: MailerClient,
  configuration: Configuration
) extends Actor with ActorLogging {
  
  import EmailActor._
  
  private val defaultFrom = configuration.get[String]("mail.from")
  private val defaultFromName = configuration.get[String]("mail.fromName")

  override def receive: Receive = {
    case SendEmail(to, subject, bodyText, bodyHtml, cc, bcc, from) =>
      try {
        val email = Email(
          subject = subject,
          from = from.getOrElse(s"$defaultFromName <$defaultFrom>"),
          to = to,
          cc = cc,
          bcc = bcc,
          bodyText = bodyText,
          bodyHtml = bodyHtml
        )
        
        val messageId = mailerClient.send(email)
        log.info(s"[EmailActor] Email sent successfully. MessageId: $messageId, To: ${to.mkString(", ")}")
        
        // Notify sender of success
        sender() ! EmailSent(messageId, to)
        
      } catch {
        case e: Exception =>
          val errorMsg = s"Failed to send email to: ${to.mkString(", ")} - ${e.getMessage}"
          log.error(e, s"[EmailActor] $errorMsg")
          
          // Notify sender of failure
          sender() ! EmailFailed(errorMsg, to)
      }
  }
}


