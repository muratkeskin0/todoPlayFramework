package forms

import play.api.data._
import play.api.data.Forms._

/**
 * Todo form data and validation
 */
object TodoForm {
  
  case class TodoData(
    title: String,
    description: Option[String],
    completed: Boolean
  )

  val form: Form[TodoData] = Form(
    mapping(
      "title" -> nonEmptyText(maxLength = 255)
        .verifying("Title cannot be empty", _.trim.nonEmpty),
      "description" -> optional(text(maxLength = 5000)),
      "completed" -> boolean
    )(TodoData.apply)(TodoData.unapply)
  )
}

