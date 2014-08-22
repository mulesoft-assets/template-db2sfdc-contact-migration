/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import static org.mule.templates.builders.SfdcObjectBuilder.aContact;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.processor.chain.InterceptingChainLifecycleWrapper;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.builders.SfdcObjectBuilder;
import org.mule.templates.db.MySQLDbCreator;
import org.mule.util.UUID;

import com.mulesoft.module.batch.BatchTestHelper;
import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is validating the correct behavior of the flows
 * for this Mule Anypoint Template
 * 
 */
@SuppressWarnings("unchecked")
public class BusinessLogicIT extends AbstractTemplateTestCase {

	private static final String INBOUND_FLOW_NAME = "triggerFlow";
	private static final String ANYPOINT_TEMPLATE_NAME = "db2sfdc-contact-migration";
	private static final int TIMEOUT_MILLIS = 60;
	private BatchTestHelper batchTestHelper;

	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	private static final String PATH_TO_SQL_SCRIPT = "src/main/resources/contact.sql";
	private static final String DATABASE_NAME = "DB2SFDContactMigration" + new Long(new Date().getTime()).toString();
	private static final MySQLDbCreator DBCREATOR = new MySQLDbCreator(DATABASE_NAME, PATH_TO_SQL_SCRIPT, PATH_TO_TEST_PROPERTIES);

	private static List<String> contactsCreatedInSalesforce = new ArrayList<String>();
	private static List<String> accountsCreatedInSalesforce = new ArrayList<String>();
	private String name = "";
	
	private static SubflowInterceptingChainLifecycleWrapper deleteContactFromSalesforceFlow;
	private static SubflowInterceptingChainLifecycleWrapper createContactInDatabaseFlow;
	private static SubflowInterceptingChainLifecycleWrapper queryContactFromSalesforceFlow;

	@BeforeClass
	public static void beforeTestClass() {
		System.setProperty("page.size", "1000");
		System.setProperty("db.jdbcUrl", DBCREATOR.getDatabaseUrlWithName());
		DBCREATOR.setUpDatabase();
		
		// Set default water-mark expression to current time
		System.clearProperty("watermark.default.expression");
		DateTime now = new DateTime(DateTimeZone.UTC);
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		System.setProperty("watermark.default.expression", now.toString(dateFormat));
		System.setProperty("account.sync.policy", "syncAccount");
	}
	
	@Before
	public void setUp() throws Exception {
		getAndInitializeFlows();
		DBCREATOR.setUpDatabase();
		batchTestHelper = new BatchTestHelper(muleContext);
		
		name = ANYPOINT_TEMPLATE_NAME + "_" + UUID.getUUID();
		// Build test contacts associated with account
		SfdcObjectBuilder contact = aContact()
				.with("Id", 999999)
				.with("LastName", name)
				.with("AccountName", name)
				.with("Email",
						ANYPOINT_TEMPLATE_NAME + "-"
								+ System.currentTimeMillis()
								+ "@mail.com");
		SfdcObjectBuilder c = contact;
		createTestContactsInDatabase(c.build(), createContactInDatabaseFlow);
	}

	@AfterClass
	public static void shutDown() {
		System.clearProperty("polling.frequency");
		System.clearProperty("watermark.default.expression");
		System.clearProperty("account.sync.policy");
	}

	@After
	public void tearDown() throws MuleException, Exception {
		cleanUpSandboxesByRemovingTestContacts();
		DBCREATOR.tearDownDataBase();
	}

	private void getAndInitializeFlows() throws InitialisationException {
		// Flow for creating contacts in sfdc A instance
		createContactInDatabaseFlow = getSubFlow("createContactInDatabaseFlow");
		createContactInDatabaseFlow.initialise();

		// Flow for deleting contacts in sfdc B instance
		deleteContactFromSalesforceFlow = getSubFlow("deleteContactFromSalesforceFlow");
		deleteContactFromSalesforceFlow.initialise();

		// Flow for querying contacts in sfdc B instance
		queryContactFromSalesforceFlow = getSubFlow("queryContactFromSalesforceFlow");
		queryContactFromSalesforceFlow.initialise();
	}

	private static void cleanUpSandboxesByRemovingTestContacts() throws MuleException, Exception {
		final List<String> idList = new ArrayList<String>();
		for (String contact : contactsCreatedInSalesforce) {
			idList.add(contact);
		}
		deleteContactFromSalesforceFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

	@Test
	public void testMainFlow() throws MuleException, Exception {
		// Execution
		runFlow(INBOUND_FLOW_NAME);
		executeWaitAndAssertBatchJob(INBOUND_FLOW_NAME);
		
		// query for just created contact in Salesforce
		Map<String, Object> contactInSf = new HashMap<String, Object>();
		contactInSf.put("LastName", name);
		Map<String, Object> response = (Map<String, Object>)queryContactFromSalesforceFlow.process(getTestEvent(contactInSf, MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload();

		System.err.println(contactInSf);
		System.err.println("res " + response);
		
		Assert.assertNotNull("There should be a contact created", response.get("Name"));
		Assert.assertTrue("Contact LastName should match", response.get("Name").equals(name));
		contactsCreatedInSalesforce.add((String)contactInSf.get("Id"));		
		
		
		Assert.assertNotNull("There should be an account created for contact", response.get("Account"));
		Map<String, Object> account = (HashMap<String, Object>)response.get("Account");
		Assert.assertTrue("Account name does not match", account.get("Name").equals(name));
		accountsCreatedInSalesforce.add((String)account.get("Id"));		
	}

	private String createTestContactsInDatabase(Map<String, Object> contact,	InterceptingChainLifecycleWrapper createContactFlow) throws MuleException, Exception {
		List<Map<String, Object>> salesforceContacts = new ArrayList<Map<String, Object>>();
		salesforceContacts.add(contact);

		createContactFlow.process(getTestEvent(salesforceContacts, MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload();
		return null;
	}

	private void executeWaitAndAssertBatchJob(String flowConstructName)
			throws Exception {

		// Execute synchronization
		runSchedulersOnce(flowConstructName);

		// Wait for the batch job execution to finish
		batchTestHelper.awaitJobTermination(TIMEOUT_MILLIS * 1000, 500);
		batchTestHelper.assertJobWasSuccessful();
	}
}
