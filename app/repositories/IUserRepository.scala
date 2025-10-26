package repositories

import models.User
import scala.concurrent.Future

trait IUserRepository {
  
  // Get all users
  def list(): Future[Seq[User]]
  
  // Get user by ID
  def findById(id: Long): Future[Option[User]]
  
  
  // Get user by email
  def findByEmail(email: String): Future[Option[User]]
  
  // Create new user
  def create(user: User): Future[User]
  
  // Update user
  def update(id: Long, user: User): Future[Int]
  
  // Delete user
  def delete(id: Long): Future[Int]
  
  // List active users
  def listActive(): Future[Seq[User]]
  
  // List inactive users
  def listInactive(): Future[Seq[User]]
}
