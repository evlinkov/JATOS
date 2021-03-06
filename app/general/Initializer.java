package general;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import daos.common.UserDao;
import general.common.Common;
import models.common.User;
import play.Logger;
import play.Logger.ALogger;
import play.db.jpa.JPAApi;
import services.gui.UserService;

/**
 * This Initializer is called once with every start of JATOS and does some JATOS
 * specific initialisation.
 * 
 * @author Kristian Lange
 */
@Singleton
public class Initializer {

	private static final ALogger LOGGER = Logger.of(Initializer.class);

	private final JPAApi jpa;
	private final Common common;
	private final UserService userService;
	private final UserDao userDao;

	@Inject
	Initializer(JPAApi jpa, Common common, UserDao userDao,
			UserService userService) {
		this.jpa = jpa;
		this.common = common;
		this.userDao = userDao;
		this.userService = userService;
		initialize();
	}

	/**
	 * This method is called once with every start and does some health checks
	 * or DB updates.
	 */
	public void initialize() {
		checkAdmin();
		checkStudyAssetsRootDir();
		// checkGroupResults();
		LOGGER.info("JATOS initialized");
	}

	/**
	 * Check whether studies assets root directory exists and create if not.
	 */
	private void checkStudyAssetsRootDir() {
		File studyAssetsRoot = new File(common.getStudyAssetsRootPath());
		boolean success = studyAssetsRoot.mkdirs();
		if (success) {
			LOGGER.info(
					".checkStudyAssetsRootDir: Created study assets root directory "
							+ common.getStudyAssetsRootPath());
		}
		if (!studyAssetsRoot.isDirectory()) {
			LOGGER.error(
					".checkStudyAssetsRootDir: Study assets root directory "
							+ common.getStudyAssetsRootPath()
							+ " couldn't be created.");
		}
	}

	/**
	 * Check for user admin: In case the application is started the first time
	 * we need an initial user: admin. If admin can't be found, create one.
	 */
	private void checkAdmin() {
		jpa.withTransaction(() -> {
			User admin = userDao.findByEmail(UserService.ADMIN_EMAIL);
			if (admin == null) {
				userService.createAndPersistAdmin();
			}
		});
	}

}
