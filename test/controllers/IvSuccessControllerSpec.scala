/*
 * Copyright 2019 HM Revenue & Customs
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

import base.SpecBase
import connectors.TaxEnrolmentsConnector
import models.{TaxEnrolmentsRequest, UserAnswers}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{IsAgentManagingTrustPage, UtrPage}
import play.api.inject.bind
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.RelationshipEstablishment
import uk.gov.hmrc.http.HttpResponse
import views.html.IvSuccessView

import scala.concurrent.Future

class IvSuccessControllerSpec extends SpecBase with BeforeAndAfterAll {

  private val utr = "0987654321"

  private val connector = mock[TaxEnrolmentsConnector]
  private val service = mock[RelationshipEstablishment]

  "IvSuccess Controller" must {

    "return OK and the correct view for a GET with no Agent" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingTrustPage, false).success.value
        .set(UtrPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = service)
        .overrides(Seq(
          bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
        ))
        .build()

      val request = FakeRequest(GET, routes.IvSuccessController.onPageLoad().url)

      val view = application.injector.instanceOf[IvSuccessView]

      val viewAsString = view(isAgent = false, utr)(fakeRequest, messages).toString

      when(service.check(eqTo("id"), eqTo(utr), any(), any())(any()))
        .thenReturn(Future.successful(Results.Ok(viewAsString)))

      when(connector.enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verify(connector).enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any())
      verify(service).check(eqTo("id"), eqTo(utr), any(), any())(any())

      reset(connector)
      reset(service)

      application.stop()

    }

    "return OK and the correct view for a GET with Agent" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingTrustPage, true).success.value
        .set(UtrPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = service)
        .overrides(Seq(
          bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
        ))
        .build()

      val request = FakeRequest(GET, routes.IvSuccessController.onPageLoad().url)

      val view = application.injector.instanceOf[IvSuccessView]

      val viewAsString = view(isAgent = true, utr)(fakeRequest, messages).toString

      when(service.check(eqTo("id"), eqTo(utr), any(), any())(any()))
        .thenReturn(Future.successful(Results.Ok(viewAsString)))

      when(connector.enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verify(connector).enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any())
      verify(service).check(eqTo("id"), eqTo(utr), any(), any())(any())

      reset(connector)
      reset(service)

      application.stop()

    }


    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.IvSuccessController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()

    }

  }
}
