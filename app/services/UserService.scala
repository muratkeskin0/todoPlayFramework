package services

import javax.inject.{Inject, Singleton}
import models.User

import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import exceptions._
import repositories.IUserRepository

@Singleton
class UserService @Inject()(
  userRepository: IUserRepository
)(implicit ec: ExecutionContext) extends IUserService {

  // Create new user
  def createUser(user: User): Future[User] = {
    validateUser(user)

    for {
      // Check if email already exists
      existingEmail <- userRepository.findByEmail(user.email)
      _ = if (existingEmail.isDefined) throw ValidationException("Email already exists")
      
      // Create user
      createdUser <- userRepository.create(user)
    } yield createdUser
  }

  // Update user
  def updateUser(id: Long, user: User): Future[User] = {
    validateUser(user)
    
    for {
      existing <- userRepository.findById(id).map(_.getOrElse(throw UserNotFoundException(id)))
      
      // Check if email is taken by another user
      existingEmail <- userRepository.findByEmail(user.email)
      _ = if (existingEmail.isDefined && existingEmail.get.id != Some(id)) 
        throw ValidationException("Email already exists")
      
      updated = user.copy(id = Some(id), createdAt = existing.createdAt, updatedAt = Some(Instant.now()))
      rowsAffected <- userRepository.update(id, updated)
      _ = if (rowsAffected == 0) throw UserNotFoundException(id)
      result <- userRepository.findById(id).map(_.getOrElse(throw UserNotFoundException(id)))
    } yield result
  }

  // Update user (overloaded method for direct user update)
  def updateUser(user: User): Future[User] = {
    user.id match {
      case Some(id) => updateUser(id, user)
      case None => Future.failed(new IllegalArgumentException("User ID is required for update"))
    }
  }

  // Get user by ID
  def getUserById(id: Long): Future[User] = {
    userRepository.findById(id).map {
      case Some(user) => user
      case None => throw UserNotFoundException(id)
    }
  }

  // Get user by email
  def getUserByEmail(email: String): Future[User] = {
    userRepository.findByEmail(email).map {
      case Some(user) => user
      case None => throw UserEmailNotFoundException(email) // Email not found
    }
  }

  // List all users
  def listUsers(): Future[Seq[User]] = {
    userRepository.list()
  }

  // List active users
  def listActiveUsers(): Future[Seq[User]] = {
    userRepository.listActive()
  }

  // List inactive users
  def listInactiveUsers(): Future[Seq[User]] = {
    userRepository.listInactive()
  }

  // Delete user
  def deleteUser(id: Long): Future[Unit] = {
    userRepository.delete(id).map { rowsAffected =>
      if (rowsAffected == 0) throw UserNotFoundException(id)
      ()
    }
  }

  // Toggle user active status
  def toggleUserStatus(id: Long): Future[User] = {
    for {
      existing <- userRepository.findById(id).map(_.getOrElse(throw UserNotFoundException(id)))
      updated = existing.copy(isActive = !existing.isActive, updatedAt = Some(Instant.now()))
      rowsAffected <- userRepository.update(id, updated)
      _ = if (rowsAffected == 0) throw UserNotFoundException(id)
      result <- userRepository.findById(id).map(_.getOrElse(throw UserNotFoundException(id)))
    } yield result
  }

  // Private helper: Validation
  private def validateUser(user: User): Unit = {
    if (user.email.trim.isEmpty) {
      throw ValidationException("Email cannot be empty")
    }
    if (!user.email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$")) {
      throw ValidationException("Email format is invalid")
    }
    user.firstName.foreach { name =>
      if (name.length > 100) {
        throw ValidationException("First name cannot exceed 100 characters")
      }
    }
    user.lastName.foreach { name =>
      if (name.length > 100) {
        throw ValidationException("Last name cannot exceed 100 characters")
      }
    }
  }
}
