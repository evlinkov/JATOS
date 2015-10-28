package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.workers.JatosWorker;
import models.workers.PersonalMultipleWorker;
import models.workers.PersonalSingleWorker;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import play.data.validation.ValidationError;
import utils.IOUtils;
import utils.JsonUtils;
import utils.MessagesStrings;

/**
 * Model of study and group properties for UI (not persisted in DB)
 * 
 * @author Kristian Lange
 */
public class StudyProperties {

	public static final String STUDY_ID = "studyId";
	public static final String UUID = "uuid";
	public static final String TITLE = "title";
	public static final String JSON_DATA = "jsonData";
	public static final String DESCRIPTION = "description";
	public static final String DIRNAME = "dirName";
	public static final String COMMENTS = "comments";
	public static final String GROUP_STUDY = "groupStudy";
	public static final String ALLOWED_WORKER_TYPE = "allowedWorkerType";
	public static final String ALLOWED_WORKER_TYPE_LIST = "allowedWorkerTypeList";
	public static final String GROUP_ID = "groupId";
	public static final String MIN_ACTIVE_MEMBER_SIZE = "minActiveMemberSize";
	public static final String MAX_ACTIVE_MEMBER_SIZE = "maxActiveMemberSize";
	public static final String MAX_TOTAL_MEMBER_SIZE = "maxTotalMemberSize";

	private Long studyId;

	/**
	 * Universally (world-wide) unique ID. Used for import/export between
	 * different JATOS instances. On one JATOS instance it is only allowed to
	 * have one study with the same UUID.
	 */
	private String uuid;

	private String title;

	private String description;

	/**
	 * Timestamp of the creation or the last update of this study
	 */
	private Timestamp date;

	/**
	 * If a study is locked, it can't be changed.
	 */
	private boolean locked = false;

	/**
	 * List of worker types that are allowed to run this study. If the worker
	 * type is not in this list, it has no permission to run this study.
	 */
	private Set<String> allowedWorkerTypeList = new HashSet<>();

	/**
	 * Study assets directory name
	 */
	private String dirName;

	/**
	 * User comments, reminders, something to share with others. They have no
	 * further meaning.
	 */
	private String comments;

	/**
	 * Data in JSON format that are responded after public APIs 'getData' call.
	 */
	private String jsonData;

	/**
	 * Is this a group study with several workers running it at once.
	 */
	private boolean groupStudy = false;

	private Long groupId;

	/**
	 * Minimum number of workers in the group that are active at the same time.
	 * It's at least 2.
	 */
	private int minActiveMemberSize = 2;

	/**
	 * Maximum number of workers in the group that are active at the same time.
	 * It's at least 2.
	 */
	private int maxActiveMemberSize = 2;

