package actors

import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import play.api.libs.mailer.MailerClient
import play.api.Configuration
import jakarta.inject._
import models.{EmailMessage, EmailResult}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@Singleton
class EmailActorManager @Inject()(
  actorSystem: ActorSystem,
  mailerClient: MailerClient,
  configuration: Configuration
)(implicit ec: ExecutionContext) {
  
  import EmailActor._
  
  implicit val timeout: Timeout = Timeout(30.seconds)
  
  // Create the EmailActor
  private val emailActor: ActorRef = actorSystem.actorOf(
    EmailActor.props(mailerClient, configuration),
    "email-actor"
  )
  
  /**
   * Send email using the EmailActor
   */
  def sendEmail(emailMessage: EmailMessage): Future[EmailResult] = {
    val actorMessage = SendEmail(
      to = Seq(emailMessage.to),
      subject = emailMessage.subject,
      bodyText = emailMessage.bodyText,
      bodyHtml = emailMessage.bodyHtml,
      from = emailMessage.from
    )
    
    (emailActor ? actorMessage).map {
      case EmailSent(messageId, to) =>
        EmailResult(
          messageId = messageId,
          success = true
        )
      case EmailFailed(error, to) =>
        EmailResult(
          messageId = "",
          success = false,
          errorMessage = Some(error)
        )
      case other =>
        EmailResult(
          messageId = "",
          success = false,
          errorMessage = Some(s"Unexpected response from EmailActor: $other")
        )
    }.recover {
      case ex: Exception =>
        EmailResult(
          messageId = "",
          success = false,
          errorMessage = Some(s"Actor communication failed: ${ex.getMessage}")
        )
    }
  }
  
  /**
   * Send email with fire-and-forget (no response expected)
   */
  def sendEmailFireAndForget(emailMessage: EmailMessage): Unit = {
    val actorMessage = SendEmail(
      to = Seq(emailMessage.to),
      subject = emailMessage.subject,
      bodyText = emailMessage.bodyText,
      bodyHtml = emailMessage.bodyHtml,
      from = emailMessage.from
    )
    
    emailActor ! actorMessage
  }
}


