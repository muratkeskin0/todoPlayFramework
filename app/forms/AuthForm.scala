package forms

import play.api.data.Forms._
import play.api.data._

/**
 * Authentication form data and validation
 */
object AuthForm {
  
  // Login form data
  case class LoginData(
    email: String,
    password: String
  )

  // Register form data
  case class RegisterData(
    email: String,
    password: String,
    confirmPassword: String,
    firstName: Option[String],
    lastName: Option[String]
  )

  // Login form
  val loginForm: Form[LoginData] = Form(
    mapping(
      "email" -> email
        .verifying("Email is required", _.nonEmpty)
        .verifying("Email format is invalid", _.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$")),
      "password" -> text
        .verifying("Password is required", _.nonEmpty)
    )(LoginData.apply)(LoginData.unapply)
  )

  // Register form
  val registerForm: Form[RegisterData] = Form(
    mapping(
      "email" -> email
        .verifying("Email is required", _.nonEmpty)
        .verifying("Email format is invalid", _.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$")),
      "password" -> text
        .verifying("Password is required", _.nonEmpty)
        .verifying("Password must be at least 6 characters", _.length >= 6),
      "confirmPassword" -> text
        .verifying("Confirm password is required", _.nonEmpty),
      "firstName" -> optional(text(maxLength = 100)),
      "lastName" -> optional(text(maxLength = 100))
    )(RegisterData.apply)(RegisterData.unapply)
      .verifying("Passwords do not match", data => data.password == data.confirmPassword)
  )
}
