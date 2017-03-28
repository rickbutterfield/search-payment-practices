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

import com.google.inject.AbstractModule
import config._
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment, Logger}
import services._
import services.live.CompaniesHouseSearch
import services.mocks.MockCompanySearch

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {

    val config = new AppConfig(configuration).config

    config.companiesHouse match {
      case Some(ch) =>
        bind(classOf[CompaniesHouseConfig]).toInstance(ch)
        bind(classOf[CompanySearchService]).to(classOf[CompaniesHouseSearch])
      case None =>
        Logger.debug("Wiring in Company Search Mock")
        bind(classOf[CompanySearchService]).to(classOf[MockCompanySearch])
    }

    bind(classOf[PageConfig]).toInstance(config.pageConfig)

    bind(classOf[ServiceConfig]).toInstance(config.service.getOrElse(ServiceConfig.empty))
  }
}
