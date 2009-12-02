package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class FnObjectStoreTest extends TestCase {

	private IObjectStore objectStore;

	protected void setUp() throws Exception {
		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession(FnConnection.appId,
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.completePathToWcmApiConfig));
		session.verify();
		objectStore = objectFactory.getObjectStore(
				FnConnection.objectStoreName, session);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectStore.getObject(String)'
	 */
	public void testGetObject() throws RepositoryException {

		IDocument doc = (IDocument) objectStore.getObject(
				IBaseObject.TYPE_DOCUMENT, FnConnection.docId);
		assertNotNull(doc);
		assertTrue(doc instanceof FnDocument);
		assertEquals(FnConnection.docIdTitle, doc
				.getPropertyStringValue("Name"));

		IVersionSeries vs = (IVersionSeries) objectStore.getObject(
				IBaseObject.TYPE_VERSIONSERIES,
				"{7B56042FFC-976E-4F61-8B32-B789218B9324}");
		assertNotNull(vs);
		assertTrue(vs instanceof FnVersionSeries);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectStore.getName()'
	 */
	public void testGetName() throws RepositoryException {
		assertEquals(FnConnection.objectStoreName, objectStore.getName());
	}

}
