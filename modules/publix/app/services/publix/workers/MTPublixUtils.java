package services.publix.workers;

import javax.inject.Inject;
import javax.inject.Singleton;

import daos.common.BatchDao;
import daos.common.ComponentDao;
import daos.common.ComponentResultDao;
import daos.common.StudyDao;
import daos.common.StudyResultDao;
import daos.common.worker.WorkerDao;
import exceptions.publix.ForbiddenPublixException;
import models.common.workers.MTWorker;
import models.common.workers.Worker;
import services.publix.PublixUtils;
import services.publix.ResultCreator;
import services.publix.group.GroupService;
import services.publix.idcookie.IdCookieService;

/**
 * MTPublix' implementation of PublixUtils (studies started via MTurk).
 * 
 * @author Kristian Lange
 */
@Singleton
public class MTPublixUtils extends PublixUtils<MTWorker> {

	@Inject
	MTPublixUtils(ResultCreator resultCreator, IdCookieService idCookieService,
			GroupService groupService, MTErrorMessages errorMessages,
			StudyDao studyDao, StudyResultDao studyResultDao,
			ComponentDao componentDao, ComponentResultDao componentResultDao,
			WorkerDao workerDao, BatchDao batchDao) {
		super(resultCreator, idCookieService, groupService, errorMessages,
				studyDao, studyResultDao, componentDao, componentResultDao,
				workerDao, batchDao);
	}

	@Override
	public MTWorker retrieveTypedWorker(Long workerId)
			throws ForbiddenPublixException {
		Worker worker = super.retrieveWorker(workerId);
		if (!(worker instanceof MTWorker)) {
			throw new ForbiddenPublixException(
					errorMessages.workerNotCorrectType(worker.getId()));
		}
		return (MTWorker) worker;
	}

}
