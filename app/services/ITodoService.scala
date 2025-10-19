package services

import dto.TodoDTO._
import scala.concurrent.Future

trait ITodoService {
  def createTodo(request: CreateTodoRequest): Future[TodoResponse]
  def updateTodo(id: Long, request: UpdateTodoRequest): Future[TodoResponse]
  def listTodos(): Future[Seq[TodoResponse]]
  def getTodoById(id: Long): Future[TodoResponse]
  def deleteTodo(id: Long): Future[Unit]
  def listCompletedTodos(): Future[Seq[TodoResponse]]
  def listIncompleteTodos(): Future[Seq[TodoResponse]]
}


