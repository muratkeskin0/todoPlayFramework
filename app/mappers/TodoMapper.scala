package mappers

import java.time.Instant
import models.Todo
import dto.TodoDTO._

object TodoMapper {
  def fromCreate(request: CreateTodoRequest, now: Instant = Instant.now()): Todo = {
    Todo(
      id = None,
      title = request.title.trim,
      description = request.description.map(_.trim).filter(_.nonEmpty),
      completed = request.completed,
      createdAt = now
    )
  }

  def fromUpdate(existing: Todo, request: UpdateTodoRequest): Todo = {
    existing.copy(
      title = request.title.trim,
      description = request.description.map(_.trim).filter(_.nonEmpty),
      completed = request.completed
    )
  }

  def toResponse(todo: Todo): TodoResponse = {
    TodoResponse(
      id = todo.id.getOrElse(0L),
      title = todo.title,
      description = todo.description,
      completed = todo.completed,
      createdAt = todo.createdAt.toString
    )
  }
}


