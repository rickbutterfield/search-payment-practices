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

package models

import com.wellfactored.playbindings.ValueClassFormats
import dbrows.{ContractDetailsRow, ReportRow}
import forms.DateRange
import forms.report._
import org.joda.time.LocalDate
import play.api.libs.json.{Json, OWrites}
import utils.YesNo

case class Report(
  id: ReportId,
  companyName: String,
  companyId: CompaniesHouseId,
  filingDate: LocalDate,

  approvedBy: String,
  confirmationEmailAddress: String,

  reportDates: DateRange,
  paymentCodes: ConditionalText,

  contractDetails: Option[ContractDetails]
)

object Report extends ValueClassFormats {
  implicit val write: OWrites[Report] = Json.writes[Report]

  def apply(r: (ReportRow, Option[ContractDetailsRow])): Report = {
    val (reportRow, contractDetailsRow) = r
    import reportRow._
    Report(
      id,
      companyName,
      companyId,
      filingDate,
      approvedBy,
      confirmationEmailAddress,
      DateRange(startDate, endDate),
      ConditionalText(paymentCodes),
      contractDetailsRow.map(buildContractDetails)
    )
  }

  def buildContractDetails(row: ContractDetailsRow): ContractDetails = {
    import row._
    ContractDetails(
      PaymentTerms(
        shortestPaymentPeriod: Int,
        longestPaymentPeriod: Option[Int],
        paymentTerms,
        maximumContractPeriod,
        maximumContractPeriodComment,
        PaymentTermsChanged(ConditionalText(paymentTermsChangedComment), Some(ConditionalText(paymentTermsChangedNotifiedComment))).normalise,
        paymentTermsComment,
        disputeResolution
      ),
      PaymentHistory(averageDaysToPay, percentPaidLaterThanAgreedTerms, PercentageSplit(percentInvoicesWithin30Days, percentInvoicesWithin60Days, percentInvoicesBeyond60Days)),
      offerEInvoicing,
      offerSupplyChainFinance,
      retentionChargesInPolicy,
      retentionChargesInPast
    )
  }
}

case class ContractDetails(
  paymentTerms: PaymentTerms,
  paymentHistory: PaymentHistory,
  offerEInvoicing: YesNo,
  offerSupplyChainFinance: YesNo,
  retentionChargesInPolicy: YesNo,
  retentionChargesInPast: YesNo
)

object ContractDetails {
  implicit def writes: OWrites[ContractDetails] = Json.writes[ContractDetails]
}