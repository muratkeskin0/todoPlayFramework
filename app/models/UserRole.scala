package models

/**
 * User role enumeration
 * Defines the different types of users in the system
 */
sealed trait UserRole {
  def value: String
  def isAdmin: Boolean = this == UserRole.Admin
  def isBasic: Boolean = this == UserRole.Basic
}

object UserRole {
  case object Admin extends UserRole { val value = "admin" }
  case object Basic extends UserRole { val value = "basic" }
  
  val values: Seq[UserRole] = Seq(Admin, Basic)
  
  def fromString(role: String): Option[UserRole] = {
    role.toLowerCase match {
      case "admin" => Some(Admin)
      case "basic" => Some(Basic)
      case _ => None
    }
  }
  
  def isAdmin(role: UserRole): Boolean = role.isAdmin
  def isBasic(role: UserRole): Boolean = role.isBasic
}
