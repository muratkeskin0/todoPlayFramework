package services

import scala.concurrent.Future
import models.User

/**
 * Authentication service interface
 */
trait IAuthService {
  
  /**
   * Authenticate user with email and password
   * @param email User email
   * @param password Plain text password
   * @return User if authentication successful, None otherwise
   */
  def authenticate(email: String, password: String): Future[Option[User]]
  
  /**
   * Register a new user
   * @param user User to register
   * @return Created user
   */
  def register(user: User): Future[User]
  
  /**
   * Verify password against stored hash
   * @param plainPassword Plain text password
   * @param hashedPassword Stored hash
   * @return True if password matches
   */
  def verifyPassword(plainPassword: String, hashedPassword: String): Boolean
  
  /**
   * Check if user exists by email
   * @param email User email
   * @return True if user exists
   */
  def userExists(email: String): Future[Boolean]
}
