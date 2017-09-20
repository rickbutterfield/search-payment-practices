/*
 * Copyright (C) 2017  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package config

import javax.inject.{Inject, Singleton}

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.Configuration

import scala.util.Try

case class CompaniesHouseConfig(apiKey: String)

case class GoogleAnalyticsConfig(code: Option[String])

object GoogleAnalyticsConfig {
  val empty = GoogleAnalyticsConfig(None)
}

case class ServiceConfig(startDate: Option[LocalDate])

object ServiceConfig {
  val empty = ServiceConfig(None)
  val defaultServiceStartDate = new LocalDate(2017, 4, 6)
}

case class SurveyMonkeyConfig(feedbackFormCode: Option[String])

object SurveyMonkeyConfig {
  val empty = SurveyMonkeyConfig(None)
}

case class RoutesConfig(publishHost: Option[String])

object RoutesConfig {
  val empty = RoutesConfig(None)
}

case class Config(
                   service: Option[ServiceConfig],
                   companiesHouse: Option[CompaniesHouseConfig],
                   logAssets: Option[Boolean],
                   logRequests: Option[Boolean],
                   pageConfig: PageConfig
                 )

case class PageConfig(googleAnalyticsConfig: GoogleAnalyticsConfig, routesConfig: RoutesConfig, surveyMonkeyConfig: SurveyMonkeyConfig)

@Singleton
class AppConfig @Inject()(configuration: Configuration) {
  val df = DateTimeFormat.forPattern("yyyy-M-d")

  import pureconfig._
  import ConfigConvert._

  private def load[T: ConfigConvert](path: String): Option[T] = Try {
    loadConfig[T](configuration.underlying, path).toOption
  }.toOption.flatten

  implicit val localDateConvert: ConfigConvert[LocalDate] = ConfigConvert.stringConvert[LocalDate](s => Try(df.parseLocalDate(s)), df.print(_))

  val service: Option[ServiceConfig] = load[ServiceConfig]("service")
  val companiesHouse: Option[CompaniesHouseConfig] = load[CompaniesHouseConfig]("companiesHouse")
  val googleAnalytics: GoogleAnalyticsConfig = load[GoogleAnalyticsConfig]("googleAnalytics").getOrElse(GoogleAnalyticsConfig.empty)
  val sessionTimeoutInMinutes: Option[Int] = load[Int]("sessionTimeoutInMinutes")
  val logAssets: Option[Boolean] = load[Boolean]("logAssets")
  val logRequests: Option[Boolean] = load[Boolean]("logRequests")
  val printDBTables: Option[Boolean] = load[Boolean]("printDBTables")
  val routesConfig: RoutesConfig = load[RoutesConfig]("externalRouter").getOrElse(RoutesConfig.empty)
  val surveyMonkeyConfig: SurveyMonkeyConfig = load[SurveyMonkeyConfig]("surveyMonkey").getOrElse(SurveyMonkeyConfig.empty)

  val config = Config(service, companiesHouse, logAssets, logRequests, PageConfig(googleAnalytics, routesConfig, surveyMonkeyConfig))
}