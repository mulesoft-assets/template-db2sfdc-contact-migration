
# Anypoint Template: database to Salesforce Contact Migration

# License Agreement
This template is subject to the conditions of the 
<a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>.
Review the terms of the license before downloading and using this template. You can use this template for free 
with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
As a Salesforce administrator I want to do a one-time synchronize of contacts from a database to Salesforce.

This template helps you set an online migration of contacts from a database to a Salesforce instance. Every time the HTTP endpoint is triggered, the integration migrates all the contacts in the database in a one time integration and it is responsible for updating or inserting a contact into the target Salesforce instance.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this template leverages the batch module.
The batch job is divided in Process Records and On Complete stages.
The integration is triggered by browsing to the HTTP endpoint defined in the flow that triggers the application, queries the database contacts matching a filter criteria, and executes the batch job.
During the Process Records stage, each database contact is filtered depending on if it has an existing matching contact in the Salesforce instance.
The last step of the Process stage groups the contacts and inserts or updates them into Salesforce.
Finally during the On Complete stage, the template logs output statistics data on the console and sends an email with the results.

In this template, you may choose whether the account for contact is created as well during the process.

# Considerations

To make this template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source (database) and destination (Salesforce) systems, that must be made in order for all to run smoothly. 
Failing to do so could lead to unexpected behavior of the template.

This template illustrates the migration use case between a database and Salesforce, thus it requires a database instance to work.
The template comes packaged with a SQL script to create the database table that it uses. It is your responsibility to use that script to create the table in an available schema and change the configuration accordingly. The SQL script file can be found in src/main/resources/contact.sql.

## DB Considerations

To get this template to work:

This template may use date time or timestamp fields from the database to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it cannot handle time offsets.
We define time offsets as the time difference that may surface between date time and timestamp fields from different systems due to a differences in the system's internal clock.
Take this in consideration and take the actions needed to avoid the time offset.

### As a Data Source

There are no considerations with using a database as a data origin.


## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work.

### FAQ

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>


### As a Data Destination

There are no considerations with using Salesforce as a data destination.









# Run it!
Simple steps to get database to Salesforce Contact Migration running.
**Note:** This template illustrates the migration use case between Salesforce and a database, thus it requires a database instance to work.
The template comes packaged with a SQL script to create the database table that it uses. 
It is your responsibility to use the script to create the table in an available schema and change the configuration accordingly. The SQL script file can be found in src/main/resources/contact.sql.

This template is customized for MySQL. To use it with different SQL implementation, some changes are necessary:

* Update the SQL script dialect to the desired one.
* Replace the MySQL driver library or add another dependency to the desired one in the pom.xml file.
* Update the database config to a suitable connection instead of `db:my-sql-connection` in the global elements (config.xml).
* Update connection configurations in the `mule.*.properties` file.

This is an example of the output you see after browsing to the HTTP endpoint:

	{
	  "Message": "Batch Process initiated",
	  "ID": "7fc674b0-e4b7-11e7-9627-100ba905a441",
	  "RecordCount": 32,
	  "StartExecutionOn": "2018-12-19T13:24:03Z"
	}

## Running On Premises
In this section we help you run your template on your computer.


### Where to Download Anypoint Studio and the Mule Runtime
If you are a newcomer to Mule, here is where to get the tools.

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)


### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your
Anypoint Platform credentials, search for the template, and click **Open**.


### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`
+ Click `Mule Application (configure)`
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`
+ Click `Run`


### Running on Mule Standalone
Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable. To follow the example, this is `mule.env=prod`. 
After this, to trigger the use case browse to the local HTTP connector with the port you configured in your file. If this is, for instance, `9090` then browse to `http://localhost:9090/migratecontacts` and this outputs a summary report and sends it in the e-mail.

