package repositories

import models.User
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends IUserRepository {
  
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  import java.sql.Timestamp
  import models.UserRole

  // Implicit mapper for Instant (UTC) conversions
  implicit val instantMapper: BaseColumnType[Instant] = MappedColumnType.base[Instant, Timestamp](
    instant => Timestamp.from(instant),
    ts => ts.toInstant
  )

  // Implicit mapper for UserRole enum
  implicit val userRoleMapper: BaseColumnType[UserRole] = MappedColumnType.base[UserRole, String](
    role => role.value,
    str => UserRole.fromString(str).getOrElse(UserRole.Basic)
  )

  private class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    def password = column[String]("password")
    def firstName = column[Option[String]]("first_name")
    def lastName = column[Option[String]]("last_name")
    def role = column[UserRole]("role")
    def isActive = column[Boolean]("is_active")
    def createdAt = column[Instant]("created_at")
    def updatedAt = column[Option[Instant]]("updated_at")

    def * = (id.?, email, password, firstName, lastName, role, isActive, createdAt, updatedAt) <> 
      ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UserTable]

  // Get all users
  override def list(): Future[Seq[User]] = db.run {
    users.sortBy(_.createdAt.desc).result
  }

  // Get user by ID
  override def findById(id: Long): Future[Option[User]] = db.run {
    users.filter(_.id === id).result.headOption
  }


  // Get user by email
  override def findByEmail(email: String): Future[Option[User]] = db.run {
    users.filter(_.email === email.toLowerCase).result.headOption
  }

  // Create new user
  override def create(user: User): Future[User] = db.run {
    val now = Instant.now()
    (users.map(u => (u.email, u.password, u.firstName, u.lastName, u.role, u.isActive, u.createdAt, u.updatedAt))
      returning users.map(_.id)
      into ((fields, id) => User(Some(id), fields._1, fields._2, fields._3, fields._4, fields._5, fields._6, fields._7, fields._8))
      ) += (user.email, user.password, user.firstName, user.lastName, user.role, user.isActive, user.createdAt, Some(user.updatedAt.getOrElse(now)))
  }

  // Update user
  override def update(id: Long, user: User): Future[Int] = db.run {
    val now = Instant.now()
    users.filter(_.id === id)
      .map(u => (u.email, u.password, u.firstName, u.lastName, u.role, u.isActive, u.updatedAt))
      .update((user.email, user.password, user.firstName, user.lastName, user.role, user.isActive, Some(user.updatedAt.getOrElse(now))))
  }

  // Delete user
  override def delete(id: Long): Future[Int] = db.run {
    users.filter(_.id === id).delete
  }

  // List active users
  override def listActive(): Future[Seq[User]] = db.run {
    users.filter(_.isActive === true).sortBy(_.createdAt.desc).result
  }

  // List inactive users
  override def listInactive(): Future[Seq[User]] = db.run {
    users.filter(_.isActive === false).sortBy(_.createdAt.desc).result
  }
}
