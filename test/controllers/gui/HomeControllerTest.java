package controllers.gui;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;

import general.TestHelper;
import models.common.User;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.mvc.Http;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;
import services.gui.BreadcrumbsService;

/**
 * Testing actions of controller.gui.Home.
 * 
 * @author Kristian Lange
 */
public class HomeControllerTest {

	@Inject
	private static Application fakeApplication;

	@Inject
	private TestHelper testHelper;

	@Before
	public void startApp() throws Exception {
		fakeApplication = Helpers.fakeApplication();

		GuiceApplicationBuilder builder = new GuiceApplicationLoader()
				.builder(new ApplicationLoader.Context(Environment.simple()));
		Guice.createInjector(builder.applicationModule()).injectMembers(this);

		Helpers.start(fakeApplication);
	}

	@After
	public void stopApp() throws Exception {
		// Clean up
		testHelper.removeAllStudies();

		Helpers.stop(fakeApplication);
		testHelper.removeStudyAssetsRootDir();
	}

	@Test
	public void callHome() throws Exception {
		User admin = testHelper.getAdmin();
		RequestBuilder request = new RequestBuilder().method("GET")
				.session(Users.SESSION_EMAIL, admin.getEmail())
				.uri(controllers.gui.routes.Home.home().url());
		Result result = route(request);

		assertThat(result.status()).isEqualTo(OK);
		assertThat(result.charset().get()).isEqualTo("utf-8");
		assertThat(result.contentType().get()).isEqualTo("text/html");
		assertThat(contentAsString(result)).contains(BreadcrumbsService.HOME);
	}

	@Test
	public void callLog() throws Exception {
		User admin = testHelper.getAdmin();
		RequestBuilder request = new RequestBuilder().method("GET")
				.session(Users.SESSION_EMAIL, admin.getEmail())
				.uri(controllers.gui.routes.Home.log(1000).url());
		Result result = route(request);

		assertThat(result.status()).isEqualTo(OK);
		assertThat(result.charset().get()).isEqualTo("utf-8");
		assertThat(result.contentType().get()).isEqualTo("text/plain");
		assertThat(result.body()).isNotNull();
	}

	@Test
	public void callLogNotAsAdmin() throws Exception {
		User notAdminUser = testHelper
				.createAndPersistUser("bla@bla.com", "Bla", "bla");
		RequestBuilder request = new RequestBuilder().method("GET")
				.session(Users.SESSION_EMAIL, notAdminUser.getEmail())
				.uri(controllers.gui.routes.Home.log(1000).url());
		testHelper.assertJatosGuiException(request,
				Http.Status.FORBIDDEN);
	}

}
