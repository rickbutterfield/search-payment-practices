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

package slicks.repos

import javax.inject.Inject

import dbrows.CommentRow
import models.{CommentId, CompaniesHouseId, Report, ReportId}
import org.joda.time.{LocalDate, LocalDateTime}
import org.reactivestreams.Publisher
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import services.ReportService
import slick.jdbc.JdbcProfile
import slicks.modules.{CoreModule, ReportModule}

import scala.concurrent.{ExecutionContext, Future}

class ReportTable @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends CoreModule
    with ReportService
    with ReportModule
    with ReportQueries
    with HasDatabaseConfig[JdbcProfile] {

  override lazy val dbConfig = dbConfigProvider.get[JdbcProfile]

  import profile.api._

  //noinspection TypeAnnotation
  def activeReportByIdQ(reportId: Rep[ReportId]) = activeReportQuery.filter(_._1.id === reportId)

  val activeReportByIdC   = Compiled(activeReportByIdQ _)

  override def find(id: ReportId): Future[Option[Report]] = db.run {
    activeReportByIdC(id).result.headOption.map(_.map(Report.apply))
  }

  //noinspection TypeAnnotation
  def activeReportByCoNoQ(cono: Rep[CompaniesHouseId]) = activeReportQuery.filter(_._1.companyId === cono)

  val reportByCoNoC = Compiled(activeReportByCoNoQ _)

  override def byCompanyNumber(companiesHouseId: CompaniesHouseId): Future[Seq[Report]] = db.run {
    reportByCoNoC(companiesHouseId).result.map(_.map(Report.apply))
  }

  /**
    * Code to adjust fetchSize on Postgres driver taken from:
    * https://engineering.sequra.es/2016/02/database-streaming-on-play-with-slick-from-publisher-to-chunked-result/
    */
  override def list(cutoffDate: LocalDate): Publisher[Report] = {
    val disableAutocommit = SimpleDBIO(_.connection.setAutoCommit(false))
    val action = activeReportQueryC.result.withStatementParameters(fetchSize = 10000)

    db.stream(disableAutocommit andThen action).mapResult(Report.apply)
  }

  override def count: Future[Int] = db.run(activeReportQuery.length.result)
}
