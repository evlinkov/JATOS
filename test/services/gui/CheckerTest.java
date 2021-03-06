package services.gui;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import org.fest.assertions.Fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import exceptions.gui.BadRequestException;
import exceptions.gui.ForbiddenException;
import general.TestHelper;
import general.common.MessagesStrings;
import models.common.Component;
import models.common.Study;
import models.common.User;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

/**
 * Tests Checker
 * 
 * @author Kristian Lange
 */
public class CheckerTest {

	private Injector injector;

	@Inject
	private TestHelper testHelper;

	@Inject
	private Checker checker;

	@Before
	public void startApp() throws Exception {
		GuiceApplicationBuilder builder = new GuiceApplicationLoader()
				.builder(new ApplicationLoader.Context(Environment.simple()));
		injector = Guice.createInjector(builder.applicationModule());
		injector.injectMembers(this);
	}

	@After
	public void stopApp() throws Exception {
		// Clean up
		testHelper.removeAllStudies();
		testHelper.removeStudyAssetsRootDir();
	}

	@Test
	public void simpleCheck() {
		int a = 1 + 1;
		assertThat(a).isEqualTo(2);
	}

	/**
	 * Test Checker.checkStandardForComponents()
	 */
	@Test
	public void checkCheckStandardForComponents()
			throws NoSuchAlgorithmException, IOException {
		Study study = testHelper.createAndPersistExampleStudyForAdmin(injector);
		Component component = study.getFirstComponent();

		try {
			checker.checkStandardForComponents(study.getId(), component.getId(),
					component);
		} catch (BadRequestException e) {
			Fail.fail();
		}

		long nonExistentStudyId = 11111l;
		try {
			checker.checkStandardForComponents(nonExistentStudyId,
					component.getId(), component);
			Fail.fail();
		} catch (BadRequestException e) {
			assertThat(e.getMessage())
					.isEqualTo(MessagesStrings.componentNotBelongToStudy(
							nonExistentStudyId, component.getId()));
		}

		component.setStudy(null);
		try {
			checker.checkStandardForComponents(study.getId(), component.getId(),
					component);
			Fail.fail();
		} catch (BadRequestException e) {
			assertThat(e.getMessage()).isEqualTo(
					MessagesStrings.componentHasNoStudy(component.getId()));
		}

		component = null;
		try {
			checker.checkStandardForComponents(study.getId(), null, component);
			Fail.fail();
		} catch (BadRequestException e) {
			assertThat(e.getMessage())
					.isEqualTo(MessagesStrings.componentNotExist(null));
		}
	}

	@Test
	public void testCheckStudyLocked() {
		Study study = testHelper.createAndPersistExampleStudyForAdmin(injector);

		try {
			checker.checkStudyLocked(study);
		} catch (ForbiddenException e) {
			Fail.fail();
		}

		study.setLocked(true);
		try {
			checker.checkStudyLocked(study);
			Fail.fail();
		} catch (ForbiddenException e) {
			assertThat(e.getMessage())
					.isEqualTo(MessagesStrings.studyLocked(study.getId()));
		}
	}

	@Test
	public void testCheckStandardForStudy() {
		User admin = testHelper.getAdmin();

		try {
			checker.checkStandardForStudy(null, 1l, admin);
			Fail.fail();
		} catch (ForbiddenException e) {
			Fail.fail();
		} catch (BadRequestException e) {
			assertThat(e.getMessage())
					.isEqualTo(MessagesStrings.studyNotExist(1l));
		}

		Study study = testHelper.createAndPersistExampleStudyForAdmin(injector);
		try {
			checker.checkStandardForStudy(study, study.getId(), admin);
		} catch (ForbiddenException e) {
			Fail.fail();
		} catch (BadRequestException e) {
			Fail.fail();
		}

		study.getUserList().remove(admin);
		try {
			checker.checkStandardForStudy(study, study.getId(), admin);
			Fail.fail();
		} catch (ForbiddenException e) {
			assertThat(e.getMessage())
					.isEqualTo(MessagesStrings.studyNotUser(admin.getName(),
							admin.getEmail(), study.getId(), study.getTitle()));
		} catch (BadRequestException e) {
			Fail.fail();
		}
	}

	@Test
	public void testCheckUserLoggedIn() {
		User admin = testHelper.getAdmin();
		try {
			checker.checkUserLoggedIn(admin, admin);
		} catch (ForbiddenException e) {
			Fail.fail();
		}

		User testUser = testHelper.createAndPersistUser("bla@bla.com", "Bla",
				"bla");
		try {
			checker.checkUserLoggedIn(testUser, admin);
			Fail.fail();
		} catch (ForbiddenException e) {
			assertThat(e.getMessage()).isEqualTo(
					MessagesStrings.userMustBeLoggedInToSeeProfile(testUser));
		}
	}

}
