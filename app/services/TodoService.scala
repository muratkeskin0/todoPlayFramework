package services

import javax.inject.{Inject, Singleton}
import models.Todo
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import exceptions.{TodoNotFoundException, ValidationException}
import repositories.ITodoRepository

@Singleton
class TodoService @Inject()(
  todoRepository: ITodoRepository
)(implicit ec: ExecutionContext) extends ITodoService {

  // Business logic: Create todo
  def createTodo(todo: Todo): Future[Todo] = {
    validateTodo(todo)
    todoRepository.create(todo)
  }

  // Business logic: Update todo
  def updateTodo(id: Long, todo: Todo): Future[Todo] = {
    validateTodo(todo)
    
    for {
      existing <- todoRepository.findById(id).map(_.getOrElse(throw TodoNotFoundException(id)))
      updated = todo.copy(id = Some(id), createdAt = existing.createdAt)
      rowsAffected <- todoRepository.update(id, updated)
      _ = if (rowsAffected == 0) throw TodoNotFoundException(id)
      result <- todoRepository.findById(id).map(_.getOrElse(throw TodoNotFoundException(id)))
    } yield result
  }

  // List todos
  def listTodos(): Future[Seq[Todo]] = {
    todoRepository.list()
  }

  // List todos by user
  def listTodosByUser(userId: Long): Future[Seq[Todo]] = {
    todoRepository.listByUser(userId)
  }

  // Get todo by ID
  def getTodoById(id: Long): Future[Todo] = {
    todoRepository.findById(id).map {
      case Some(todo) => todo
      case None => throw TodoNotFoundException(id)
    }
  }

  // Delete todo
  def deleteTodo(id: Long): Future[Unit] = {
    todoRepository.delete(id).map { rowsAffected =>
      if (rowsAffected == 0) throw TodoNotFoundException(id)
      ()
    }
  }

  // List completed todos
  def listCompletedTodos(): Future[Seq[Todo]] = {
    todoRepository.listCompleted()
  }

  // List completed todos by user
  def listCompletedTodosByUser(userId: Long): Future[Seq[Todo]] = {
    todoRepository.listCompletedByUser(userId)
  }

  // List incomplete todos
  def listIncompleteTodos(): Future[Seq[Todo]] = {
    todoRepository.listIncomplete()
  }

  // List incomplete todos by user
  def listIncompleteTodosByUser(userId: Long): Future[Seq[Todo]] = {
    todoRepository.listIncompleteByUser(userId)
  }

  // Private helper: Validation
  private def validateTodo(todo: Todo): Unit = {
    if (todo.title.trim.isEmpty) {
      throw ValidationException("Title cannot be empty")
    }
    if (todo.title.length > 255) {
      throw ValidationException("Title cannot exceed 255 characters")
    }
    todo.description.foreach { desc =>
      if (desc.length > 5000) {
        throw ValidationException("Description cannot exceed 5000 characters")
      }
    }
  }

}

