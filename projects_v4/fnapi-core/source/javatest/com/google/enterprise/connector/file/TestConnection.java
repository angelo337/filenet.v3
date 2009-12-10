package com.google.enterprise.connector.file;

import java.util.HashSet;

public class TestConnection {

	public static String adminUsername = "Administrator";

	public static String adminPassword = "root";
	
	public static String username = "userTest";
	
	public static String password = "p@ssw0rd";
	
	public static String wrongPassword ="password";
	
	public static String uri = "http://swp-vm-fncltv4:7001/wsi/FNCEWS40DIME";
	
	public static String objectFactory = "com.google.enterprise.connector.file.filejavawrap.FnObjectFactory";
	
	public static String displayURL = "http://swp-vm-fncltv4:7001/Workplace";
	
	public static String objectStore = "GED";

//	docId1 is Doc1 available for Administrator only
	public static String docId1 = "3811870F-410F-4C25-B853-CAC56014C552";

//	docVsId1
	public static String docVsId1 = "1D80A5F2-D452-46D6-8670-6A97207F171B";
	
//	docId2 is TestAuthentication available for Administrator and JPasquon user	
	public static String docId2 = "27CDE7CA-3273-4324-B468-D6C0C8550732";
	
//	docVsId1
	public static String docVsId2 = "C3581336-144F-43C8-BA6E-6F7CC7B160BF";
	
//	docId3 is Herb Alpert & The Tijuana Brass - Greatest Hits - 08 - Tijuana Taxi.mp3 available for all user
	public static String docId3 = "6745F58B-55DD-41EC-BC43-2EC6D6991780";
	
//	docVsId1
	public static String docVsId3 = "9EAE0F8B-BC7A-4C04-B459-E7AB0094E8A8";
	
//	docId4 isAuthentication in P8 4.0.pdf available for all user 
	public static String docId4 = "D791EBD8-E27C-48D4-A63E-EACEE727689D";
	
//	docVsId1
	public static String docVsId4 = "6B585D5C-4931-4C7E-BFC0-62D6A583FC8A";
	
//	CHeckpoint of the last modified document TestAuthentication.pdf
	public static String checkpoint1 = "{\"uuidToDelete\":\"\",\"uuid\":\"4E26DEC8-CDEE-4146-961B-E43E85400D8C\",\"lastRemoveDate\":\"2008-09-15T09:38:58.781\",\"lastModified\":\"2008-01-16T10:35:00.327\"}";
	 
	public static String checkpoint2 = "{\"uuidToDelete\":\"\",\"uuid\":\"4E26DEC8-CDEE-4146-961B-E43E85400D8C\",\"lastRemoveDate\":\"2008-09-15T09:38:58.781\",\"lastModified\":\"2008-01-16T10:35:00.327\"}"; 
	
	public static HashSet included_meta = null;
	static {
		included_meta = new HashSet();
		included_meta.add("ClassificationStatus");
		included_meta.add("ContentRetentionDate");
		included_meta.add("ContentSize");
		included_meta.add("CurrentState");
		included_meta.add("DateCreated");
		included_meta.add("DocumentTitle");
		included_meta.add("IsCurrentVersion");
		included_meta.add("IsFrozenVersion");
		included_meta.add("IsReserved");
		included_meta.add("LastModifier");
		included_meta.add("LockTimeout");
		included_meta.add("LockToken");
		included_meta.add("MajorVersionNumber");
		included_meta.add("MinorVersionNumber");
		included_meta.add("Name");
		included_meta.add("Owner");
		included_meta.add("StorageLocation");
		included_meta.add("VersionStatus");

	}

	public static HashSet excluded_meta = null;

