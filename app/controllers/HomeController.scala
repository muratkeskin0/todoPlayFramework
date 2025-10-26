package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import services.{ITodoService, IUserService}
import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  val controllerComponents: ControllerComponents,
  todoService: ITodoService,
  userService: IUserService
)(implicit ec: ExecutionContext) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action.async { implicit request: Request[AnyContent] =>
    // Check if user is logged in
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          // Get user info and their todos
          for {
            user <- userService.getUserById(userId)
            todos <- todoService.listTodosByUser(userId)
            completedTodos <- todoService.listCompletedTodosByUser(userId)
            incompleteTodos <- todoService.listIncompleteTodosByUser(userId)
          } yield {
            Ok(views.html.index(Some(user), todos, completedTodos, incompleteTodos))
          }
        } catch {
          case _: NumberFormatException =>
            // Invalid userId in session, clear session and redirect to login
            Future.successful(
              Redirect(routes.AuthController.loginPage())
                .withNewSession
                .flashing("error" -> "Invalid session. Please login again.")
            )
        }
      case None =>
        // No user session, show public home page
        Future.successful(Ok(views.html.index(None, Seq.empty, Seq.empty, Seq.empty)))
    }
  }

}
