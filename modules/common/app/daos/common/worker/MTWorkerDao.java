package daos.common.worker;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;

import models.common.workers.MTSandboxWorker;
import models.common.workers.MTWorker;
import models.common.workers.Worker;
import play.db.jpa.JPAApi;

/**
 * DAO for MTWorker entity
 * 
 * @author Kristian Lange
 */
@Singleton
public class MTWorkerDao extends WorkerDao {

	@Inject
	MTWorkerDao(JPAApi jpa) {
		super(jpa);
	}

	/**
	 * Create MTWorker. Distinguishes between normal MechTurk and Sandbox
	 * MechTurk via mTurkSandbox parameter.
	 */
	public MTWorker create(String mtWorkerId, boolean mTurkSandbox) {
		MTWorker worker;
		if (mTurkSandbox) {
			worker = new MTSandboxWorker(mtWorkerId);
		} else {
			worker = new MTWorker(mtWorkerId);
		}
		persist(worker);
		return worker;
	}

	/**
	 * Retrieves the worker with the given MTurk worker ID in a case insensitive
	 * way.
	 */
	public MTWorker findByMTWorkerId(String mtWorkerId) {
		String queryStr = "SELECT w FROM Worker w WHERE "
				+ "upper(w.mtWorkerId)=:mtWorkerId";
		TypedQuery<Worker> query = jpa.em().createQuery(queryStr, Worker.class);
		query.setParameter("mtWorkerId", mtWorkerId);
		query.setMaxResults(1);
		List<Worker> workerList = query.getResultList();
		return workerList.isEmpty() ? null : (MTWorker) workerList.get(0);
	}

}
