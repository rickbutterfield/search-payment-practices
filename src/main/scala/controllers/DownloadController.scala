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

import javax.inject.Inject

import akka.NotUsed
import akka.stream.scaladsl.{Concat, Flow, Source}
import akka.util.ByteString
import config.PageConfig
import models.{Report, ReportId}
import org.joda.time.LocalDate
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc._
import services.ReportService

class DownloadController @Inject()(
  reportService: ReportService,
  val pageConfig: PageConfig
) extends Controller with PageHelper {

  def show = Action { implicit request =>
    Ok(page("Export data for published reports")(home, views.html.download.accessData(er)))
  }

  private def forwardedFromHttps(implicit rh: RequestHeader): Boolean = {
    rh.headers.get("X-Forwarded-Proto") match {
      case Some("https") => true
      case _             => false
    }
  }

  def export = Action { implicit request =>
    val urlFunction = { reportId: ReportId =>
      routes.SearchController.view(reportId).absoluteURL(request.secure || forwardedFromHttps)
    }

    val disposition = ("Content-Disposition", "attachment;filename=payment-practices.csv")

    val publisher = reportService.list(LocalDate.now().minusMonths(24))

    val headerSource = Source.single(ReportCSV.columns(urlFunction).map(_._1).mkString(","))
    val rowSource = Source.fromPublisher(publisher).map(toCsv(_, urlFunction))
    val csvSource = Source.combine[String, String](headerSource, rowSource)(_ => Concat()).map(ByteString(_))

    val entity = HttpEntity.Streamed(csvSource, None, Some("text/csv"))
    Result(ResponseHeader(OK, Map()), entity).withHeaders(disposition)
  }

  def toCsv(row: Report, urlFunction: ReportId => String): String = "\n" + ReportCSV.columns(urlFunction).map(_._2(row).s).mkString(",")

  def json = Action { implicit request =>
    val publisher = reportService.list(LocalDate.now().minusMonths(24))

    val rowSource = Source.fromPublisher(publisher)
      .map(row => ByteString(Json.prettyPrint(Json.toJson(row))))
      .via(Flow[ByteString].intersperse(ByteString("["), ByteString(","), ByteString("]")))

    val entity = HttpEntity.Streamed(rowSource, None, Some("application/json"))
    Result(ResponseHeader(OK, Map()), entity)
  }
}
