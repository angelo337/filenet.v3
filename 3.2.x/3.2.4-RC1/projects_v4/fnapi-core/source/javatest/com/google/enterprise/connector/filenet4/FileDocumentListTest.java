// Copyright 2007-2010 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4;

import com.google.common.base.Strings;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.filejavawrap.FnObjectList;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.mockjavawrap.MockBaseObject;
import com.google.enterprise.connector.filenet4.mockjavawrap.MockObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.DatabaseType;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FileDocumentListTest extends FileNetTestCase {
  private static final Logger LOGGER =
      Logger.getLogger(FileDocumentListTest.class.getName());

  private static DateFormat dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private enum SkipPosition {FIRST, MIDDLE, LAST};

  FileSession fs;
  FileTraversalManager ftm;

  protected void setUp() throws Exception {
    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    fs = (FileSession)connec.login();
    ftm = (FileTraversalManager) fs.getTraversalManager();
  }

  public void testLiveCheckpoint() throws Exception {
    ftm.setBatchHint(100);
    // Under live test and the test account, the deletion events weren't
    // returned from FileNet.
    boolean tested = false;
    DocumentList docList = ftm.startTraversal();
    Document doc;
    while ((doc = docList.nextDocument()) != null) {
      assertTrue(checkpointContains(docList.checkpoint(),
          doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
          JsonField.LAST_MODIFIED_TIME));
      tested = true;
    }
    assertTrue(tested);
  }

  /*
   * Testing chronological traversal
   */
  public void testLiveNextDocument() throws Exception {
    boolean isTested = false;
    DocumentList docList = ftm.startTraversal();
    assertNotNull("Document list is null", docList);
    Document doc = docList.nextDocument();
    while (doc != null) {
      Property lastModifiedProp =
          doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED);
      Value lastModifiedValue = lastModifiedProp.nextValue();
      Calendar cal = Value.iso8601ToCalendar(lastModifiedValue.toString());

      Document nextDoc = docList.nextDocument();
      if (nextDoc != null) {
        Property nextDocLastModifiedProp =
            nextDoc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED);
        Value nextDocLastModifiedValue = nextDocLastModifiedProp.nextValue();
        Calendar nextCal =
            Value.iso8601ToCalendar(nextDocLastModifiedValue.toString());
        assertTrue(cal.compareTo(nextCal) <= 0);
        isTested = true;
      }
      doc = nextDoc;
    }
    assertTrue(isTested);
  }

  public void testTimeSorting() throws Exception {
    String[][] entries = {
        {"AAAAAAAA-0000-0000-0000-000000000000", "2014-02-11T08:15:30.129"},
        {"BBBBBBBB-0000-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"BBBBBBAA-0000-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCCC-0000-0000-0000-000000000000", "2014-02-11T08:15:10.329"},
        {"DDDDDDDD-0000-0000-0000-000000000000", "2014-02-11T07:14:30.329"},
        {"EEEEEEEE-0000-0000-0000-000000000000", "2014-02-11T07:15:30.329"},
        {"FFFFFFFF-0000-0000-0000-000000000000", "2014-02-10T08:15:30.329"},
        {"GGGGGGGG-0000-0000-0000-000000000000", "2014-01-11T08:15:30.329"},
        {"HHHHHHHH-0000-0000-0000-000000000000", "2013-02-11T08:15:30.329"}
    };
    testSorting(new int[] {8, 7, 6, 4, 5, 3, 0, 2, 1}, entries,
        DatabaseType.ORACLE);
  }

  public void testTimeAndGUIDSorting() throws Exception {
    String[][] entries = {
        {"AAAAAA01-0000-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"BBBBBBCC-0000-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-00BB-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-00AA-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-AAAA-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-DDDD-AAAA-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-DDDD-00AA-0000-000000000000", "2014-02-11T08:15:30.329"}
    };
    testSorting(new int[]{0, 3, 4, 2, 6, 5, 1}, entries, DatabaseType.DB2);
    testSorting(new int[]{0, 3, 4, 2, 6, 5, 1}, entries, DatabaseType.ORACLE);
    testSorting(new int[]{0, 1, 3, 4, 2, 6, 5}, entries, DatabaseType.MSSQL);
  }

  private void testSorting(int[] expectedOrder, String[][] entries,
      DatabaseType dbType) throws Exception {
    MockObjectStore os = new MockObjectStore("objectstore", dbType,
        generateObjectMap(entries, false, true));
    DocumentList docList =
        getObjectUnderTest(os, getDocuments(os.getObjects()),
            getCustomDeletion(os.getObjects()),
            getDeletionEvents(os.getObjects()));

    // Test the order
    for (int index : expectedOrder) {
      Document doc = docList.nextDocument();
      Property fid = doc.findProperty(SpiConstants.PROPNAME_DOCID);
      assertEquals("[" + dbType + "] Incorrect id sorting order",
          entries[index][0], fid.nextValue().toString());
    }
  }

  private String[][] getEntries() {
    return new String[][] {
      {"AAAAAAA1-0000-0000-0000-000000000000", "2014-02-01T08:00:00.100"},
      {"AAAAAAA2-0000-0000-0000-000000000000", "2014-02-02T08:00:00.100"},
      {"BBBBBBB1-0000-0000-0000-000000000000", "2014-03-01T08:00:00.100"},
      {"BBBBBBB2-0000-0000-0000-000000000000", "2014-03-02T08:00:00.100"}
    };
  }

  public void testUnreleasedNextDeletionEvent_firstEntry() throws Exception {
    testUnreleasedNextDeletionEvent("2014-01-01T08:00:00.100",
        SkipPosition.FIRST);
  }

  public void testUnreleasedNextDeletionEvent_middleEntry() throws Exception {
    testUnreleasedNextDeletionEvent("2014-02-03T08:00:00.100",
        SkipPosition.MIDDLE);
  }

  public void testUnreleasedNextDeletionEvent_lastEntry() throws Exception {
    testUnreleasedNextDeletionEvent("2014-03-03T08:00:00.100",
        SkipPosition.LAST);
  }

  private void testUnreleasedNextDeletionEvent(String timeStamp,
      SkipPosition expectedPosition) throws Exception {
    String[][] unreleasedEntries =
          { {"AAAAAAA3-0000-0000-0000-000000000000", timeStamp} };
    testUnreleasedNextDocument(getEntries(),
        generateObjectMap(unreleasedEntries, true, false), expectedPosition);
  }

  public void testUnreleasedNextCustomDeletion_firstEntry() throws Exception {
    testUnreleasedNextCustomDeletion("2014-01-01T08:00:00.100",
        SkipPosition.FIRST);
  }

  public void testUnreleasedNextCustomDeletion_middleEntry() throws Exception {
    testUnreleasedNextCustomDeletion("2014-02-03T08:00:00.100",
        SkipPosition.MIDDLE);
  }

  public void testUnreleasedNextCustomDeletion_lastEntry() throws Exception {
    testUnreleasedNextCustomDeletion("2014-03-03T08:00:00.100",
        SkipPosition.LAST);
  }

  private void testUnreleasedNextCustomDeletion(String timeStamp,
      SkipPosition position) throws Exception {
    String[][] unreleasedEntries =
        { {"AAAAAAA3-0000-0000-0000-000000000000", timeStamp} };
    testUnreleasedNextDocument(getEntries(),
        generateCustomDeletion(unreleasedEntries, false), position);
  }

  private void testUnreleasedNextDocument(String[][] docEntries,
      Map<String, IBaseObject> unreleasedEntries,
      SkipPosition expectedPosition) throws Exception {
    String unreleasedGuid = unreleasedEntries.keySet().iterator().next();
    Map<String, IBaseObject> entries =
        generateObjectMap(docEntries, false, true);
    entries.putAll(unreleasedEntries);
    testUnreleasedNextDocument(entries, unreleasedGuid, expectedPosition);
  }

  private void testUnreleasedNextDocument(Map<String, IBaseObject> entries,
      String unreleasedGuid, SkipPosition expectedPosition) throws Exception {
    // Setup object store
    @SuppressWarnings("unchecked")
    MockObjectStore os =
        newObjectStore("MockObjectStore", DatabaseType.MSSQL, entries);

    // Begin testing nextDocument for exception
    DocumentList docList = getObjectUnderTest(os, getDocuments(os.getObjects()),
        getCustomDeletion(os.getObjects()), getDeletionEvents(os.getObjects()));

    SkipPosition actualPosition = SkipPosition.FIRST;
    try {
      for (Document doc = docList.nextDocument(); doc != null;
          doc = docList.nextDocument()) {
        actualPosition = SkipPosition.MIDDLE;
      }
      fail("Expect SkippedDocumentException");
    } catch (SkippedDocumentException expected) {
      if (!expected.getMessage().contains(unreleasedGuid)) {
        throw expected;
      }
      if (docList.nextDocument() == null) {
        actualPosition = SkipPosition.LAST;
      }
    }
    assertEquals(expectedPosition, actualPosition);
  }

  public void testMockCheckpoint() throws Exception {
    String[] docEntries = {
        "AAAAAAA1-0000-0000-0000-000000000000",
        "AAAAAAA2-0000-0000-0000-000000000000",
        "AAAAAAA3-0000-0000-0000-000000000000",
        "AAAAAAA4-0000-0000-0000-000000000000"
    };
    String[] deEntries = {
        "DE000001-0000-0000-0000-000000000000",
        "DE000002-0000-0000-0000-000000000000"
    };
    String[] cdEntries = {
        "CD000001-0000-0000-0000-000000000000",
        "CD000002-0000-0000-0000-000000000000",
        "CD000003-0000-0000-0000-000000000000"
    };

    // Setup object store
    @SuppressWarnings("unchecked")
    MockObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL,
        generateObjectMap(docEntries, false, true),
        generateObjectMap(deEntries, true, true),
        generateCustomDeletion(cdEntries, true));
    
    // Test checkpoint
    testMockCheckpoint(os, getDocuments(os.getObjects()),
        getCustomDeletion(os.getObjects()),
        getDeletionEvents(os.getObjects()));

    // Test checkpoint with null custom deletion list
    testMockCheckpoint(os, getDocuments(os.getObjects()), null,
        getDeletionEvents(os.getObjects()));

    // Test checkpoint with empty lists
    testMockCheckpoint(os, newEmptyObjectSet(),
        getCustomDeletion(os.getObjects()), getDeletionEvents(os.getObjects()));
    testMockCheckpoint(os, getDocuments(os.getObjects()),
        newEmptyObjectSet(), getDeletionEvents(os.getObjects()));
    testMockCheckpoint(os, getDocuments(os.getObjects()),
        getCustomDeletion(os.getObjects()), newEmptyObjectSet());
  }

  private void testMockCheckpoint(IObjectStore os, IObjectSet docSet,
      IObjectSet customDeletionSet, IObjectSet deletionEventSet)
          throws Exception {
    boolean expectAddTested = (docSet != null && docSet.getSize() > 0);
    boolean expectCustomDeletionTested =
        (customDeletionSet != null && customDeletionSet.getSize() > 0);
    boolean expectDeletionEventTested =
        (deletionEventSet != null && deletionEventSet.getSize() > 0);

    boolean isAddTested = false;
    boolean isDeletionEventTested = false;
    boolean isCustomDeletionTested = false;

    DocumentList docList =
        getObjectUnderTest(os, docSet, customDeletionSet, deletionEventSet);
    Document doc = null;
    while ((doc = docList.nextDocument()) != null) {
      Property actionProp = doc.findProperty(SpiConstants.PROPNAME_ACTION);
      ActionType actionType = SpiConstants.ActionType.findActionType(
          actionProp.nextValue().toString());

      String id =
          doc.findProperty(SpiConstants.PROPNAME_DOCID).nextValue().toString();
      if (ActionType.ADD.equals(actionType)) {
        IBaseObject object = os.getObject(null, id);
        assertFalse(object instanceof FileDeletionObject);
        assertTrue(checkpointContains(docList.checkpoint(),
            doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
            JsonField.LAST_MODIFIED_TIME));
        isAddTested = true;
      } else if (ActionType.DELETE.equals(actionType)) {
        // TODO(tdnguyen): revisit this logic to trim the curly braces when
        // making changes to version series ID.
        IBaseObject object =
            os.getObject(null, id.substring(1, id.length() - 1));
        if (object.isDeletionEvent()) {
          assertTrue(checkpointContains(docList.checkpoint(),
              doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
              JsonField.LAST_DELETION_EVENT_TIME));
          isDeletionEventTested = true;
        } else {
          assertTrue(checkpointContains(docList.checkpoint(),
              doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
              JsonField.LAST_CUSTOM_DELETION_TIME));
          isCustomDeletionTested = true;
        }
      }
    }
    assertEquals(expectAddTested, isAddTested);
    assertEquals(expectCustomDeletionTested, isCustomDeletionTested);
    assertEquals(expectDeletionEventTested, isDeletionEventTested);
  }

  private MockObjectStore newObjectStore(String name, DatabaseType dbType,
      Map<String, IBaseObject>... objectMaps) {
    Map<String, IBaseObject> data = new HashMap<String, IBaseObject>();
    for (Map<String, IBaseObject> objectMap : objectMaps) {
      data.putAll(objectMap);
    }
    return new MockObjectStore(name, dbType, data);
  }

  private DocumentList getObjectUnderTest(IObjectStore os, IObjectSet docSet,
      IObjectSet customDeletionSet, IObjectSet deletionEventSet) {
    Calendar cal = Calendar.getInstance();
    return new FileDocumentList(docSet, customDeletionSet, deletionEventSet,
        os, true, TestConnection.displayURL, TestConnection.included_meta,
        TestConnection.excluded_meta, getDateFirstPush(cal),
        TestConnection.checkpoint1, null);
  }

  private boolean checkpointContains(String checkpoint, Property lastModified,
      JsonField jsonField) throws JSONException, RepositoryException {
    if (Strings.isNullOrEmpty(checkpoint) || lastModified == null
        || jsonField == null) {
      return false;
    }
    JSONObject json = new JSONObject(checkpoint);
    String checkpointTime = (String) json.get(jsonField.toString());
    String docLastModifiedTime = lastModified.nextValue().toString();
    
    return checkpointTime.equals(docLastModifiedTime);
  }

  // Helper method to create object
  private IBaseObject createObject(String guid, String timeStr,
      boolean isDeletionEvent, boolean isReleasedVersion)
          throws ParseException {
    Date createdTime = dateFormatter.parse(timeStr);
    return new MockBaseObject(guid, "{" + guid + "}",
        createdTime, isDeletionEvent, isReleasedVersion);
  }

  /**
   * Generate a map of IBaseObject objects.
   * 
   * @param entries - a 2D array of object ID and created time.
   * @param isDeleteEvent - deletion event flag
   * @param releasedVersion - released version flag
   * @param cal - setting object's creation time.  The creation time will be
   *              incremented by timeIncrement before setting the object.
   * @param timeIncrement - time increment between objects in milliseconds.
   * @return Map<String, IBaseObject>
   * @throws ParseException 
   */
  private Map<String, IBaseObject> generateObjectMap(String[][] entries,
      boolean isDeleteEvent, boolean releasedVersion) throws ParseException {
    Map<String, IBaseObject> objectMap = new HashMap<String, IBaseObject>();
    for (String[] line : entries) {
      objectMap.put(line[0], createObject(line[0], line[1], isDeleteEvent,
          releasedVersion));
    }
    return objectMap;
  }

  private Map<String, IBaseObject> generateObjectMap(String[] entries,
      boolean isDeleteEvent, boolean releasedVersion) throws ParseException {
    Map<String, IBaseObject> objectMap = new HashMap<String, IBaseObject>();
    Calendar cal = Calendar.getInstance();
    for (String entry : entries) {
      objectMap.put(entry, createObject(entry, Value.calendarToIso8601(cal),
          isDeleteEvent, releasedVersion));
    }
    return objectMap;
  }

  private IObjectSet newEmptyObjectSet() {
    return new FnObjectList(new ArrayList<IBaseObject>());
  }

  /**
   * Generate a map of FileDeletionObject objects for custom deletion.
   * 
   * @param ids - an array of objects' ID to be generated.
   * @param releasedVersion - released version flag
   * @param cal - setting object's creation time.  The creation time will be
   *              incremented by timeIncrement before setting the object.
   * @param timeIncrement - time increment between objects in milliseconds.
   * @return Map<String, IBaseObject>
   * @throws ParseException 
   */
  private Map<String, IBaseObject> generateCustomDeletion(
      String[] entries, boolean releasedVersion) throws ParseException {
    Map<String, IBaseObject> objectMap = new HashMap<String, IBaseObject>();
    Calendar cal = Calendar.getInstance();
    for (String entry : entries) {
      IBaseObject object = createObject(entry, Value.calendarToIso8601(cal),
          false, releasedVersion);
      objectMap.put(entry, new FileDeletionObject(object));
    }
    return objectMap;
  }

  private Map<String, IBaseObject> generateCustomDeletion(String[][] entries,
      boolean releasedVersion) throws ParseException {
    Map<String, IBaseObject> objectMap = new HashMap<String, IBaseObject>();
    for (String[] line : entries) {
      IBaseObject object =
          createObject(line[0], line[1], false, releasedVersion);
      objectMap.put(line[0], new FileDeletionObject(object));
    }
    return objectMap;
  }

  private String getDateFirstPush(Calendar cal) {
    return dateFormatter.format(cal.getTime());
  }

  private IObjectSet getDocuments(Map<String, IBaseObject> objects)
      throws RepositoryDocumentException {
    List<IBaseObject> objectList = new ArrayList<IBaseObject>(objects.size());
    for (IBaseObject obj : objects.values()) {
      if (!obj.isDeletionEvent() && !(obj instanceof FileDeletionObject)) {
        objectList.add(obj);
      }
    }
    return new FnObjectList(objectList);
  }

  private IObjectSet getDeletionEvents(Map<String, IBaseObject> objects)
      throws RepositoryDocumentException {
    List<IBaseObject> objectList = new ArrayList<IBaseObject>();
    for (IBaseObject obj : objects.values()) {
      if (obj.isDeletionEvent()) {
        objectList.add(obj);
      }
    }
    return new FnObjectList(objectList);
  }

  private IObjectSet getCustomDeletion(Map<String, IBaseObject> objects)
      throws RepositoryDocumentException {
    List<IBaseObject> objectList = new ArrayList<IBaseObject>();
    for (IBaseObject obj : objects.values()) {
      if (obj instanceof FileDeletionObject) {
        objectList.add(obj);
      }
    }
    return new FnObjectList(objectList);
  }
}
