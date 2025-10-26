package services

import models.{EmailMessage, EmailResult}
import scala.concurrent.Future

trait IEmailService {
  
  /**
   * Send email using EmailMessage
   * @param emailMessage Email message to send
   * @return Future[EmailResult] - Completes with result when email is sent
   */
  def sendEmail(emailMessage: EmailMessage): Future[EmailResult]
  
  /**
   * Send welcome email to new user
   * @param email User's email address
   * @param firstName User's first name (optional)
   * @return Future[Unit] - Completes when email is sent
   */
  def sendWelcomeEmail(email: String, firstName: Option[String]): Future[Unit]
}
