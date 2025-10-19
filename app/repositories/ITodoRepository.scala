package repositories

import models.Todo

import scala.concurrent.Future

trait ITodoRepository {
  def list(): Future[Seq[Todo]]
  def findById(id: Long): Future[Option[Todo]]
  def create(todo: Todo): Future[Todo]
  def update(id: Long, todo: Todo): Future[Int]
  def delete(id: Long): Future[Int]
  def listCompleted(): Future[Seq[Todo]]
  def listIncomplete(): Future[Seq[Todo]]
}
