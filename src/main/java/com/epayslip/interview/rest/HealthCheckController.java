package com.epayslip.interview.rest;

import static com.epayslip.interview.vo.RestResult.fail;
import static com.epayslip.interview.vo.RestResult.success;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.epayslip.interview.vo.RestResult;

@RestController("healthCheckController")
public class HealthCheckController {
	private static final String DB_CHECK_SQL = "select 1 from dual";
	private static final Logger LOG = LoggerFactory.getLogger(HealthCheckController.class);

	@Resource(name = "jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@RequestMapping(value = "/api/public/health", method = RequestMethod.GET)
	public RestResult healthcheck() {
		String dbErrorMsg = checkDB(jdbcTemplate, "Interview");
		boolean dbOK = StringUtils.isEmpty(dbErrorMsg);
		if (dbOK) {
			LOG.info("Health Check is fine.");
			return success();
		} else {
			LOG.error("Health Check is failure. Error Message: {}", dbErrorMsg);
			return fail(dbErrorMsg);
		}
	}

	private String checkDB(JdbcTemplate jdbcTemplate, String dbName) {
		try {
			jdbcTemplate.queryForObject(DB_CHECK_SQL, Integer.class);
		} catch (Throwable th) {
			LOG.error(th.getMessage(), th);
			return String.format("Database - %s ERROR : %s", dbName, th.getMessage());
		}

		return null;
	}

}
