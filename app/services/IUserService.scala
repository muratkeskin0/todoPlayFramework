package services

import scala.concurrent.Future
import models.User

trait IUserService {
  
  // Create new user
  def createUser(user: User): Future[User]
  
  // Update user
  def updateUser(id: Long, user: User): Future[User]
  
  // Get user by ID
  def getUserById(id: Long): Future[User]
  
  // Get user by email
  def getUserByEmail(email: String): Future[User]
  
  // List all users
  def listUsers(): Future[Seq[User]]
  
  // List active users
  def listActiveUsers(): Future[Seq[User]]
  
  // List inactive users
  def listInactiveUsers(): Future[Seq[User]]
  
  // Delete user
  def deleteUser(id: Long): Future[Unit]
  
  // Toggle user active status
  def toggleUserStatus(id: Long): Future[User]
}
