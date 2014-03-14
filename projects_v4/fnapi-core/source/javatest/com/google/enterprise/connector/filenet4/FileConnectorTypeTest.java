// Copyright 2009 Google Inc. All Rights Reserved.
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

import com.google.enterprise.connector.spi.ConfigureResponse;

import java.util.HashMap;
import java.util.Locale;

public class FileConnectorTypeTest extends FileNetTestCase {

  public void testValidateConfigWithBlankWhereClause() {
    HashMap<String, String> map = new HashMap<String, String>();
    String[] fields = { "username", "Password", "object_store",
        "object_factory", "workplace_display_url",
        "content_engine_url", "is_public",
        "additional_where_clause", "authentication_type" };
    map.put("username", TestConnection.adminUsername);
    map.put("Password", TestConnection.adminPassword);
    map.put("object_factory", TestConnection.objectFactory);
    map.put("object_store", TestConnection.objectStore);
    map.put("workplace_display_url", TestConnection.displayURL);
    map.put("content_engine_url", TestConnection.uri);
    map.put("additional_where_clause", "");//NOT COMPULSORY
    map.put("authentication_type", "");
    map.put("is_public", "on");
    map.put("delete_additional_where_clause", "");
    map.put("check_marking", "");
    map.put("googleGlobalNamespace", "Default");
    FileConnectorType testConnectorType = new FileConnectorType();
    testConnectorType.setConfigKeys(fields);
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    assertNull(resp);//NULL - IF NO ERROR VALUE FOUND
  }

  public void testValidateConfigIncorrectWhereClause() {
    HashMap<String, String> map = new HashMap<String, String>();
    String[] fields = { "username", "Password", "object_store",
        "object_factory", "workplace_display_url",
        "content_engine_url",
        "additional_where_clause", "authentication_type" };
    map.put("username", TestConnection.adminUsername);
    map.put("Password", TestConnection.adminPassword);
    map.put("object_factory", TestConnection.objectFactory);
    map.put("object_store", TestConnection.objectStore);
    map.put("workplace_display_url", TestConnection.displayURL);
    map.put("content_engine_url", TestConnection.uri);
    map.put("additional_where_clause", TestConnection.wrong_additional_where_clause);//NOT COMPULSORY
    map.put("authentication_type", "");
    map.put("is_public", "on");
    map.put("delete_additional_where_clause", "");
    map.put("check_marking", "");
    map.put("googleGlobalNamespace", "Default");
    FileConnectorType testConnectorType = new FileConnectorType();
    testConnectorType.setConfigKeys(fields);
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    //    assertNull(resp);//NULL - IF NO ERROR VALUE FOUND
    assertTrue(resp.getMessage().indexOf(TestConnection.where_clause_error_message) > -1);
  }

  public void testValidateConfigCorrectWhereClause() {
    HashMap<String, String> map = new HashMap<String, String>();
    String[] fields = { "username", "Password", "object_store",
        "object_factory", "workplace_display_url",
        "content_engine_url", "is_public",
        "additional_where_clause", "authentication_type" };
    map.put("username", TestConnection.adminUsername);
    map.put("Password", TestConnection.adminPassword);
    map.put("object_factory", TestConnection.objectFactory);
    map.put("object_store", TestConnection.objectStore);
    map.put("workplace_display_url", TestConnection.displayURL);
    map.put("content_engine_url", TestConnection.uri);
    map.put("additional_where_clause", TestConnection.additional_where_clause);//NOT COMPULSORY
    map.put("authentication_type", "");
    map.put("is_public", "on");
    map.put("delete_additional_where_clause", "");
    map.put("check_marking", "");
    map.put("googleGlobalNamespace", "Default");
    FileConnectorType testConnectorType = new FileConnectorType();
    testConnectorType.setConfigKeys(fields);
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    assertNull(resp);
  }

  public void testInvalidWorkplaceURL() {
    try {
      HashMap<String, String> map = new HashMap<String, String>();
      String[] fields = { "username", "Password", "object_store",
          "object_factory", "workplace_display_url",
          "content_engine_url", "is_public",
          "additional_where_clause", "authentication_type" };
      map.put("username", TestConnection.adminUsername);
      map.put("Password", TestConnection.adminPassword);
      map.put("object_factory", TestConnection.objectFactory);
      map.put("object_store", TestConnection.objectStore);
      map.put("workplace_display_url", TestConnection.incorrectDisplayURL);
      map.put("content_engine_url", TestConnection.uri);
      map.put("additional_where_clause", TestConnection.additional_where_clause);//NOT COMPULSORY
      map.put("authentication_type", "");
      map.put("is_public", "on");
      map.put("delete_additional_where_clause", "");
      map.put("check_marking", "");
      map.put("googleGlobalNamespace", "Default");
      FileConnectorType testConnectorType = new FileConnectorType();
      testConnectorType.setConfigKeys(fields);
      ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
      assertEquals(TestConnection.workplace_url__error_message, resp.getMessage());
    } catch (Exception e) {
      System.out.println("Incorrect URL");
    }
  }

  public void testRepeatedSlashContentEngineURL() {

    HashMap<String, String> map = new HashMap<String, String>();
    String[] fields = { "username", "Password", "object_store",
        "object_factory", "workplace_display_url",
        "content_engine_url", "is_public",
        "additional_where_clause", "authentication_type" };
    map.put("username", TestConnection.adminUsername);
    map.put("Password", TestConnection.adminPassword);
    map.put("object_factory", TestConnection.objectFactory);
    map.put("object_store", TestConnection.objectStore);
    map.put("workplace_display_url", TestConnection.displayURL);
    map.put("content_engine_url", TestConnection.uri);
    map.put("additional_where_clause", "");//NOT COMPULSORY
    map.put("authentication_type", "");
    map.put("is_public", "on");
    map.put("delete_additional_where_clause", "");
    map.put("check_marking", "");
    map.put("googleGlobalNamespace", "Default");
    FileConnectorType testConnectorType = new FileConnectorType();
    testConnectorType.setConfigKeys(fields);
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    assertNull(resp);//NULL - IF NO ERROR VALUE FOUND
  }
}
