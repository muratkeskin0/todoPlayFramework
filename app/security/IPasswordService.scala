package security

/**
 * Interface for password-related security operations
 */
trait IPasswordService {
  
  /**
   * Hash a plain text password using BCrypt
   * @param plainPassword The plain text password to hash
   * @return The hashed password
   */
  def hashPassword(plainPassword: String): String
  
  /**
   * Verify a plain text password against a hashed password
   * @param plainPassword The plain text password to verify
   * @param hashedPassword The hashed password to check against
   * @return True if the password matches, false otherwise
   */
  def verifyPassword(plainPassword: String, hashedPassword: String): Boolean
  
  /**
   * Generate a random salt for password hashing
   * @return A random salt string
   */
  def generateSalt(): String
  
  /**
   * Hash password with custom salt
   * @param plainPassword The plain text password
   * @param salt The custom salt to use
   * @return The hashed password
   */
  def hashPasswordWithSalt(plainPassword: String, salt: String): String
}

