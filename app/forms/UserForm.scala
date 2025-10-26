package forms

import play.api.data._
import play.api.data.Forms._
import models.UserRole

/**
 * User form data and validation
 */
object UserForm {
  
  case class UserData(
    email: String,
    password: String,
    firstName: Option[String],
    lastName: Option[String],
    role: UserRole,
    isActive: Boolean
  )

  val form: Form[UserData] = Form(
    mapping(
      "email" -> email
        .verifying("Email is required", _.nonEmpty)
        .verifying("Email format is invalid", _.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$")),
      "password" -> text
        .verifying("Password is required", _.nonEmpty)
        .verifying("Password must be at least 6 characters", _.length >= 6),
      "firstName" -> optional(text(maxLength = 100)),
      "lastName" -> optional(text(maxLength = 100)),
      "role" -> text
        .verifying("Role is required", _.nonEmpty)
        .transform[UserRole](
          role => UserRole.fromString(role).getOrElse(UserRole.Basic),
          role => role.toString
        ),
      "isActive" -> boolean
    )(UserData.apply)(UserData.unapply)
  )
}
