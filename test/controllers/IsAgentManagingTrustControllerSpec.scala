/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.IsAgentManagingTrustFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{IsAgentManagingTrustPage, IdentifierPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{FakeRelationshipEstablishmentService, RelationshipNotFound}
import views.html.IsAgentManagingTrustView

import scala.concurrent.Future

class IsAgentManagingTrustControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new IsAgentManagingTrustFormProvider()
  val form = formProvider()
  val utr = "0987654321"

  lazy val isAgentManagingTrustRoute = controllers.routes.IsAgentManagingTrustController.onPageLoad(NormalMode).url

  val fakeEstablishmentServiceFailing = new FakeRelationshipEstablishmentService(RelationshipNotFound)

  "IsAgentManagingTrust Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .set(IdentifierPage, utr)
        .success
        .value

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        relationshipEstablishment = fakeEstablishmentServiceFailing ).build()

      val request = FakeRequest(GET, isAgentManagingTrustRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[IsAgentManagingTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, utr)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingTrustPage, true)
        .success.value
        .set(IdentifierPage, utr)
        .success.value

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        relationshipEstablishment = fakeEstablishmentServiceFailing).build()

      val request = FakeRequest(GET, isAgentManagingTrustRoute)

      val view = application.injector.instanceOf[IsAgentManagingTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), NormalMode, utr)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), relationshipEstablishment = fakeEstablishmentServiceFailing)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, isAgentManagingTrustRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(IdentifierPage, utr)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers),
        relationshipEstablishment = fakeEstablishmentServiceFailing)
        .build()

      val request =
        FakeRequest(POST, isAgentManagingTrustRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[IsAgentManagingTrustView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, utr)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, fakeEstablishmentServiceFailing).build()

      val request = FakeRequest(GET, isAgentManagingTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, fakeEstablishmentServiceFailing).build()

      val request =
        FakeRequest(POST, isAgentManagingTrustRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
