package controllers

import models.User
import forms.AuthForm
import models.UserRole
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{IAuthService, AuthService}

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Authentication Controller
 * Handles login, register, and logout
 */
@Singleton
class AuthController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  authService: IAuthService
)(implicit ec: ExecutionContext) extends MessagesAbstractController(controllerComponents) with I18nSupport with Logging {

  /**
   * Login page
   * GET /auth/login
   */
  def loginPage() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.auth.login(AuthForm.loginForm))
  }

  /**
   * Process login
   * POST /auth/login
   */
  def login() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    AuthForm.loginForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.auth.login(formWithErrors))
        )
      },
      loginData => {
        authService.authenticate(loginData.email, loginData.password).map {
          case Some(user) =>
            // Successful login - redirect to home with user in session
            user.id match {
              case Some(userId) =>
                Redirect(routes.HomeController.index())
                  .withSession("userId" -> userId.toString, "userEmail" -> user.email)
                  .flashing("success" -> s"Welcome back, ${user.firstName.getOrElse(user.email)}!")
              case None =>
                // This shouldn't happen for existing users, but handle gracefully
                BadRequest(views.html.auth.login(
                  AuthForm.loginForm.withGlobalError("User data error. Please try again.")
                ))
            }
          case None =>
            // Authentication failed
            logger.warn(s"Login failed for email: ${loginData.email} - Invalid credentials")
            BadRequest(views.html.auth.login(
              AuthForm.loginForm.withGlobalError("Invalid email or password")
            ))
        }.recover {
          case ex: Exception =>
            logger.error(s"Login error for email: ${loginData.email}", ex)
            BadRequest(views.html.auth.login(
              AuthForm.loginForm.withGlobalError("An error occurred during login")
            ))
        }
      }
    )
  }

  /**
   * Register page
   * GET /auth/register
   */
  def registerPage() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.auth.register(AuthForm.registerForm))
  }

  /**
   * Process registration
   * POST /auth/register
   */
  def register() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    AuthForm.registerForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.auth.register(formWithErrors))
        )
      },
      registerData => {
        // Check if user already exists
        authService.userExists(registerData.email).flatMap { exists =>
          if (exists) {
            Future.successful(
              BadRequest(views.html.auth.register(
                AuthForm.registerForm.withGlobalError("Email already exists")
              ))
            )
          } else {
            // Convert form data directly to User model
            val user = models.User(
              email = registerData.email,
              password = registerData.password, // This should be hashed in a real app
              firstName = registerData.firstName,
              lastName = registerData.lastName,
              role = UserRole.Basic,
              isActive = false // Will be set to false by AuthService.register
            )

            authService.register(user).map { createdUser =>
              // Successful registration - redirect to login with welcome message
              Redirect(routes.AuthController.loginPage())
                .flashing("success" -> s"Kayıt başarılı! ${createdUser.firstName.getOrElse("Kullanıcı")}, hoş geldin emaili gönderildi. Giriş yapabilirsiniz!")
            }.recover {
              case ex: Exception =>
                logger.error("Error during registration", ex)
                BadRequest(views.html.auth.register(
                  AuthForm.registerForm.withGlobalError("Kayıt sırasında bir hata oluştu. Lütfen tekrar deneyin.")
                ))
            }
          }
        }
      }
    )
  }

  /**
   * Logout
   * GET /auth/logout
   */
  def logout() = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.AuthController.loginPage())
      .withNewSession
      .flashing("success" -> "You have been logged out successfully!")
  }
}
