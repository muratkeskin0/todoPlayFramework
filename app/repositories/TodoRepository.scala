package repositories

import models.Todo
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TodoRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends ITodoRepository {
  
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  import java.sql.Timestamp

  // Implicit mapper for Instant (UTC) conversions
  implicit val instantMapper: BaseColumnType[Instant] = MappedColumnType.base[Instant, Timestamp](
    instant => Timestamp.from(instant),
    ts => ts.toInstant
  )

  private class TodoTable(tag: Tag) extends Table[Todo](tag, "todos") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[Option[String]]("description")
    def completed = column[Boolean]("completed")
    def userId = column[Option[Long]]("user_id")
    def createdAt = column[Instant]("created_at")

    def * = (id.?, title, description, completed, userId, createdAt) <> ((Todo.apply _).tupled, Todo.unapply)
  }

  private val todos = TableQuery[TodoTable]

  // Get all todos
  override def list(): Future[Seq[Todo]] = db.run {
    todos.result
  }

  // Get todos by user
  override def listByUser(userId: Long): Future[Seq[Todo]] = db.run {
    todos.filter(_.userId === userId).result
  }

  // Get todo by ID
  override def findById(id: Long): Future[Option[Todo]] = db.run {
    todos.filter(_.id === id).result.headOption
  }

  // Create new todo
  override def create(todo: Todo): Future[Todo] = db.run {
    (todos.map(t => (t.title, t.description, t.completed, t.userId, t.createdAt))
      returning todos.map(_.id)
      into ((fields, id) => Todo(Some(id), fields._1, fields._2, fields._3, fields._4, fields._5))
      ) += (todo.title, todo.description, todo.completed, todo.userId, todo.createdAt)
  }

  // Update todo
  override def update(id: Long, todo: Todo): Future[Int] = db.run {
    todos.filter(_.id === id)
      .map(t => (t.title, t.description, t.completed, t.userId))
      .update((todo.title, todo.description, todo.completed, todo.userId))
  }

  // Delete todo
  override def delete(id: Long): Future[Int] = db.run {
    todos.filter(_.id === id).delete
  }

  // Get completed todos
  override def listCompleted(): Future[Seq[Todo]] = db.run {
    todos.filter(_.completed === true).result
  }

  // Get completed todos by user
  override def listCompletedByUser(userId: Long): Future[Seq[Todo]] = db.run {
    todos.filter(t => t.completed === true && t.userId === userId).result
  }

  // Get incomplete todos
  override def listIncomplete(): Future[Seq[Todo]] = db.run {
    todos.filter(_.completed === false).result
  }

  // Get incomplete todos by user
  override def listIncompleteByUser(userId: Long): Future[Seq[Todo]] = db.run {
    todos.filter(t => t.completed === false && t.userId === userId).result
  }
}

