package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n.I18nSupport
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging

import services.IUserService
import models.User
import forms.UserForm
import exceptions.{UserNotFoundException, ValidationException}

@Singleton
class UserController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  userService: IUserService
)(implicit ec: ExecutionContext) extends MessagesAbstractController(controllerComponents) with I18nSupport with Logging {

  /**
   * User list page
   * GET /users
   */
  def index() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in and is admin
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          // Get user info to check if admin
          userService.getUserById(userId).flatMap { currentUser =>
            if (currentUser.role.isAdmin) {
              userService.listUsers().map { users =>
                Ok(views.html.users.index(users, UserForm.form))
              }
            } else {
              Future.successful(
                Redirect(routes.HomeController.index())
                  .flashing("error" -> "Access denied. Admin privileges required.")
              )
            }
          }.recover {
            case ex: Exception =>
              logger.error("Error checking user permissions", ex)
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Session expired. Please login again.")
          }
        } catch {
          case _: NumberFormatException =>
            Future.successful(
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Invalid session. Please login again.")
            )
        }
      case None =>
        Future.successful(
          Redirect(routes.AuthController.loginPage())
            .flashing("error" -> "Please login to access user management.")
        )
    }
  }

  /**
   * Create new user
   * POST /users
   */
  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in and is admin
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          userService.getUserById(userId).flatMap { currentUser =>
            if (currentUser.role.isAdmin) {
              UserForm.form.bindFromRequest().fold(
                formWithErrors => {
                  // Validation errors
                  userService.listUsers().map { users =>
                    BadRequest(views.html.users.index(users, formWithErrors))
                  }
                },
                userData => {
                  // Convert form data directly to User model
                  val user = models.User(
                    email = userData.email,
                    password = userData.password, // This should be hashed in a real app
                    firstName = userData.firstName,
                    lastName = userData.lastName,
                    role = userData.role,
                    isActive = userData.isActive
                  )
                  
                  userService.createUser(user).map { _ =>
                    Redirect(routes.UserController.index())
                      .flashing("success" -> "User created successfully!")
                  }.recoverWith {
                    case ValidationException(msg) =>
                      userService.listUsers().map { users =>
                        BadRequest(views.html.users.index(
                          users, 
                          UserForm.form.fill(userData).withGlobalError(msg)
                        ))
                      }
                    case ex: Exception =>
                      logger.error("Error creating user", ex)
                      Future.successful(
                        Redirect(routes.UserController.index())
                          .flashing("error" -> "An error occurred while creating user")
                      )
                  }
                }
              )
            } else {
              Future.successful(
                Redirect(routes.HomeController.index())
                  .flashing("error" -> "Access denied. Admin privileges required.")
              )
            }
          }.recover {
            case ex: Exception =>
              logger.error("Error checking user permissions", ex)
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Session expired. Please login again.")
          }
        } catch {
          case _: NumberFormatException =>
            Future.successful(
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Invalid session. Please login again.")
            )
        }
      case None =>
        Future.successful(
          Redirect(routes.AuthController.loginPage())
            .flashing("error" -> "Please login to create users.")
        )
    }
  }

  /**
   * User edit page
   * GET /users/:id/edit
   */
  def edit(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in and is admin
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          userService.getUserById(userId).flatMap { currentUser =>
            if (currentUser.role.isAdmin) {
              userService.getUserById(id).map { user =>
                val formData = UserForm.UserData(
                  email = user.email,
                  password = "", // Don't show existing password
                  firstName = user.firstName,
                  lastName = user.lastName,
                  role = user.role,
                  isActive = user.isActive
                )
                Ok(views.html.users.edit(id, UserForm.form.fill(formData)))
              }.recover {
                case UserNotFoundException(_) =>
                  Redirect(routes.UserController.index())
                    .flashing("error" -> "User not found")
              }
            } else {
              Future.successful(
                Redirect(routes.HomeController.index())
                  .flashing("error" -> "Access denied. Admin privileges required.")
              )
            }
          }.recover {
            case ex: Exception =>
              logger.error("Error checking user permissions", ex)
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Session expired. Please login again.")
          }
        } catch {
          case _: NumberFormatException =>
            Future.successful(
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Invalid session. Please login again.")
            )
        }
      case None =>
        Future.successful(
          Redirect(routes.AuthController.loginPage())
            .flashing("error" -> "Please login to edit users.")
        )
    }
  }

  /**
   * Update user
   * POST /users/:id
   */
  def update(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in and is admin
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          userService.getUserById(userId).flatMap { currentUser =>
            if (currentUser.role.isAdmin) {
              UserForm.form.bindFromRequest().fold(
                formWithErrors => {
                  Future.successful(BadRequest(views.html.users.edit(id, formWithErrors)))
                },
                userData => {
                  // Convert form data directly to User model
                  val user = models.User(
                    id = Some(id),
                    email = userData.email,
                    password = userData.password, // This should be hashed in a real app
                    firstName = userData.firstName,
                    lastName = userData.lastName,
                    role = userData.role,
                    isActive = userData.isActive
                  )
                  
                  userService.updateUser(id, user).map { _ =>
                    Redirect(routes.UserController.index())
                      .flashing("success" -> "User updated successfully!")
                  }.recover {
                    case UserNotFoundException(_) =>
                      Redirect(routes.UserController.index())
                        .flashing("error" -> "User not found")
                    case ValidationException(msg) =>
                      BadRequest(views.html.users.edit(id, UserForm.form.fill(userData).withGlobalError(msg)))
                    case ex: Exception =>
                      logger.error(s"Error updating user $id", ex)
                      Redirect(routes.UserController.index())
                        .flashing("error" -> "An error occurred while updating user")
                  }
                }
              )
            } else {
              Future.successful(
                Redirect(routes.HomeController.index())
                  .flashing("error" -> "Access denied. Admin privileges required.")
              )
            }
          }.recover {
            case ex: Exception =>
              logger.error("Error checking user permissions", ex)
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Session expired. Please login again.")
          }
        } catch {
          case _: NumberFormatException =>
            Future.successful(
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Invalid session. Please login again.")
            )
        }
      case None =>
        Future.successful(
          Redirect(routes.AuthController.loginPage())
            .flashing("error" -> "Please login to update users.")
        )
    }
  }

  /**
   * Delete user
   * POST /users/:id/delete
   */
  def delete(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    // Check if user is logged in and is admin
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          userService.getUserById(userId).flatMap { currentUser =>
            if (currentUser.role.isAdmin) {
              userService.deleteUser(id).map { _ =>
                Redirect(routes.UserController.index())
                  .flashing("success" -> "User deleted successfully!")
              }.recover {
                case UserNotFoundException(_) =>
                  Redirect(routes.UserController.index())
                    .flashing("error" -> "User not found")
                case ex: Exception =>
                  logger.error(s"Error deleting user $id", ex)
                  Redirect(routes.UserController.index())
                    .flashing("error" -> "An error occurred while deleting user")
              }
            } else {
              Future.successful(
                Redirect(routes.HomeController.index())
                  .flashing("error" -> "Access denied. Admin privileges required.")
              )
            }
          }.recover {
            case ex: Exception =>
              logger.error("Error checking user permissions", ex)
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Session expired. Please login again.")
          }
        } catch {
          case _: NumberFormatException =>
            Future.successful(
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Invalid session. Please login again.")
            )
        }
      case None =>
        Future.successful(
          Redirect(routes.AuthController.loginPage())
            .flashing("error" -> "Please login to delete users.")
        )
    }
  }

  /**
   * Toggle user active status
   * POST /users/:id/toggle
   */
  def toggle(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    // Check if user is logged in and is admin
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          userService.getUserById(userId).flatMap { currentUser =>
            if (currentUser.role.isAdmin) {
              userService.toggleUserStatus(id).map { _ =>
                Redirect(routes.UserController.index())
                  .flashing("success" -> "User status updated!")
              }.recover {
                case UserNotFoundException(_) =>
                  Redirect(routes.UserController.index())
                    .flashing("error" -> "User not found")
                case ex: Exception =>
                  logger.error(s"Error toggling user status $id", ex)
                  Redirect(routes.UserController.index())
                    .flashing("error" -> "An error occurred while updating user status")
              }
            } else {
              Future.successful(
                Redirect(routes.HomeController.index())
                  .flashing("error" -> "Access denied. Admin privileges required.")
              )
            }
          }.recover {
            case ex: Exception =>
              logger.error("Error checking user permissions", ex)
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Session expired. Please login again.")
          }
        } catch {
          case _: NumberFormatException =>
            Future.successful(
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Invalid session. Please login again.")
            )
        }
      case None =>
        Future.successful(
          Redirect(routes.AuthController.loginPage())
            .flashing("error" -> "Please login to toggle user status.")
        )
    }
  }
}
