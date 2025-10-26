import com.google.inject.AbstractModule
import actors.EmailActorManager
import repositories.{ITodoRepository, TodoRepository, IUserRepository, UserRepository}
import services.{AuthService, IAuthService, ITodoService, IUserService, TodoService, UserService, EmailService, IEmailService}
import security.{PasswordService, IPasswordService}
import startup.StartupHook

class Modules extends AbstractModule {
  override def configure(): Unit = {
    // Todo bindings
    bind(classOf[ITodoRepository]).to(classOf[TodoRepository])
    bind(classOf[ITodoService]).to(classOf[TodoService])
    
    // User bindings
    bind(classOf[IUserRepository]).to(classOf[UserRepository])
    bind(classOf[IUserService]).to(classOf[UserService])
    
    // Auth bindings
    bind(classOf[IAuthService]).to(classOf[AuthService])
    
    // Email service bindings
    bind(classOf[IEmailService]).to(classOf[EmailService])
    
    // Security bindings
    bind(classOf[IPasswordService]).to(classOf[PasswordService])
    
    // Actor bindings
    bind(classOf[EmailActorManager]).asEagerSingleton()
    
    // Startup bindings
    bind(classOf[StartupHook]).asEagerSingleton()
  }
}