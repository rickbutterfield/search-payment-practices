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

import com.softwaremill.macwire.wire
import config.{AppConfig, PageConfig, ServiceConfig}
import controllers._
import play.api._
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickComponents}
import play.api.i18n.{I18nComponents, Lang, Messages}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.Router
import router.Routes
import services.live.CompaniesHouseSearch
import services.fakes.FakeCompanySearch
import services.{CompanySearchService, ReportService}
import slick.basic.BasicProfile
import slicks.repos.ReportTable

import scala.concurrent.ExecutionContext

class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach { configurator =>
      configurator.configure(context.environment)
    }
    (new BuiltInComponentsFromContext(context) with AppComponents).application
  }
}

trait AppComponents extends BuiltInComponents
  with SlickComponents with AhcWSComponents with I18nComponents {
  implicit lazy val executionContext: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

  implicit lazy val messages: Messages = new Messages(Lang.defaultLang, messagesApi)

  lazy val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile] = api.dbConfig(DbName("default"))
  }

  lazy val assets              : Assets               = wire[Assets]
  lazy val router              : Router               = {
    val prefix: String = "/"
    wire[Routes]
  }
  lazy val maybeRouter                                = Option(router)
  lazy val appConfig           : AppConfig            = wire[AppConfig]
  lazy val optionalSourceMapper: OptionalSourceMapper = new OptionalSourceMapper(sourceMapper)

  implicit lazy val pageConfig   : PageConfig    = appConfig.config.pageConfig
  implicit lazy val serviceConfig: ServiceConfig = appConfig.config.service.getOrElse(ServiceConfig.empty)

  lazy val defaultController   : Default            = wire[Default]
  lazy val calculatorController: HomeController     = wire[HomeController]
  lazy val searchController    : SearchController   = wire[SearchController]
  lazy val downloadController  : DownloadController = wire[DownloadController]
  lazy val reportsController   : ReportsController  = wire[ReportsController]

  lazy val reportService: ReportService = wire[ReportTable]

  lazy val companySearchService: CompanySearchService = appConfig.config.companiesHouse match {
    case Some(c) => wire[CompaniesHouseSearch]
    case None    =>
      Logger.debug("Wiring in Company Search Mock")
      wire[FakeCompanySearch]
  }
}
