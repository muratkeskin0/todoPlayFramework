package migrations

import org.mindrot.jbcrypt.BCrypt

object HashPassword {
  def main(args: Array[String]): Unit = {
    val password = "admin123"
    val hashed = BCrypt.hashpw(password, BCrypt.gensalt())
    println(s"Password: $password")
    println(s"Hashed: $hashed")
  }
}

