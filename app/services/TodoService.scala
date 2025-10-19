package services

import javax.inject.{Inject, Singleton}
import models.Todo
import dto.TodoDTO._
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import exceptions.{TodoNotFoundException, ValidationException}
import repositories.ITodoRepository
import mappers.TodoMapper

@Singleton
class TodoService @Inject()(
  todoRepository: ITodoRepository
)(implicit ec: ExecutionContext) extends ITodoService {

  // Business logic: Create todo
  def createTodo(request: CreateTodoRequest): Future[TodoResponse] = {
    validateRequest(request)

    val todo = TodoMapper.fromCreate(request, Instant.now())
    todoRepository.create(todo).map(TodoMapper.toResponse)
  }

  // Business logic: Update todo
  def updateTodo(id: Long, request: UpdateTodoRequest): Future[TodoResponse] = {
    validateRequest(request)
    
    for {
      existing <- todoRepository.findById(id).map(_.getOrElse(throw TodoNotFoundException(id)))
      updated = TodoMapper.fromUpdate(existing, request)
      rowsAffected <- todoRepository.update(id, updated)
      _ = if (rowsAffected == 0) throw TodoNotFoundException(id)
      result <- todoRepository.findById(id).map(_.getOrElse(throw TodoNotFoundException(id)))
    } yield TodoMapper.toResponse(result)
  }

  // List todos
  def listTodos(): Future[Seq[TodoResponse]] = {
    todoRepository.list().map(_.map(TodoMapper.toResponse))
  }

  // Get todo by ID
  def getTodoById(id: Long): Future[TodoResponse] = {
    todoRepository.findById(id).map {
      case Some(todo) => TodoMapper.toResponse(todo)
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
  def listCompletedTodos(): Future[Seq[TodoResponse]] = {
    todoRepository.listCompleted().map(_.map(TodoMapper.toResponse))
  }

  // List incomplete todos
  def listIncompleteTodos(): Future[Seq[TodoResponse]] = {
    todoRepository.listIncomplete().map(_.map(TodoMapper.toResponse))
  }

  // Private helper: Validation
  private def validateRequest(request: TodoRequest): Unit = {
    if (request.title.trim.isEmpty) {
      throw ValidationException("Title cannot be empty")
    }
    if (request.title.length > 255) {
      throw ValidationException("Title cannot exceed 255 characters")
    }
    request.description.foreach { desc =>
      if (desc.length > 5000) {
        throw ValidationException("Description cannot exceed 5000 characters")
      }
    }
  }

}

