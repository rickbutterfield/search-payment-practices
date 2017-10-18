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

import cats.data.OptionT
import cats.instances.future._
import config.PageConfig
import models.{CompaniesHouseId, PagedResults, Report, ReportId}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Result}
import play.twirl.api.Html
import services._

import scala.concurrent.ExecutionContext

class SearchController @Inject()(
  val companySearch: CompanySearchService,
  val reportService: ReportService,
  val pageConfig: PageConfig
)(implicit val ec: ExecutionContext)
  extends Controller
    with PageHelper
    with SearchHelper {

  private val df = DateTimeFormat.forPattern("d MMMM YYYY")

  def start() = Action { implicit request =>
    Ok(page("Search payment practice reports")(views.html.search.start()))
  }

  private val searchForReports = "Search for published payment practice reports"
  private val searchHeader     = h1(searchForReports)
  private val searchLink       = routes.SearchController.search(None, None, None).url
  private val searchPageTitle  = "Search for published payment practice reports"

  private def companyLink(id: CompaniesHouseId, pageNumber: Option[Int]) =
    routes.SearchController.company(id, pageNumber).url

  private def pageLink(query: Option[String], itemsPerPage: Option[Int], pageNumber: Int) =
    routes.SearchController.search(query, Some(pageNumber), itemsPerPage).url

  //noinspection TypeAnnotation
  def search(query: Option[String], pageNumber: Option[Int], itemsPerPage: Option[Int]) = Action.async { implicit request =>
    def resultsPage(q: String, results: Option[PagedResults[CompanySearchResult]], countMap: Map[CompaniesHouseId, Int]): Html =
      page(searchPageTitle)(views.html.search.search(searchHeader, q, results, countMap, searchLink, companyLink(_, pageNumber), pageLink(query, itemsPerPage, _), er))

    doSearch(query, pageNumber, itemsPerPage, resultsPage).map(Ok(_))
  }

  //noinspection TypeAnnotation
  def company(companiesHouseId: CompaniesHouseId, pageNumber: Option[Int]) = Action.async { implicit request =>
    val pageLink = { i: Int => routes.SearchController.company(companiesHouseId, Some(i)).url }
    val result = for {
      co <- OptionT(companySearch.find(companiesHouseId))
      rs <- OptionT.liftF(reportService.byCompanyNumber(companiesHouseId).map(rs => PagedResults.page(rs, pageNumber.getOrElse(1))))
    } yield {
      Ok(page(s"Payment practice reports for ${co.companyName}")(home, views.html.search.company(co, rs, pageLink, df)))
    }

    result.value.map {
      case Some(r) => r
      case None    => NotFound
    }
  }

  //noinspection TypeAnnotation
  def view(reportId: ReportId) = Action.async { implicit request =>
    val f = for {
      report <- OptionT(reportService.find(reportId))
    } yield {
      if (request.acceptedTypes.exists(_.accepts("application/json"))) renderJson(report)
      else renderHtml(report)
    }

    f.value.map {
      case Some(ok) => ok
      case None     => NotFound
    }
  }

  private def renderJson(report: Report): Result = {
    Ok(Json.toJson(report))
  }

  private def renderHtml(report: Report)(implicit pageContext:PageContext): Result = {
    val companyCrumb = Breadcrumb(routes.SearchController.company(report.companyId, None), s"${report.companyName} reports")
    val crumbs = breadcrumbs(homeBreadcrumb, companyCrumb)
    Ok(page(s"Payment practice report for ${report.companyName}")(crumbs, views.html.search.report(report, df)))
  }
}
