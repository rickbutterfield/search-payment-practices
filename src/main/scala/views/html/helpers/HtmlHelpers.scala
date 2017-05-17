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

package views.html.helpers

import forms.report.ConditionalText
import org.joda.time.LocalDate
import play.twirl.api.{Html, HtmlFormat}
import utils.YesNo
import utils.YesNo.{No, Yes}

import scala.language.implicitConversions

/**
  * Various converters to reduce boilerplate in the table and row descriptors
  */
trait HtmlHelpers {
  def limitLength(s: String, maxLength: Int = 1000) =
    if (s.length <= maxLength) s else s"${s.take(maxLength)}..."

  def yesNo(yn: YesNo): String = yn.entryName.capitalize

  def breakLines(s: String): Html = Html(HtmlFormat.escape(limitLength(s)).toString.replace("\n", "<br />"))

  def conditionalText(ct: ConditionalText): Html = ct.yesNo match {
    case Yes => Html(s"<strong>Yes </strong>&ndash; ${breakLines(ct.text.map(limitLength(_)).getOrElse(""))}")
    case No => Html("<strong>No</strong>")
  }

  implicit def stringToHtml(s: String): Html = HtmlFormat.escape(limitLength(s))

  implicit def intToHtml(i: Int): Html = Html(i.toString)

  /**
    * Slightly hacky. Take a pair with an int and a string that contains the units (e.g. "days" or "%")
    * and format them up. would be better to have strong types representing days and percentages.
    */
  implicit def unitsToHtml(p: (Int, String)): Html = Html(s"${p._1} ${p._2}")

  implicit def dateToHtml(d: LocalDate): Html = Html(d.toString)

  implicit def yesNoToHtml(yn: YesNo): Html = Html(yesNo(yn))

  implicit def optionToHtml(o: Option[String]): Html = Html(o.map(limitLength(_)).getOrElse(""))

  implicit def optionHtmlToHtml(o: Option[Html]): Html = o.getOrElse(Html(""))
}

object HtmlHelpers extends HtmlHelpers