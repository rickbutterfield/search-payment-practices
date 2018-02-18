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

package actions

import javax.inject.Inject

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * This action can be used by controllers that implement actions that are api endpoints. All it does
  * is to add the `Access-Control-Allow-Origin` to support CORS so that javascript applications in a
  * browser can call the endpoints.
  */
class ApiAction @Inject()(implicit ec: ExecutionContext) extends ActionBuilder[Request] {
  override def invokeBlock[A](request: Request[A], body: Request[A] => Future[Result]): Future[Result] =
    body(request).map(_.withHeaders("Access-Control-Allow-Origin" -> "*"))
}
