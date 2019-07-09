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

package services

import com.google.inject.ImplementedBy
import models.{CompaniesHouseId, Report, ReportId}
import org.joda.time.{LocalDate, LocalDateTime}
import org.reactivestreams.Publisher
import slicks.repos.ReportTable

import scala.concurrent.Future

@ImplementedBy(classOf[ReportTable])
trait ReportService {
  def find(id: ReportId): Future[Option[Report]]
  def byCompanyNumber(companiesHouseId: CompaniesHouseId): Future[Seq[Report]]
  def countByCompanyNumber(companiesHouseId: CompaniesHouseId): Future[Int]
  def list(cutoffDate: LocalDate): Publisher[Report]
  def count: Future[Int]
}