	static {
		excluded_meta = new HashSet();
		excluded_meta.add("ActiveMarkings");
		excluded_meta.add("Annotations");
		excluded_meta.add("AuditedEvents");
		excluded_meta.add("ContentElementsPresent");
		excluded_meta.add("CurrentVersion");
		excluded_meta.add("DateContentLastAccessed");
		excluded_meta.add("DestinationDocuments");
		excluded_meta.add("DocumentLifecyclePolicy");
		excluded_meta.add("EntryTemplateId");
		excluded_meta.add("EntryTemplateLaunchedWorkflowNumber");
		excluded_meta.add("EntryTemplateObjectStoreName");
		excluded_meta.add("FoldersFiledIn");
		excluded_meta.add("IsInExceptionState");
		excluded_meta.add("IsVersioningEnabled");
		excluded_meta.add("LockOwner");
		excluded_meta.add("OwnerDocument");
		excluded_meta.add("PublicationInfo");
		excluded_meta.add("ReleasedVersion");
		excluded_meta.add("Reservation");
		excluded_meta.add("ReservationType");
		excluded_meta.add("SecurityParent");
		excluded_meta.add("SecurityPolicy");
		excluded_meta.add("StoragePolicy");
		excluded_meta.add("WorkflowSubscriptions");
		excluded_meta.add("CompoundDocumentState");
		excluded_meta.add("ComponentBindingLabel");
		excluded_meta.add("StorageArea");
		excluded_meta.add("ChildRelationships");
		excluded_meta.add("This");
		excluded_meta.add("IgnoreRedirect");
		excluded_meta.add("IndexationId");
		excluded_meta.add("DependentDocuments");
		excluded_meta.add("Permissions");
		excluded_meta.add("Versions");
		excluded_meta.add("ParentDocuments");
		excluded_meta.add("ParentRelationships");
		excluded_meta.add("Containers");
		excluded_meta.add("ChildDocuments");
		excluded_meta.add("Creator");
		excluded_meta.add("PublishingSubsidiaryFolder");
	}
	
	public static String[][] type = {
			{"ClassificationStatus","LONG"},
			{"ContentRetentionDate","DATE"},
			{"ContentSize","DOUBLE"},
			{"CurrentState","STRING"},
			{"DateCreated","DATE"},
			{"DocumentTitle","STRING"},
			{"IsCurrentVersion","BOOLEAN"},
			{"IsFrozenVersion","BOOLEAN"},
			{"IsReserved","BOOLEAN"},
			{"LastModifier","STRING"},
			{"LockTimeout","LONG"},
			{"LockToken","GUID"},
			{"MajorVersionNumber","LONG"},
			{"MinorVersionNumber","LONG"},
			{"Name","STRING"},
			{"Owner","STRING"},
			{"StorageLocation","STRING"},
			{"VersionStatus","LONG"},
			{"Id","GUID"},
			{"ClassDescription","OBJECT"},
			{"ContentElements","OBJECT"},
			{"DateLastModified","DATE"},
			{"MimeType","STRING"},
			{"VersionSeries","OBJECT"},
			{"ActiveMarkings","OBJECT"},
			{"Annotations","OBJECT"},
			{"AuditedEvents","OBJECT"},
			{"ContentElementsPresent","STRING"},
			{"CurrentVersion","OBJECT"},
			{"DateContentLastAccessed","DATE"},
			{"DestinationDocuments","null"},
			{"DocumentLifecyclePolicy","OBJECT"},
			{"EntryTemplateId","GUID"},
			{"EntryTemplateLaunchedWorkflowNumber","STRING"},
			{"EntryTemplateObjectStoreName","STRING"},
			{"FoldersFiledIn","OBJECT"},
			{"IsInExceptionState","BOOLEAN"},
			{"IsVersioningEnabled","BOOLEAN"},
			{"LockOwner","STRING"},
			{"OwnerDocument","OBJECT"},
			{"PublicationInfo","BINARY"},
			{"ReleasedVersion","OBJECT"},
			{"Reservation","OBJECT"},
			{"ReservationType","LONG"},
			{"SecurityParent","OBJECT"},
			{"SecurityPolicy","OBJECT"},
			{"StoragePolicy","OBJECT"},
			{"WorkflowSubscriptions","OBJECT"},
			{"CompoundDocumentState","LONG"},
			{"ComponentBindingLabel","STRING"},
			{"StorageArea","OBJECT"},
			{"ChildRelationships","OBJECT"},
			{"This","OBJECT"},
			{"IgnoreRedirect","BOOLEAN"},
			{"IndexationId","GUID"},
			{"DependentDocuments","OBJECT"},
			{"Permissions","OBJECT"},
			{"Versions","OBJECT"},
			{"ParentDocuments","OBJECT"},
			{"ParentRelationships","OBJECT"},
			{"Containers","OBJECT"},
			{"ChildDocuments","OBJECT"},
			{"Creator","STRING"},
			{"PublishingSubsidiaryFolder","OBJECT"},
	};

}
