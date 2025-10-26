package filters

import org.apache.pekko.stream.Materializer
import play.api.libs.json._
import play.api.mvc._
import play.api.Logger
import jakarta.inject._
import exceptions._
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

@Singleton
class RequestLoggingFilter @Inject()(
  implicit val mat: Materializer,
  ec: ExecutionContext
) extends Filter {

  private val logger = Logger("RequestLoggingFilter")

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis()
    val requestId = java.util.UUID.randomUUID().toString.take(8)
    
    // Session'dan kullanıcı bilgilerini çek
    val userId = requestHeader.session.get("userId")
    val userEmail = requestHeader.session.get("userEmail")
    val userRole = requestHeader.session.get("userRole")
    
    // Safe header extraction
    val userAgent = requestHeader.headers.get("User-Agent").getOrElse("Unknown")
    val referer = requestHeader.headers.get("Referer")
    val contentType = requestHeader.headers.get("Content-Type")
    
    // Request JSON log
    val requestLog = Json.obj(
      "type" -> "REQUEST",
      "requestId" -> requestId,
      "timestamp" -> Instant.now().toString,
      "method" -> requestHeader.method,
      "uri" -> requestHeader.uri,
      "path" -> requestHeader.path,
      "remoteAddress" -> requestHeader.remoteAddress,
      "userAgent" -> userAgent,
      "referer" -> referer,
      "contentType" -> contentType,
      "user" -> Json.obj(
        "id" -> userId,
        "email" -> userEmail,
        "role" -> userRole,
        "authenticated" -> userId.isDefined
      )
    )
    
    logger.info(s"REQ: ${Json.stringify(requestLog)}")
    
    nextFilter(requestHeader).map { result =>
      val endTime = System.currentTimeMillis()
      val duration = endTime - startTime
      
      // Response JSON log
      val responseLog = Json.obj(
        "type" -> "RESPONSE",
        "requestId" -> requestId,
        "timestamp" -> Instant.now().toString,
        "method" -> requestHeader.method,
        "uri" -> requestHeader.uri,
        "status" -> result.header.status,
        "statusText" -> getStatusText(result.header.status),
        "duration" -> s"${duration}ms",
        "contentType" -> result.body.contentType,
        "user" -> Json.obj(
          "id" -> userId,
          "email" -> userEmail,
          "authenticated" -> userId.isDefined
        ),
        "performance" -> Json.obj(
          "slow" -> (duration > 1000),
          "category" -> getPerformanceCategory(duration)
        ),
        "security" -> Json.obj(
          "error" -> (result.header.status >= 400),
          "clientError" -> (result.header.status >= 400 && result.header.status < 500),
          "serverError" -> (result.header.status >= 500)
        )
      )
      
      // Log level'ını status'a göre belirle
      if (result.header.status >= 500) {
        logger.error(s"RES: ${Json.stringify(responseLog)}")
      } else if (result.header.status >= 400) {
        logger.warn(s"RES: ${Json.stringify(responseLog)}")
      } else if (duration > 2000) {
        logger.warn(s"RES (SLOW): ${Json.stringify(responseLog)}")
      } else {
        logger.info(s"RES: ${Json.stringify(responseLog)}")
      }
      
      result
    }.recover {
      case ex: Throwable =>
        val errorTime = System.currentTimeMillis()
        val errorDuration = errorTime - startTime
        
        // Simple domain vs technical exception handling
        val (category, severity, message) = ex match {
          case domainEx: DomainException => 
            ("DOMAIN", "LOW", domainEx.message)
          case _ => 
            ("TECHNICAL", "HIGH", "Unexpected system error occurred")
        }
        
        // Exception JSON log
        val errorLog = Json.obj(
          "type" -> "ERROR",
          "requestId" -> requestId,
          "timestamp" -> Instant.now().toString,
          "method" -> requestHeader.method,
          "uri" -> requestHeader.uri,
          "duration" -> s"${errorDuration}ms",
          "user" -> Json.obj(
            "id" -> userId,
            "email" -> userEmail,
            "authenticated" -> userId.isDefined
          ),
          "error" -> Json.obj(
            "message" -> message,
            "originalMessage" -> ex.getMessage,
            "class" -> ex.getClass.getSimpleName,
            "category" -> category,
            "severity" -> severity,
            "isDomain" -> ex.isInstanceOf[DomainException],
            "stackTrace" -> ex.getStackTrace.take(5).map(_.toString).mkString("; ")
          )
        )
        
        // Log based on severity
        if (severity == "HIGH") {
          logger.error(s"ERR-TECHNICAL: ${Json.stringify(errorLog)}")
        } else {
          logger.info(s"ERR-DOMAIN: ${Json.stringify(errorLog)}")
        }
        
        // Return error response instead of throwing
        Results.InternalServerError("Internal Server Error")
    }
  }
  
  private def getStatusText(status: Int): String = status match {
    case 200 => "OK"
    case 201 => "Created"
    case 302 => "Redirect"
    case 400 => "Bad Request"
    case 401 => "Unauthorized"
    case 403 => "Forbidden"
    case 404 => "Not Found"
    case 500 => "Internal Server Error"
    case _ => s"HTTP $status"
  }
  
  private def getPerformanceCategory(duration: Long): String = duration match {
    case d if d < 100 => "fast"
    case d if d < 500 => "normal"
    case d if d < 1000 => "slow"
    case d if d < 2000 => "very-slow"
    case _ => "critical"
  }
}
