package filters

import play.api.http.HttpFilters
import play.filters.csrf.CSRFFilter
import play.filters.headers.SecurityHeadersFilter
import play.filters.hosts.AllowedHostsFilter
import jakarta.inject._

@Singleton
class Filters @Inject()(
  csrfFilter: CSRFFilter,
  securityHeadersFilter: SecurityHeadersFilter,
  allowedHostsFilter: AllowedHostsFilter,
  requestLoggingFilter: RequestLoggingFilter
) extends HttpFilters {

  override val filters = Seq(
    requestLoggingFilter,    // İlk önce logging filter
    csrfFilter,
    securityHeadersFilter,
    allowedHostsFilter
  )
}


