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

import models.{Report, ReportId}
import org.joda.time.LocalDate
import utils.YesNo

import scala.language.implicitConversions

object ReportCSV {

  case class CSVString(s: String)

  val charsThatNeedQuoting  = Seq(',', '\n', '\r')
  val charsThatNeedDoubling = Seq('"')

  def quote(s: String): String = s""""$s""""

  def escape(s: String): String = s match {
    case _ if charsThatNeedDoubling.exists(s.contains(_)) => quote(charsThatNeedDoubling.foldLeft(s) { case (t, c) => t.replace(s"$c", s"$c$c") })
    case _ if charsThatNeedQuoting.exists(s.contains(_))  => quote(s)
    case _                                                => s
  }

  implicit def stringToCSVString(s: String): CSVString = CSVString(escape(s))

  implicit def intToCSVString(i: Int): CSVString = CSVString(i.toString)

  implicit def longToCSVString(l: Long): CSVString = CSVString(l.toString)

  implicit def decimalToCSVString(d: BigDecimal): CSVString = CSVString(d.toString)

  implicit def dateToCSVString(d: LocalDate): CSVString = CSVString(d.toString)

  implicit def booleanToCSVString(b: Boolean): CSVString = CSVString(b.toString)

  implicit def yesNoToCSVString(yn: YesNo): CSVString = yn.toBoolean

  implicit def optionToCSVString(o: Option[String]): CSVString = o.map(stringToCSVString).getOrElse(CSVString(""))

  implicit def optionIntToCSVString(o: Option[Int]): CSVString = o.map(intToCSVString).getOrElse(CSVString(""))

  implicit def optionYesNoToCSVString(o: Option[YesNo]): CSVString = o.map(yesNoToCSVString).getOrElse(CSVString(""))

  implicit def optionBooleanToCSVString(o: Option[Boolean]): CSVString = o.map(booleanToCSVString).getOrElse(CSVString(""))

  def columns(urlFunction: (ReportId => String)): Seq[(String, (Report) => CSVString)] = Seq[(String, Report => CSVString)](
    ("Report Id", _.id.id),
    ("Start date", _.reportDates.startDate),
    ("End date", _.reportDates.endDate),
    ("Filing date", _.filingDate),
    ("Company", _.companyName),
    ("Company number", _.companyId.id),
    ("Average time to pay", _.contractDetails.map(_.paymentHistory.averageDaysToPay)),
    ("% Invoices paid within 30 days", _.contractDetails.map(_.paymentHistory.percentageSplit.percentWithin30Days)),
    ("% Invoices paid between 31 and 60 days", _.contractDetails.map(_.paymentHistory.percentageSplit.percentWithin60Days)),
    ("% Invoices paid later than 60 days", _.contractDetails.map(_.paymentHistory.percentageSplit.percentBeyond60Days)),
    ("% Invoices not paid within agreed terms", _.contractDetails.map(_.paymentHistory.percentPaidLaterThanAgreedTerms)),
    ("Shortest (or only) standard payment period", _.contractDetails.map(_.paymentTerms.shortestPaymentPeriod)),
    ("Longest standard payment period", _.contractDetails.flatMap(_.paymentTerms.longestPaymentPeriod)),
    ("Maximum contractual payment period", _.contractDetails.map(_.paymentTerms.maximumContractPeriod)),
    ("Payment terms have changed", _.contractDetails.map(_.paymentTerms.paymentTermsChanged.comment.isDefined)),
    ("Suppliers notified of changes", _.contractDetails.flatMap(_.paymentTerms.paymentTermsChanged.notified.map(_.isDefined))),
    ("Participates in payment codes", _.paymentCodes.isDefined),
    ("E-Invoicing offered", _.contractDetails.map(_.offerEInvoicing)),
    ("Supply-chain financing offered", _.contractDetails.map(_.offerSupplyChainFinance)),
    ("Policy covers charges for remaining on supplier list", _.contractDetails.map(_.retentionChargesInPolicy)),
    ("Charges have been made for remaining on supplier list", _.contractDetails.map(_.retentionChargesInPast)),
    ("URL", report => urlFunction(report.id))
  )
}
