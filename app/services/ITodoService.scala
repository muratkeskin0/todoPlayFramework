package services

import scala.concurrent.Future
import models.Todo

trait ITodoService {
  def createTodo(todo: Todo): Future[Todo]
  def updateTodo(id: Long, todo: Todo): Future[Todo]
  def listTodos(): Future[Seq[Todo]]
  def listTodosByUser(userId: Long): Future[Seq[Todo]]
  def getTodoById(id: Long): Future[Todo]
  def deleteTodo(id: Long): Future[Unit]
  def listCompletedTodos(): Future[Seq[Todo]]
  def listCompletedTodosByUser(userId: Long): Future[Seq[Todo]]
  def listIncompleteTodos(): Future[Seq[Todo]]
  def listIncompleteTodosByUser(userId: Long): Future[Seq[Todo]]
}


