package services

import exceptions.UserNotFoundException
import security.IPasswordService
import models.User

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

/**
 * Authentication service for login and registration
 */
@Singleton
class AuthService @Inject()(
  userService: IUserService,
  emailService: IEmailService,
  passwordService: IPasswordService
)(implicit ec: ExecutionContext) extends IAuthService {

  /**
   * Authenticate user with email and password
   * @param email User email
   * @param password Plain text password
   * @return User if authentication successful, None otherwise
   */
  def authenticate(email: String, password: String): Future[Option[User]] = {
    userService.getUserByEmail(email).map { user =>
      // Check if user is active and password matches
      if (user.isActive && passwordService.verifyPassword(password, user.password)) {
        Some(user)
      } else {
        None
      }
    }.recover {
      case _: UserNotFoundException => None
      case _ => None
    }
  }

  /**
   * Register a new user
   * @param user User to register
   * @return Created user
   */
  def register(user: User): Future[User] = {
    userService.createUser(user).flatMap { createdUser =>
      // Send welcome email
      emailService.sendWelcomeEmail(
        createdUser.email,
        createdUser.firstName
      ).map(_ => createdUser)
    }
  }

  /**
   * Verify password against stored hash
   * Note: This would need access to the user's password hash
   * For now, we'll implement a basic version
   */
  def verifyPassword(plainPassword: String, hashedPassword: String): Boolean = {
    passwordService.verifyPassword(plainPassword, hashedPassword)
  }

  /**
   * Check if user exists by email
   */
  def userExists(email: String): Future[Boolean] = {
    userService.getUserByEmail(email).map(_ => true).recover {
      case _: UserNotFoundException => false
      case _ => false
    }
  }
}
