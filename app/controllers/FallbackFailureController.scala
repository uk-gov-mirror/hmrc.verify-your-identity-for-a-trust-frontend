/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import handlers.ErrorHandler
import javax.inject.Inject
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.Session

class FallbackFailureController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        errorHandler: ErrorHandler
                                      ) extends FrontendBaseController with I18nSupport {

  private val logger = Logger(getClass)

  def onPageLoad(): Action[AnyContent] = Action {
    implicit request =>
      logger.error(s"[Verifying][Trust IV][Session ID: ${Session.id(hc)}] Trust IV encountered a problem that could not be recovered from")
      InternalServerError(errorHandler.internalServerErrorTemplate)
  }
}
