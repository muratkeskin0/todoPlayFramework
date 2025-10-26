package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n.I18nSupport
import services.ITodoService
import forms.TodoForm
import exceptions.{TodoNotFoundException, ValidationException}
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging

/**
 * View-based Todo Controller
 * Uses HTML forms and server-side rendering
 */
@Singleton
class TodoController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  todoService: ITodoService
)(implicit ec: ExecutionContext) extends MessagesAbstractController(controllerComponents) with I18nSupport with Logging {

  /**
   * Home page - List user's todos
   * GET /todos
   */
  def index() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          todoService.listTodosByUser(userId).map { todos =>
            Ok(views.html.todos.index(todos, TodoForm.form))
          }.recover {
            case ex: Exception =>
              logger.error("Error listing todos", ex)
              InternalServerError(views.html.error("An error occurred", ex.getMessage))
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
            .flashing("error" -> "Please login to access your todos.")
        )
    }
  }

  /**
   * Create new todo
   * POST /todos
   */
  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          TodoForm.form.bindFromRequest().fold(
            formWithErrors => {
              // Validation errors
              todoService.listTodosByUser(userId).map { todos =>
                BadRequest(views.html.todos.index(todos, formWithErrors))
              }
            },
            todoData => {
              // Convert form data directly to Todo model
              val todo = models.Todo(
                title = todoData.title,
                description = todoData.description,
                completed = todoData.completed,
                userId = Some(userId) // Use logged in user's ID
              )
              
              todoService.createTodo(todo).map { _ =>
                Redirect(routes.TodoController.index())
                  .flashing("success" -> "Todo created successfully!")
              }.recoverWith {
                case ValidationException(msg) =>
                  todoService.listTodosByUser(userId).map { todos =>
                    BadRequest(views.html.todos.index(
                      todos, 
                      TodoForm.form.fill(todoData).withGlobalError(msg)
                    ))
                  }
                case ex: Exception =>
                  logger.error("Error creating todo", ex)
                  Future.successful(
                    Redirect(routes.TodoController.index())
                      .flashing("error" -> "An error occurred while creating todo")
                  )
              }
            }
          )
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
            .flashing("error" -> "Please login to create todos.")
        )
    }
  }

  /**
   * Todo edit page
   * GET /todos/:id/edit
   */
  def edit(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          todoService.getTodoById(id).map { todo =>
            // Check if todo belongs to the logged in user
            if (todo.userId.contains(userId)) {
              val formData = TodoForm.TodoData(
                title = todo.title,
                description = todo.description,
                completed = todo.completed,
                userId = todo.userId
              )
              Ok(views.html.todos.edit(id, TodoForm.form.fill(formData)))
            } else {
              Redirect(routes.TodoController.index())
                .flashing("error" -> "You can only edit your own todos")
            }
          }.recover {
            case TodoNotFoundException(_) =>
              Redirect(routes.TodoController.index())
                .flashing("error" -> "Todo not found")
            case ex: Exception =>
              logger.error(s"Error loading todo $id", ex)
              Redirect(routes.TodoController.index())
                .flashing("error" -> "An error occurred")
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
            .flashing("error" -> "Please login to edit todos.")
        )
    }
  }

  /**
   * Update todo
   * POST /todos/:id/update
   */
  def update(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          TodoForm.form.bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(views.html.todos.edit(id, formWithErrors)))
            },
            todoData => {
              // First check if todo belongs to user
              todoService.getTodoById(id).flatMap { existingTodo =>
                if (existingTodo.userId.contains(userId)) {
                  // Convert form data directly to Todo model
                  val updatedTodo = models.Todo(
                    id = Some(id),
                    title = todoData.title,
                    description = todoData.description,
                    completed = todoData.completed,
                    userId = Some(userId), // Use logged in user's ID
                    createdAt = existingTodo.createdAt // Preserve original creation time
                  )
                  
                  todoService.updateTodo(id, updatedTodo).map { _ =>
                    Redirect(routes.TodoController.index())
                      .flashing("success" -> "Todo updated successfully!")
                  }.recover {
                    case TodoNotFoundException(_) =>
                      Redirect(routes.TodoController.index())
                        .flashing("error" -> "Todo not found")
                    case ValidationException(msg) =>
                      BadRequest(views.html.todos.edit(id, TodoForm.form.fill(todoData).withGlobalError(msg)))
                    case ex: Exception =>
                      logger.error(s"Error updating todo $id", ex)
                      Redirect(routes.TodoController.index())
                        .flashing("error" -> "An error occurred while updating todo")
                  }
                } else {
                  Future.successful(
                    Redirect(routes.TodoController.index())
                      .flashing("error" -> "You can only update your own todos")
                  )
                }
              }.recover {
                case TodoNotFoundException(_) =>
                  Redirect(routes.TodoController.index())
                    .flashing("error" -> "Todo not found")
                case ex: Exception =>
                  logger.error(s"Error loading todo $id", ex)
                  Redirect(routes.TodoController.index())
                    .flashing("error" -> "An error occurred")
              }
            }
          )
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
            .flashing("error" -> "Please login to update todos.")
        )
    }
  }

  /**
   * Delete todo
   * POST /todos/:id/delete
   */
  def delete(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          // First check if todo belongs to user
          todoService.getTodoById(id).flatMap { todo =>
            if (todo.userId.contains(userId)) {
              todoService.deleteTodo(id).map { _ =>
                Redirect(routes.TodoController.index())
                  .flashing("success" -> "Todo deleted successfully!")
              }.recover {
                case TodoNotFoundException(_) =>
                  Redirect(routes.TodoController.index())
                    .flashing("error" -> "Todo not found")
                case ex: Exception =>
                  logger.error(s"Error deleting todo $id", ex)
                  Redirect(routes.TodoController.index())
                    .flashing("error" -> "An error occurred while deleting todo")
              }
            } else {
              Future.successful(
                Redirect(routes.TodoController.index())
                  .flashing("error" -> "You can only delete your own todos")
              )
            }
          }.recover {
            case TodoNotFoundException(_) =>
              Redirect(routes.TodoController.index())
                .flashing("error" -> "Todo not found")
            case ex: Exception =>
              logger.error(s"Error loading todo $id", ex)
              Redirect(routes.TodoController.index())
                .flashing("error" -> "An error occurred")
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
            .flashing("error" -> "Please login to delete todos.")
        )
    }
  }

  /**
   * Toggle todo completed status
   * POST /todos/:id/toggle
   */
  def toggle(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // Check if user is logged in
    request.session.get("userId") match {
      case Some(userIdStr) =>
        try {
          val userId = userIdStr.toLong
          todoService.getTodoById(id).flatMap { todo =>
            // Check if todo belongs to user
            if (todo.userId.contains(userId)) {
              // Convert form data directly to Todo model
              val updatedTodo = models.Todo(
                id = Some(id),
                title = todo.title,
                description = todo.description,
                completed = !todo.completed,
                userId = Some(userId), // Use logged in user's ID
                createdAt = todo.createdAt // Preserve original creation time
              )
              
              todoService.updateTodo(id, updatedTodo).map { _ =>
                Redirect(routes.TodoController.index())
                  .flashing("success" -> s"Todo marked as ${if (!todo.completed) "completed" else "incomplete"}!")
              }
            } else {
              Future.successful(
                Redirect(routes.TodoController.index())
                  .flashing("error" -> "You can only toggle your own todos")
              )
            }
          }.recover {
            case TodoNotFoundException(_) =>
              Redirect(routes.TodoController.index())
                .flashing("error" -> "Todo not found")
            case ex: Exception =>
              logger.error(s"Error toggling todo $id", ex)
              Redirect(routes.TodoController.index())
                .flashing("error" -> "An error occurred")
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
            .flashing("error" -> "Please login to toggle todos.")
        )
    }
  }
}
