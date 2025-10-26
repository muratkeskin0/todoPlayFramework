package services

import actors.EmailActor
import models.{EmailMessage, EmailResult}
import play.api.Configuration
import play.api.libs.mailer.MailerClient
import play.api.Logger
import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}

@Singleton
class EmailService @Inject()(
  mailerClient: MailerClient,
  configuration: Configuration,
  actorSystem: ActorSystem
)(implicit ec: ExecutionContext) extends IEmailService {

  private val logger = Logger(this.getClass)
  private val emailActor: ActorRef = actorSystem.actorOf(
    EmailActor.props(mailerClient, configuration),
    "emailActor"
  )

  def sendEmail(emailMessage: EmailMessage): Future[EmailResult] = {
    import EmailActor._
    
    implicit val timeout: Timeout = Timeout(30.seconds)
    
    logger.info(s"Sending email to: ${emailMessage.to}")
    
    (emailActor ? SendEmail(
      to = Seq(emailMessage.to),
      subject = emailMessage.subject,
      bodyText = emailMessage.bodyText,
      bodyHtml = emailMessage.bodyHtml,
      cc = Seq.empty,
      bcc = Seq.empty,
      from = emailMessage.from
    )).map {
      case EmailSent(messageId, to) =>
        logger.info(s"Email sent successfully to: ${to.mkString(", ")}, MessageId: $messageId")
        EmailResult(messageId = messageId, success = true, errorMessage = None)
      case EmailFailed(error, to) =>
        logger.error(s"Failed to send email to: ${to.mkString(", ")}. Error: $error")
        EmailResult(messageId = "", success = false, errorMessage = Some(error))
    }
  }

  def sendWelcomeEmail(email: String, firstName: Option[String]): Future[Unit] = {
    import EmailActor._
    
    implicit val timeout: Timeout = Timeout(30.seconds)
    
    logger.info(s"Sending welcome email to: $email")
    
    // Create welcome email content
    val subject = "Welcome to Todo App!"
    val displayName = firstName.getOrElse("User")
    val baseUrl = configuration.get[String]("app.baseUrl")
    val bodyText = Some(views.txt.emails.welcomeText(displayName).body)
    val bodyHtml = Some(views.html.emails.welcomeHtml(displayName, baseUrl).body)
    
    (emailActor ? SendEmail(
      to = Seq(email),
      subject = subject,
      bodyText = bodyText,
      bodyHtml = bodyHtml
    )).map {
      case EmailSent(messageId, to) =>
        logger.info(s"Welcome email sent successfully to: $email, MessageId: $messageId")
      case EmailFailed(error, to) =>
        logger.error(s"Failed to send welcome email to: $email. Error: $error")
        throw new RuntimeException(s"Failed to send welcome email: $error")
    }
  }
}
