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

import config.ApiConfig
import play.api.Logger
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

class ProtectedApiAction @Inject()(apiConfig: ApiConfig)(implicit ec: ExecutionContext) extends ActionBuilder[Request] {

  import Results.Unauthorized

  override def invokeBlock[A](request: Request[A], body: Request[A] => Future[Result]): Future[Result] = {
    Logger.debug(s"configured api token is: ${apiConfig.token}")
    val auth = request.headers.get("Authorization")
    Logger.debug(s"auth header is $auth")
    (auth, apiConfig.token) match {
      case (Some(Bearer(suppliedToken)), Some(configuredToken)) if suppliedToken == configuredToken =>
        body(request).map(_.withHeaders("Access-Control-Allow-Origin" -> "*"))

      case _ => Future.successful(Unauthorized)
    }
  }
}

object Bearer {
  private val BearerExpr = "Bearer ([\\w.~+/-]+)".r

  def unapply(s: String): Option[String] = s match {
    case BearerExpr(token) => Some(token)
    case _                 => None
  }
}