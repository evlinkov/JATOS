package general;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.route;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.google.inject.Injector;

import daos.common.StudyDao;
import daos.common.UserDao;
import exceptions.gui.JatosGuiException;
import general.common.Common;
import models.common.Study;
import models.common.User;
import play.Logger;
import play.api.mvc.RequestHeader;
import play.db.jpa.JPAApi;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Http.Cookies;
import play.mvc.Http.RequestBuilder;
import services.gui.BatchService;
import services.gui.StudyService;
import services.gui.StudyUploadUnmarshaller;
import services.gui.UploadUnmarshaller;
import services.gui.UserService;
import utils.common.IOUtils;
import utils.common.ZipUtil;

/**
 * @author Kristian Lange (2017)
 */
@Singleton
public class TestHelper {

	private static final String BASIC_EXAMPLE_STUDY_ZIP = "test/resources/basic_example_study.zip";

	@Inject
	private JPAApi jpaApi;

	@Inject
	private UserDao userDao;

	@Inject
	private StudyDao studyDao;

	@Inject
	private StudyService studyService;

	@Inject
	private UserService userService;

	@Inject
	private BatchService batchService;

	@Inject
	private IOUtils ioUtils;

	@Inject
	private Common common;

	public void removeStudyAssetsRootDir() throws IOException {
		File assetsRoot = new File(common.getStudyAssetsRootPath());
		if (assetsRoot.list() != null && assetsRoot.list().length > 0) {
			Logger.warn(TestHelper.class.getSimpleName()
					+ ".removeStudyAssetsRootDir: Study assets root directory "
					+ common.getStudyAssetsRootPath()
					+ " is not empty after finishing testing. This should not happen.");
		}
		FileUtils.deleteDirectory(assetsRoot);
	}

	public User getAdmin() {
		return jpaApi.withTransaction(() -> {
			User admin = userDao.findByEmail(UserService.ADMIN_EMAIL);
			return fetchTheLazyOnes(admin);
		});
	}

	/**
	 * Creates and persist user if an user with this email doesn't exist
	 * already.
	 */
	public User createAndPersistUser(String email, String name,
			String password) {
		return jpaApi.withTransaction(entityManager -> {
			User user = userDao.findByEmail(email);
			if (user == null) {
				user = new User(email, name);
				userService.createAndPersistUser(user, password);
			}
			return user;
		});
	}

	public Study createAndPersistExampleStudyForAdmin(Injector injector) {
		return jpaApi.withTransaction(() -> {
			User admin = userDao.findByEmail(UserService.ADMIN_EMAIL);
			Study exampleStudy;
			try {
				exampleStudy = importExampleStudy(admin, injector);
				studyService.createAndPersistStudy(admin, exampleStudy);
				return exampleStudy;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public Study importExampleStudy(User user, Injector injector)
			throws IOException {
		File studyZip = new File(BASIC_EXAMPLE_STUDY_ZIP);
		File tempUnzippedStudyDir = ZipUtil.unzip(studyZip);
		File[] studyFileList = ioUtils.findFiles(tempUnzippedStudyDir, "",
				IOUtils.STUDY_FILE_SUFFIX);
		File studyFile = studyFileList[0];
		UploadUnmarshaller<Study> uploadUnmarshaller = injector
				.getInstance(StudyUploadUnmarshaller.class);
		Study importedStudy = uploadUnmarshaller.unmarshalling(studyFile);
		studyFile.delete();

		File[] dirArray = ioUtils.findDirectories(tempUnzippedStudyDir);
		ioUtils.moveStudyAssetsDir(dirArray[0], importedStudy.getDirName());

		tempUnzippedStudyDir.delete();

		// Every study has a default batch
		importedStudy
				.addBatch(batchService.createDefaultBatch(importedStudy, user));
		return importedStudy;
	}

	public void removeStudy(Long studyId) throws IOException {
		jpaApi.withTransaction(() -> {
			try {
				Study study = studyDao.findById(studyId);
				if (study != null) {
					ioUtils.removeStudyAssetsDir(study.getDirName());
					studyService.remove(study);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public void removeAllStudies() {
		jpaApi.withTransaction(() -> {
			studyDao.findAll().forEach(study -> {
				try {
					ioUtils.removeStudyAssetsDir(study.getDirName());
					studyService.remove(study);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		});
	}

	/**
	 * Our custom ErrorHandler isn't used in test cases:
	 * https://github.com/playframework/playframework/issues/2484
	 * 
	 * So this method can be used to catch the RuntimeException and check
	 * manually that the correct JatosGuiException was thrown.
	 */
	public void assertJatosGuiException(RequestBuilder request,
			int httpStatus) {
		try {
			route(request);
		} catch (RuntimeException e) {
			assertThat(e.getCause()).isInstanceOf(JatosGuiException.class);
			JatosGuiException jatosGuiException = (JatosGuiException) e
					.getCause();
			assertThat(jatosGuiException.getSimpleResult().status())
					.isEqualTo(httpStatus);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T fetchTheLazyOnes(T obj) {
		Hibernate.initialize(obj);
		if (obj instanceof HibernateProxy) {
			obj = (T) ((HibernateProxy) obj).getHibernateLazyInitializer()
					.getImplementation();
		}
		return obj;
	}

	/**
	 * Mocks Play's Http.Context without cookies
	 */
	public void mockContext() {
		Cookies cookies = mock(Cookies.class);
		mockContext(cookies);
	}

	/**
	 * Mocks Play's Http.Context with one cookie that can be retrieved by
	 * cookies.get(name)
	 */
	public void mockContext(Cookie cookie) {
		Cookies cookies = mock(Cookies.class);
		when(cookies.get(cookie.name())).thenReturn(cookie);
		mockContext(cookies);
	}

	/**
	 * Mocks Play's Http.Context with cookies. The cookies can be retrieved by
	 * cookieList.iterator()
	 */
	public void mockContext(List<Cookie> cookieList) {
		Cookies cookies = mock(Cookies.class);
		when(cookies.iterator()).thenReturn(cookieList.iterator());
		mockContext(cookies);
	}

	private void mockContext(Cookies cookies) {
		Map<String, String> flashData = Collections.emptyMap();
		Map<String, Object> argData = Collections.emptyMap();
		Long id = 2L;
		RequestHeader header = mock(RequestHeader.class);
		Http.Request request = mock(Http.Request.class);
		when(request.cookies()).thenReturn(cookies);
		Http.Context context = new Http.Context(id, header, request, flashData,
				flashData, argData);
		Http.Context.current.set(context);
	}

}
