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

import com.github.tminglei.slickpg.PgDateSupportJoda
import models.{CompaniesHouseId, FiledReport, ReportId}
import org.joda.time.LocalDate
import org.reactivestreams.Publisher
import play.api.db.slick.DatabaseConfigProvider
import services.ReportService
import slicks.DBBinding
import slicks.modules.ReportModule

import scala.concurrent.{ExecutionContext, Future}

class ReportTable @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends DBBinding
    with ReportService
    with ReportModule
    with ReportQueries
    with PgDateSupportJoda {

  import api._

  def filedReportByIdQ(id: Rep[ReportId]) = filedReportQuery.filter(_._1.id === id)

  val filedReportByIdC = Compiled(filedReportByIdQ _)

  def findFiled(id: ReportId): Future[Option[FiledReport]] = db.run {
    filedReportByIdC(id).result.headOption.map(_.map(FiledReport.tupled))
  }

  def reportByCoNoQ(companiesHouseId: Rep[CompaniesHouseId]) = filedReportQuery.filter(_._1.companyId === companiesHouseId)

  val reportByCoNoC = Compiled(reportByCoNoQ _)

  def byCompanyNumber(companiesHouseId: CompaniesHouseId): Future[Seq[FiledReport]] = db.run {
    reportByCoNoC(companiesHouseId).result.map(_.map(FiledReport.tupled))
  }

  /**
    * Code to adjust fetchSize on Postgres driver taken from:
    * https://engineering.sequra.es/2016/02/database-streaming-on-play-with-slick-from-publisher-to-chunked-result/
    */
  def list(cutoffDate: LocalDate): Publisher[FiledReport] = {
    val disableAutocommit = SimpleDBIO(_.connection.setAutoCommit(false))
    val action = filedReportQueryC.result.withStatementParameters(fetchSize = 10000)

    db.stream(disableAutocommit andThen action).mapResult(FiledReport.tupled)
  }
}
