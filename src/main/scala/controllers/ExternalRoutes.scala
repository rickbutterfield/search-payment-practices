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

package controllers

import config.RoutesConfig
import models.CompaniesHouseId

trait ExternalRouter {
  def root: String

  def publish(): String

  def publish(companiesHouseId: CompaniesHouseId): String

  def calculate(): String

  def questionnaire(): String
}

/**
  * This class generates urls that refer to the publishing application. By default it will check to see if the
  * hostname matches the naming pattern we use for environment deployments on Heroku (e.g. beis-spp-dev.herokuapp.comm.
  * If it does match then it will use a corresponding hostname for the publising app, (e.g. beis-ppr-dev.herokuapp.com).
  * If not then it will fallback to using `localhost:9000`, which is the default in a local environment.
  *
  * This default behaviour can be overridden by specifying a hostname in the application config (using the key
  * `externalRouter.publishHost`. If that is set then that hostname will always be used.
  */
class ExternalRoutes(routesConfig: RoutesConfig) {
  import views.html.helper.urlEncode

  val HerokuPattern = "beis-spp-(.*)".r

  private val publishPath = "publish"
  private val calculatePath = "deadlines"
  private val questionnairePath = "decide"

  def apply(requestHostname: String) = new ExternalRouter {
    override val root = routesConfig.publishHost match {
      case Some(hostname) => s"https://$hostname"
      case None => requestHostname match {
        case HerokuPattern(environment) => s"https://beis-ppr-$environment"
        case _ => s"http://localhost:9000"
      }
    }

    override def publish(): String = s"$root/$publishPath"

    override def publish(companiesHouseId: CompaniesHouseId): String = s"$root/$publishPath/company/${urlEncode(companiesHouseId.id)}/start"

    override def calculate(): String = s"$root/$calculatePath"

    override def questionnaire(): String = s"$root/$questionnairePath"
  }
}