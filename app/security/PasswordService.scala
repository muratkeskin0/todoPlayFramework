package security

import org.mindrot.jbcrypt.BCrypt
import javax.inject.{Inject, Singleton}

/**
 * Password encryption and verification service
 * Handles all password-related security operations
 */
@Singleton
class PasswordService @Inject()() extends IPasswordService {
  
  /**
   * Hash a plain text password using BCrypt
   * @param plainPassword The plain text password to hash
   * @return The hashed password
   */
  def hashPassword(plainPassword: String): String = {
    BCrypt.hashpw(plainPassword, BCrypt.gensalt())
  }
  
  /**
   * Verify a plain text password against a hashed password
   * @param plainPassword The plain text password to verify
   * @param hashedPassword The hashed password to check against
   * @return True if the password matches, false otherwise
   */
  def verifyPassword(plainPassword: String, hashedPassword: String): Boolean = {
    BCrypt.checkpw(plainPassword, hashedPassword)
  }
  
  /**
   * Generate a random salt for password hashing
   * @return A random salt string
   */
  def generateSalt(): String = {
    BCrypt.gensalt()
  }
  
  /**
   * Hash password with custom salt
   * @param plainPassword The plain text password
   * @param salt The custom salt to use
   * @return The hashed password
   */
  def hashPasswordWithSalt(plainPassword: String, salt: String): String = {
    BCrypt.hashpw(plainPassword, salt)
  }
}
