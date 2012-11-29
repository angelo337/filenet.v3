package com.google.enterprise.connector.filenet4.filewrap;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import java.util.Date;

public interface IBaseObject {

  public String getId(ActionType action) throws RepositoryDocumentException;

  public Date getModifyDate(ActionType action)
      throws RepositoryDocumentException;

  public String getVersionSeriesId(ActionType action)
      throws RepositoryDocumentException;

  public Date getPropertyDateValueDelete(String name)
      throws RepositoryDocumentException;

  public String getClassNameEvent() throws RepositoryDocumentException;
}
