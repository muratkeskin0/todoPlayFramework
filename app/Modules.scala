import com.google.inject.AbstractModule
import repositories.{ITodoRepository, TodoRepository}
import services.{ITodoService, TodoService}

class Modules extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ITodoRepository]).to(classOf[TodoRepository])
    bind(classOf[ITodoService]).to(classOf[TodoService])
  }
}

