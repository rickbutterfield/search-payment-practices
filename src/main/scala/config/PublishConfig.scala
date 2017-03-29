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

import models.CompaniesHouseId

case class PublishConfig(publishUrl: String, calculatorUrl: String, questionnaireUrl: String) {
  def startPublishing(companiesHouseId: CompaniesHouseId): String = s"$publishUrl/company/${companiesHouseId.id}/start"
}

object PublishConfig {
  private val publishPath = "report-payment-practices"
  private val calculatePath = "calculate-reporting-deadlines"
  private val questionnairePath = "check-if-you-need-to-report"

  val local = PublishConfig(
    s"http://localhost:9000/$publishPath",
    s"http://localhost:9000/$calculatePath",
    s"http://localhost:9000/$questionnairePath"
  )

  def fromHostname(hostname: String): PublishConfig = {
    val Pattern = "beis-spp-(.*)".r
    val pprBase = hostname match {
      case Pattern(environment) => s"https://beis-ppr-$environment"
      case _ => "http://localhost:9000"
    }

    PublishConfig(
      s"$pprBase/$publishPath",
      s"$pprBase/$calculatePath",
      s"$pprBase/$questionnairePath"
    )
  }
}