## Running on CloudHub
While creating your application on CloudHub (or you can do it later as a next step), go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the **mule.env**.
While creating your application on CloudHub, or you can do it later as a next step, you need to go to Deployment > Advanced to set all environment variables detailed in "Properties to Configure" as well as the **mule.env**. 
Follow other steps and once your app is all set and started, there is no need to do anything else. If you choose `contactsmigration` as a domain name to trigger the use case, browse to `http://contactsmigration.cloudhub.io/migratecontacts` and a report is sent to the email addresses you configured.

### Deploying your Anypoint Template on CloudHub
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this


## Properties to Configure
To use this template, configure properties (credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
**Application Configuration**

+ http.port `9090`
+ page.size `200`
    
**Syncing policy for accounts**

+ account.sync.policy `syncAccount`

**Note:** The property **account.sync.policy** can take any of the two following values: 

+ **doNotCreateAccount**: If the propety has no value assigned to it, the application does nothing ito the account and just moves the contact over.
+ **syncAccount**: It tries to create the contact's account if this is not pressent in the Salesforce instance.

**Database Connector Configuration**

+ db.host `localhost`
+ db.port `3306`
+ db.user `user-name`
+ db.password `user-password`
+ db.databasename `dbname`

**Note:** If you need to connect to a different database, provide the JAR file for the library, and change the value of that field in the connector.

**Salesforce Connector Configuration**

+ sfdc.username `joan.baez@example.com`
+ sfdc.password `JoanBaez456`
+ sfdc.securityToken `ces56arl7apQs56XTddf34X`

**SMTP Services Configuration**

+ smtp.host `smtp.example.com`
+ smtp.port `587`
+ smtp.user `pollyhedra@example.com`
+ smtp.password `password`

**Email Details**

+ mail.from `batch.contact.migration%40example.com`
+ mail.to `user@example.com`
+ mail.subject `Batch Job Finished Report`

# API Calls
Salesforce imposes limits on the number of API Ccalls that can be made. Therefore calculating this amount is important to consider. Contact Migration template calls to the API can be calculated using the formula:

*** 1 + X + X + X / 200*** 

***X*** is the number of contacts to be synchronized on each run. 

Divide by ***200*** because by default, contacts are gathered in groups of 200 for each upsert API call in the commit step.	

For instance if 10 records are fetched from origin instance, then 31 API calls are made (1 + 10 + 10 + 10) - if the sync account policy is enabled.


# Customize It!
This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page describes the XML files used with this template.

More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml


## config.xml
Configuration for connectors and configuration properties are set in this file. Even change the configuration here, all parameters that can be modified are in properties file, which is the recommended place to make your changes. However if you want to do core changes to the logic, you need to modify this file.

In the Studio visual editor, the properties are on the *Global Element* tab.


## businessLogic.xml
The functional aspect of this template is implemented on this XML, directed by a flow that checks for Salesforce creations or updates. The several message processors constitute four high level actions that fully implement the logic of this template:

1. During the Input stage, the template goes to the database and queries all existing contacts that match the filter criteria.
2. During the Process Records stage, each database contact is checked by name against Salesforce, if it has an existing matching objects in database.
3. The account associated with a database contact is migrated to an account associated with a contact in Salesforce. The matching is performed by querying a Salesforce instance for an entry with same name as the database account name.
4. The upsert of a contact in Salesforce is performed.
5. Finally during the On Complete stage, the template logs output statistics data on the console and sends email.



## endpoints.xml
This is the file where you can find the inbound and outbound sides of your integration app.
This template uses an HTTP Listener connector as the way to trigger the use case.
### Inbound Flow

**HTTP Listener Connector** - Start Report Generation

+ `${http.port}` is set as a property to be defined either on a property file or in CloudHub environment variables.
+ The path configured by default is `migratecontacts` that you are free to change to the one you prefer.
+ The host name for all endpoints in your CloudHub configuration is `localhost`. CloudHub routes requests from your application domain URL to the endpoint.
+ The endpoint is a *request-response* and a result of calling it, is that the response fetches the total records by the criteria specified.



## errorHandling.xml
This is the right place to handle how your integration reacts depending on the different exceptions. 
This file provides error handling that is referenced by the main flow in the business logic.




