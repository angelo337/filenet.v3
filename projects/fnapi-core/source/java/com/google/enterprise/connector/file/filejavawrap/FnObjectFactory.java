package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryException;

public class FnObjectFactory implements IObjectFactory {

	public FnObjectFactory() {
		super();
	}

	public ISession getSession(String appId, String credTag, String userId,
			String password) throws RepositoryException {
		try {
			return new FnSession(ObjectFactory.getSession(appId,
					Session.DEFAULT, userId, password));
		} catch (NoClassDefFoundError e) {
			throw new RepositoryException(e);
		}

	}

	public IObjectStore getObjectStore(String objectStoreName,
			ISession fileSession) throws RepositoryException {
		fileSession.verify();
		return new FnObjectStore(ObjectFactory.getObjectStore(objectStoreName,
				((FnSession) fileSession).getSession()));
	}

	public ISearch getSearch(ISession fileSession) {
		return new FnSearch(ObjectFactory.getSearch(((FnSession) fileSession)
				.getSession()));

	}

}
