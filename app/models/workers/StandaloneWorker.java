package models.workers;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Worker if a study is run as standalone (not from MTurk)
 * 
 * @author Kristian Lange
 */
@Entity
@DiscriminatorValue(StandaloneWorker.WORKER_TYPE)
public class StandaloneWorker extends Worker {

	public static final String WORKER_TYPE = "Standalone";

	/**
	 * Worker's name or other identification
	 */
	private String name;
	
	public StandaloneWorker() {
	}
	
	@JsonCreator
	public StandaloneWorker(String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return name + ", " + super.toString();
	}

	@Override
	public String generateConfirmationCode() {
		return null;
	}

}
