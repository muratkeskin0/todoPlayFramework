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
   * Home page - List all todos
   * GET /todos
   */
  def index() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    todoService.listTodos().map { todos =>
      Ok(views.html.todos.index(todos, TodoForm.form))
    }.recover {
      case ex: Exception =>
        logger.error("Error listing todos", ex)
        InternalServerError(views.html.error("An error occurred", ex.getMessage))
    }
  }

  /**
   * Create new todo
   * POST /todos
   */
  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    TodoForm.form.bindFromRequest().fold(
      formWithErrors => {
        // Validation errors
        todoService.listTodos().map { todos =>
          BadRequest(views.html.todos.index(todos, formWithErrors))
        }
      },
      todoData => {
        // Validation successful
        val createRequest = _root_.dto.TodoDTO.CreateTodoRequest(
          title = todoData.title,
          description = todoData.description,
          completed = todoData.completed
        )
        
        todoService.createTodo(createRequest).map { _ =>
          Redirect(routes.TodoController.index())
            .flashing("success" -> "Todo created successfully!")
        }.recoverWith {
          case ValidationException(msg) =>
            todoService.listTodos().map { todos =>
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
  }

  /**
   * Todo edit page
   * GET /todos/:id/edit
   */
  def edit(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    todoService.getTodoById(id).map { todo =>
      val formData = TodoForm.TodoData(
        title = todo.title,
        description = todo.description,
        completed = todo.completed
      )
      Ok(views.html.todos.edit(id, TodoForm.form.fill(formData)))
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

  /**
   * Update todo
   * POST /todos/:id/update
   */
  def update(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    TodoForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.todos.edit(id, formWithErrors)))
      },
      todoData => {
        val updateRequest = _root_.dto.TodoDTO.UpdateTodoRequest(
          title = todoData.title,
          description = todoData.description,
          completed = todoData.completed
        )
        
        todoService.updateTodo(id, updateRequest).map { _ =>
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
      }
    )
  }

  /**
   * Delete todo
   * POST /todos/:id/delete
   */
  def delete(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
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
  }

  /**
   * Toggle todo completed status
   * POST /todos/:id/toggle
   */
  def toggle(id: Long) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    todoService.getTodoById(id).flatMap { todo =>
      val updateRequest = _root_.dto.TodoDTO.UpdateTodoRequest(
        title = todo.title,
        description = todo.description,
        completed = !todo.completed
      )
      
      todoService.updateTodo(id, updateRequest).map { _ =>
        Redirect(routes.TodoController.index())
          .flashing("success" -> s"Todo marked as ${if (!todo.completed) "completed" else "incomplete"}!")
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
  }
}
