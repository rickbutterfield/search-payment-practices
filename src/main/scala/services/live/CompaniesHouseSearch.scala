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

package services.live

import java.util.Base64
import javax.inject.Inject

import config.CompaniesHouseConfig
import models.{CompaniesHouseId, CompanyDetail, PagedResults}
import play.api.Logger
import play.api.libs.ws.WSClient
import services._

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class CompaniesHouseSearch @Inject()(val ws: WSClient, config: CompaniesHouseConfig, reportService: ReportService)(implicit val ec: ExecutionContext)
  extends RestService with CompanySearchService {

  import CompaniesHouseModels._

  private val basicAuth = "Basic " + new String(Base64.getEncoder.encode(config.apiKey.getBytes))

  def targetScope(companiesHouseId: CompaniesHouseId): String = s"https://api.companieshouse.gov.uk/company/${companiesHouseId.id}"

  // CoHo search api returns a 416 response if we try to retrieve results above number 400
  private val maxResultIndex = 100

  override def searchCompanies(search: String, page: Int, itemsPerPage: Int, timeout: Duration): Future[PagedResults[CompanySearchResult]] = {
    val s = views.html.helper.urlEncode(search)
    val maxPage = maxResultIndex / itemsPerPage
    val url = s"${config.getProtocol}://${config.getHostname}/search/companies?q=$s&items_per_page=$maxResultIndex"
    val start = System.currentTimeMillis()

    get[ResultsPage](url, basicAuth).map { resultsPage =>
      val t1 = System.currentTimeMillis() - start
      Logger.debug(s"Companies house search took ${t1}ms")

      var resultsWithReports = scala.concurrent.Await.result(
        Future.sequence(
          resultsPage.items.map(result => (result, reportService.countByCompanyNumber(result.company_number)))
            .map{case (result, count) => count.map{count2 => (result, count2)} }), timeout).filter(i => i._2 > 0)

      var results = resultsWithReports.drop((page - 1) * itemsPerPage).take(itemsPerPage)
        .map(i => CompanySearchResult(i._1.company_number, i._1.title, i._1.address_snippet, i._2))

      val t2 = System.currentTimeMillis() - start
      Logger.debug(s"Results with reports filter took ${t2}ms")

      PagedResults(results, resultsPage.items_per_page, resultsPage.page_number, resultsWithReports.size, resultLimit = Some(maxResultIndex))
    }
  }

  override def find(companiesHouseId: CompaniesHouseId): Future[Option[CompanyDetail]] = {
    val id = views.html.helper.urlEncode(companiesHouseId.id)
    val url = targetScope(companiesHouseId)

    getOpt[CompaniesHouseFindResult](url, basicAuth).map(_.map(r => CompanyDetail(r.company_number, r.company_name)))
  }
}
