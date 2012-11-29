/*
 * Copyright 2009 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDocumentList implements DocumentList {

	private static final long serialVersionUID = 1L;
	private static final String RS_DATA = "rs:data";
	private static final String RS_ROW = "z:row";
	private static final String ID = "Id";
	private static final String UUID = "uuid";
	private static final String LAST_MODIFIED_DATE = "lastModified";
	private static final String UUID_TO_DELETE = "uuidToDelete";
	private static final String LAST_MODIFIED_DATE_DELETE_CLAUSE = "lastModifiedForDeleteDoc";
	private static final String UUID_AS_DELETE_CLAUSE = "uuidAsDeleteClause";
	private static final String LAST_REMOVE_DATE = "lastRemoveDate";
	private Document resultDoc = null;
	private Document resultDeleteDoc = null;
	private Document resultDocToDelete = null;
	private FileDocument fileDocument = null;
	private FileDocument fileDeleteDocument = null;
	private FileDocument fileDocumentToDelete = null;
	private IObjectStore objectStore = null;
	private String docId = "";
	private String deleteDocId = "";
	private String displayUrl;
	private String lastCheckPoint;
	private String dateFirstPush;
	private String docIdToDelete = "";
	private boolean isPublic;
	private HashSet includedMeta;
	private HashSet excludedMeta;
	private int index = -1;
	private NodeList data = null;
	private NodeList deleteData = null;
	private NodeList dataToDelete = null;
	private static Logger LOGGER = Logger.getLogger(FileDocumentList.class.getName());

	public FileDocumentList(Document document, Document deleteDocument,
	        Document documentToDelete, IObjectStore refObjectStore,
	        boolean refIsPublic, String refDisplayUrl, HashSet refIncludedMeta,
	        HashSet refExcludedMeta, String refDateFirstPush,
	        String refCheckPoint) {
		this.resultDoc = document;
		this.resultDeleteDoc = deleteDocument;
		this.resultDocToDelete = documentToDelete;
		this.objectStore = refObjectStore;
		this.displayUrl = refDisplayUrl;
		this.index = 1;
		this.data = resultDoc.getElementsByTagName(RS_DATA).item(0).getChildNodes();
		LOGGER.log(Level.INFO, "Number of new documents discovered: "
		        + resultDoc.getElementsByTagName(RS_ROW).getLength());
		LOGGER.log(Level.FINE, "Child of rs_data : " + data);		

		if (resultDeleteDoc != null) {
			this.deleteData = resultDeleteDoc.getElementsByTagName(RS_DATA).item(0).getChildNodes();
			LOGGER.log(Level.INFO, "Number of documents matching with delete clause discovered: "
			        + resultDeleteDoc.getElementsByTagName(RS_ROW).getLength());
			LOGGER.log(Level.FINE, "List of results to delete as matching with delete clause : "
			        + deleteData);			
		}

		this.dataToDelete = resultDocToDelete.getElementsByTagName(RS_DATA).item(0).getChildNodes();
		LOGGER.log(Level.INFO, "Number of new documents to be removed: "
		        + resultDocToDelete.getElementsByTagName(RS_ROW).getLength());
		LOGGER.log(Level.FINE, "List of results to delete : "
		        + dataToDelete);

		this.isPublic = refIsPublic;
		this.includedMeta = refIncludedMeta;
		this.excludedMeta = refExcludedMeta;
		this.lastCheckPoint = refCheckPoint;
		this.dateFirstPush = refDateFirstPush;
	}

	/***
	 * The nextDocument method gets the next document from the document list
	 * that the connector acquires from the FileNet repository.
	 * 
	 * @param
	 * @return com.google.enterprise.connector.spi.Document
	 */
	public com.google.enterprise.connector.spi.Document nextDocument()
	        throws RepositoryDocumentException {
		int dataLen = data.getLength();
		int deleteDataLen = 0;

		if (deleteData != null) {
			deleteDataLen = deleteData.getLength();
		}

		LOGGER.entering("FileDocumentList", "nextDocument()");
		LOGGER.log(Level.FINE, "Number of documents to be retrieved: "
		        + data.getLength());
		LOGGER.log(Level.FINE, "Number of indexes to be deleted from appliance as matching with delete clause: "
		        + deleteDataLen);
		LOGGER.log(Level.FINE, "Number of indexes to be deleted from appliance: "
		        + dataToDelete.getLength());
		LOGGER.log(Level.FINE, "Index of the document in list " + index);

		if (index > -1 && index < dataLen) {
			NamedNodeMap nodeMap = data.item(index).getAttributes();
			for (int j = 0; j < nodeMap.getLength(); j++) {
				if (nodeMap.item(j).getNodeName().equals(ID)) {
					index++;
					if (data.item(index).getNodeType() == Node.TEXT_NODE) {
						index++;
					}
					LOGGER.info("ADD...");
					try {
						docId = (String) nodeMap.item(j).getNodeValue();
						String dateLastModified = nodeMap.item(j - 1).getNodeValue();
						LOGGER.info("dateLastModified " + dateLastModified);

						fileDocument = new FileDocument(
						        (String) nodeMap.item(j).getNodeValue(),
						        dateLastModified, this.objectStore,
						        this.isPublic, this.displayUrl,
						        this.includedMeta, this.excludedMeta,
						        SpiConstants.ActionType.ADD);

						return fileDocument;
					} catch (DOMException e) {
						// skip this document
						index++;
						LOGGER.severe("Unable to retrieve docId for item: " + e);
						throw new RepositoryDocumentException(
						        "Unable to retrieve docId for item", e);
					}
				}
			}
		} else if (index >= dataLen && index < (deleteDataLen + dataLen) - 1) {

			int indexDelete = index - (dataLen) + 1;
			NamedNodeMap nodeMap = deleteData.item(indexDelete).getAttributes();
			for (int j = 0; j < nodeMap.getLength(); j++) {
				if (nodeMap.item(j).getNodeName().equals(ID)) {
					index++;
					LOGGER.info("DELETE...(Documents satisfying additional delete clause) ");

					if (deleteData.item(indexDelete) != null) {

						if (deleteData.item(indexDelete).getNodeType() == Node.TEXT_NODE) {
							index++;
						}
						try {
							deleteDocId = (String) nodeMap.item(j).getNodeValue();
							String dateLastModified = nodeMap.item(j - 1).getNodeValue();
							LOGGER.info("dateLastModified : "
							        + dateLastModified);

							fileDeleteDocument = new FileDocument(deleteDocId,
							        dateLastModified, this.objectStore,
							        this.isPublic, this.displayUrl,
							        this.includedMeta, this.excludedMeta,
							        SpiConstants.ActionType.DELETE);

							String commonVersionId = fileDeleteDocument.getVsDocId();
							fileDeleteDocument.setVersionId(commonVersionId);

							index++;
							return fileDeleteDocument;
						} catch (DOMException e) {
							// skip this document
							index++;
							LOGGER.severe("Unable to retrieve docId for item: "
							        + e);
							throw new RepositoryDocumentException(
							        "Unable to retrieve docId for item", e);
						}
					}
				}
			}

		} else if ((index >= (deleteDataLen + dataLen) - 1)
		        && index < ((dataToDelete.getLength() + (deleteDataLen
		        + dataLen - 2)))) {
			int indexDelete = index;
			if (dataLen > 0) {
				indexDelete = indexDelete - dataLen + 1;
			}
			if (deleteDataLen > 0) {
				indexDelete = indexDelete - deleteDataLen + 1;
			}
			NamedNodeMap nodeMap = dataToDelete.item(indexDelete).getAttributes();

			for (int j = 0; j < nodeMap.getLength(); j++) {
				if (nodeMap.item(j).getNodeName().equals(ID)) {
					index++;
					LOGGER.info("DELETE...(Documents deleted from repository)");

					if (dataToDelete.item(indexDelete) != null) {

						if (dataToDelete.item(indexDelete).getNodeType() == Node.TEXT_NODE) {
							index++;
						}
						try {
							docIdToDelete = (String) nodeMap.item(j).getNodeValue();
							LOGGER.info("docIdToDelete : " + docIdToDelete);
							String commonVersionId = nodeMap.item(j + 1).getNodeValue();
							String dateLastModified = nodeMap.item(j - 1).getNodeValue();
							LOGGER.info("dateLastModified : "
							        + dateLastModified);

							fileDocumentToDelete = new FileDocument(
							        docIdToDelete, commonVersionId,
							        dateLastModified, this.objectStore,
							        this.isPublic, this.displayUrl,
							        this.includedMeta, this.excludedMeta,
							        SpiConstants.ActionType.DELETE);
							index++;
							return fileDocumentToDelete;
						} catch (DOMException e) {
							// skip this document
							index++;
							LOGGER.severe("Unable to retrieve docId for item: "
							        + e);
							throw new RepositoryDocumentException(
							        "Unable to retrieve docId for item", e);
						}
					}
				}
			}
		}
		index++;
		return null;
	}

	/***
	 * Checkpoint method indicates the current position within the document
	 * list, that is where to start a resumeTraversal method. The checkpoint
	 * method returns information that allows the resumeTraversal method to
	 * resume on the document that would have been returned by the next call to
	 * the nextDocument method.
	 * 
	 * @param
	 * @return String checkPoint - information that allows the resumeTraversal
	 *         method to resume on the document
	 */
	public String checkpoint() throws RepositoryException {

		if (((docId == null) && (docIdToDelete == null) && (deleteDocId == null))
		        || ((fileDocument == null) && (fileDocumentToDelete == null) && (fileDeleteDocument == null))) {
			LOGGER.log(Level.WARNING, "Cannot create checkpoint: No documents found.");
			throw new RepositoryException(
			        "Cannot create checkpoint: No documents found.");
		}
		LOGGER.log(Level.FINE, "Creation of the Checkpoint");
		String dateString = "";
		String dateStringDocumentToDelete = "";
		String dateStringDocumentAsDeleteClause = "";

		if (fileDocument != null) {
			Property val = fetchAndVerifyValueForCheckpoint(fileDocument, SpiConstants.PROPNAME_LASTMODIFIED);

			Calendar date = null;
			try {
				String dateStr = val.nextValue().toString();
				dateString = FileDateValue.calendarToIso8601(dateStr);
			} catch (ParseException e1) {
				LOGGER.log(Level.WARNING, "Unable to parse the date string for add. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			} catch (Exception e1) {
				LOGGER.log(Level.WARNING, "Unable to parse the date string for add. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			}
			LOGGER.log(Level.FINE, "dateString of the checkpoint of added document is "
			        + dateString);
		} else if (lastCheckPoint != null) {
			JSONObject jo;
			try {
				jo = new JSONObject(lastCheckPoint);
				docId = jo.getString(UUID);
				dateString = jo.getString(LAST_MODIFIED_DATE);
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "JSON exception, while getting last checkpoint.", e);
				throw new RepositoryException(
				        "JSON exception, while getting last checkpoint.", e);
			}
		} else {
			LOGGER.fine("date of the first push : " + dateFirstPush);
			dateString = dateFirstPush;
		}

		LOGGER.log(Level.FINE, "fileDeleteDocument : " + fileDeleteDocument);
		LOGGER.log(Level.FINE, "lastCheckPoint : " + lastCheckPoint);

		if (fileDeleteDocument != null) {
			Property valToDelete = fetchAndVerifyValueForCheckpoint(fileDeleteDocument, SpiConstants.PROPNAME_LASTMODIFIED);

			Calendar date = null;
			try {
				String dateStr = valToDelete.nextValue().toString();
				dateStringDocumentAsDeleteClause = FileDateValue.calendarToIso8601(dateStr);
			} catch (ParseException e1) {
				LOGGER.log(Level.WARNING, "Unable to parse the date string for delete. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			} catch (Exception e1) {
				LOGGER.log(Level.WARNING, "Unable to parse the date string for delete. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			}
			LOGGER.log(Level.FINE, "dateString of the checkpoint of deleted document matching with delete clause is "
			        + dateStringDocumentAsDeleteClause);
		} else if (lastCheckPoint != null) {
			LOGGER.log(Level.FINE, "Get the last modified date from the last checkpoint ");
			JSONObject jo;
			try {
				jo = new JSONObject(lastCheckPoint);
				dateStringDocumentAsDeleteClause = jo.getString(LAST_MODIFIED_DATE_DELETE_CLAUSE);
				deleteDocId = jo.getString(UUID_AS_DELETE_CLAUSE);
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "JSON exception, while getting last removed date.", e);
				throw new RepositoryException(
				        "JSON exception, while getting last removed date.", e);
			}
		} else {
			LOGGER.log(Level.FINE, "date of the first push : " + dateFirstPush);
			dateStringDocumentAsDeleteClause = dateFirstPush;
		}

		LOGGER.log(Level.FINE, "fileDocumentToDelete : " + fileDocumentToDelete);
		LOGGER.log(Level.FINE, "lastCheckPoint : " + lastCheckPoint);

		if (fileDocumentToDelete != null) {
			Property valToDelete = fetchAndVerifyValueForCheckpoint(fileDocumentToDelete, SpiConstants.PROPNAME_LASTMODIFIED);

			Calendar date = null;
			try {
				String dateStr = valToDelete.nextValue().toString();
				dateStringDocumentToDelete = FileDateValue.calendarToIso8601(dateStr);
			} catch (ParseException e1) {
				LOGGER.log(Level.WARNING, "Unable to parse the date string for delete. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			} catch (Exception e1) {
				LOGGER.log(Level.WARNING, "Unable to parse the date string for delete. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			}
			LOGGER.log(Level.FINE, "dateString of the checkpoint of deleted document is "
			        + dateStringDocumentToDelete);
		} else if (lastCheckPoint != null) {
			LOGGER.log(Level.FINE, "Get the last modified date from the last checkpoint ");
			JSONObject jo;
			try {
				jo = new JSONObject(lastCheckPoint);
				dateStringDocumentToDelete = jo.getString(LAST_REMOVE_DATE);
				docIdToDelete = jo.getString(UUID_TO_DELETE);
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "JSON exception, while getting last removed date.", e);
				throw new RepositoryException(
				        "JSON exception, while getting last removed date.", e);
			}
		} else {
			LOGGER.log(Level.FINE, "date of the first push : " + dateFirstPush);
			dateStringDocumentToDelete = dateFirstPush;
		}

		String result = null;
		try {
			JSONObject jo = new JSONObject();
			jo.put(UUID, docId);
			jo.put(LAST_MODIFIED_DATE, dateString);
			jo.put(UUID_TO_DELETE, docIdToDelete);
			jo.put(LAST_REMOVE_DATE, dateStringDocumentToDelete);
			jo.put(UUID_AS_DELETE_CLAUSE, deleteDocId);
			jo.put(LAST_MODIFIED_DATE_DELETE_CLAUSE, dateStringDocumentAsDeleteClause);
			result = jo.toString();
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Unable to create String out of JSON Object", e);
			throw new RepositoryException("Unexpected JSON problem", e);
		}
		LOGGER.info("checkpoint: " + result);
		return result;
	}

	protected Property fetchAndVerifyValueForCheckpoint(FileDocument pm,
	        String pName) throws RepositoryException {
		Property property = pm.findProperty(pName);
		if (property == null) {
			LOGGER.log(Level.WARNING, "Checkpoint must have a " + pName
			        + " property");
			throw new RepositoryException("Checkpoint must have a " + pName
			        + " property");
		}
		return property;
	}
}