	/**
	 * Maximum number of workers that are allowed to be member in the group
	 * altogether (active and inactive together). It's at least 2.
	 */
	private int maxTotalMemberSize = 2;

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public Long getStudyId() {
		return this.studyId;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	public String getDirName() {
		return this.dirName;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getComments() {
		return this.comments;
	}

	public void setDate(Timestamp timestamp) {
		this.date = timestamp;
	}

	public Timestamp getDate() {
		return this.date;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return this.locked;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public boolean isGroupStudy() {
		return groupStudy;
	}

	public void setGroupStudy(boolean groupStudy) {
		this.groupStudy = groupStudy;
	}

	public void setAllowedWorkerTypeList(Set<String> allowedWorkerTypeList) {
		this.allowedWorkerTypeList = allowedWorkerTypeList;
	}

	public Set<String> getAllowedWorkerTypeList() {
		return this.allowedWorkerTypeList;
	}

	public void addAllowedWorkerType(String workerType) {
		allowedWorkerTypeList.add(workerType);
	}

	public void removeAllowedWorkerType(String workerType) {
		allowedWorkerTypeList.remove(workerType);
	}

	public boolean hasAllowedWorkerType(String workerType) {
		return allowedWorkerTypeList.contains(workerType);
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public int getMinActiveMemberSize() {
		return minActiveMemberSize;
	}

	public void setMinActiveMemberSize(int minActiveMemberSize) {
		this.minActiveMemberSize = minActiveMemberSize;
	}

	public int getMaxActiveMemberSize() {
		return maxActiveMemberSize;
	}

	public void setMaxActiveMemberSize(int maxActiveMemberSize) {
		this.maxActiveMemberSize = maxActiveMemberSize;
	}

	public int getMaxTotalMemberSize() {
		return maxTotalMemberSize;
	}

	public void setMaxTotalMemberSize(int maxTotalMemberSize) {
		this.maxTotalMemberSize = maxTotalMemberSize;
	}

	/**
	 * Add default allowed workers
	 */
	public StudyProperties initStudyProperties() {
		addAllowedWorkerType(JatosWorker.WORKER_TYPE);
		addAllowedWorkerType(PersonalMultipleWorker.WORKER_TYPE);
		addAllowedWorkerType(PersonalSingleWorker.WORKER_TYPE);
		return this;
	}

	public List<ValidationError> validate() {
		List<ValidationError> errorList = new ArrayList<>();
		if (title == null || title.trim().isEmpty()) {
			errorList.add(new ValidationError(TITLE,
					MessagesStrings.MISSING_TITLE));
		}
		if (title != null && !Jsoup.isValid(title, Whitelist.none())) {
			errorList.add(new ValidationError(TITLE,
					MessagesStrings.NO_HTML_ALLOWED));
		}
		if (description != null
				&& !Jsoup.isValid(description, Whitelist.none())) {
			errorList.add(new ValidationError(DESCRIPTION,
					MessagesStrings.NO_HTML_ALLOWED));
		}
		if (dirName == null || dirName.trim().isEmpty()) {
			errorList.add(new ValidationError(DIRNAME,
					MessagesStrings.MISSING_DIRNAME));
		}
		Pattern pattern = Pattern.compile(IOUtils.REGEX_ILLEGAL_IN_FILENAME);
		Matcher matcher = pattern.matcher(dirName);
		if (dirName != null && matcher.find()) {
			errorList.add(new ValidationError(DIRNAME,
					MessagesStrings.INVALID_DIR_NAME));
		}
		if (comments != null && !Jsoup.isValid(comments, Whitelist.none())) {
			errorList.add(new ValidationError(COMMENTS,
					MessagesStrings.NO_HTML_ALLOWED));
		}
		if (jsonData != null && !JsonUtils.isValidJSON(jsonData)) {
			errorList.add(new ValidationError(JSON_DATA,
					MessagesStrings.INVALID_JSON_FORMAT));
		}
		if (groupStudy && minActiveMemberSize < 2) {
			errorList.add(new ValidationError(MIN_ACTIVE_MEMBER_SIZE,
					MessagesStrings.GROUP_MEMBER_SIZE));
		}
		if (groupStudy && maxActiveMemberSize < 2) {
			errorList.add(new ValidationError(MAX_ACTIVE_MEMBER_SIZE,
					MessagesStrings.GROUP_MEMBER_SIZE));
		}
		if (groupStudy && maxActiveMemberSize < minActiveMemberSize) {
			errorList.add(new ValidationError(MAX_ACTIVE_MEMBER_SIZE,
					MessagesStrings.GROUP_MAX_ACTIVE_MEMBER_SIZE));
		}
		if (groupStudy && maxTotalMemberSize < 2) {
			errorList.add(new ValidationError(MAX_TOTAL_MEMBER_SIZE,
					MessagesStrings.GROUP_MEMBER_SIZE));
		}
		if (groupStudy && maxTotalMemberSize < maxActiveMemberSize) {
			errorList.add(new ValidationError(MAX_TOTAL_MEMBER_SIZE,
					MessagesStrings.GROUP_MAX_TOTAL_MEMBER_SIZE));
		}
		return errorList.isEmpty() ? null : errorList;
	}

	@Override
	public String toString() {
		return studyId + " " + title;
	}

}