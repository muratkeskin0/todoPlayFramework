package startup

import models.{User, UserRole}
import repositories.IUserRepository
import security.IPasswordService
import play.api.Configuration
import play.api.Logger
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Startup hook to initialize admin user on application start
 */
@Singleton
class StartupHook @Inject()(
  userRepository: IUserRepository,
  passwordService: IPasswordService,
  configuration: Configuration
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  // Initialize database and admin user on startup (non-blocking)
  initializeDatabase().flatMap(_ => initializeAdminUser()).onComplete {
    case scala.util.Success(_) => logger.info("StartupHook completed successfully")
    case scala.util.Failure(ex) => logger.error(s"StartupHook failed: ${ex.getMessage}")
  }

  /**
   * Check if users table exists, if not run migrations
   */
  def initializeDatabase(): Future[Unit] = {
    logger.info("Checking if users table exists...")
    
    // Try to query users table to check if it exists
    userRepository.findByEmail("dummy@test.com").map { _ =>
      logger.info("Users table exists, database is ready")
    }.recover {
      case ex: Exception =>
        logger.warn(s"Users table not found or database not ready: ${ex.getMessage}")
        logger.info("Database will be initialized by migration service...")
        // Migration service will handle table creation
    }
  }

  /**
   * Initialize admin user on application startup
   */
  def initializeAdminUser(): Future[Unit] = {
    val adminEmail = configuration.get[String]("admin.email")
    val adminFirstName = configuration.get[String]("admin.firstName")
    val adminLastName = configuration.get[String]("admin.lastName")
    val adminPassword = configuration.get[String]("admin.password")

    logger.info(s"Checking for admin user: $adminEmail")

    userRepository.findByEmail(adminEmail).flatMap {
      case Some(_) =>
        logger.info(s"Admin user already exists: $adminEmail")
        Future.successful(())
      
      case None =>
        logger.info(s"Creating admin user: $adminEmail")
        val hashedPassword = passwordService.hashPassword(adminPassword)
        
        val adminUser = User(
          id = None, // Will be auto-generated
          email = adminEmail,
          firstName = Some(adminFirstName),
          lastName = Some(adminLastName),
          role = UserRole.Admin,
          isActive = true,
          password = hashedPassword
        )
        
        userRepository.create(adminUser).map { createdUser =>
          logger.info(s"Admin user created successfully: ${createdUser.email} (ID: ${createdUser.id})")
        }.recover {
          case ex: Exception =>
            logger.error(s"Failed to create admin user: ${ex.getMessage}")
            // Don't fail the entire startup if admin user creation fails
            ()
        }
    }.recover {
      case ex: Exception =>
        logger.error(s"Database not ready for admin user creation: ${ex.getMessage}")
        logger.info("Will retry admin user creation on next startup")
        Future.successful(())
    }
  }
}
