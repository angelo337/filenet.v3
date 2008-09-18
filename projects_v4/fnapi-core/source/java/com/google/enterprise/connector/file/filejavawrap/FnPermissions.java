package com.google.enterprise.connector.file.filejavawrap;

import java.util.Iterator;

import com.filenet.api.collection.GroupSet;
import com.filenet.api.collection.PermissionList;
import com.filenet.api.collection.UserSet;
import com.filenet.api.constants.AccessLevel;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.core.Connection;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.security.Group;
import com.filenet.api.security.User;
import com.google.enterprise.connector.file.filewrap.IPermissions;

public class FnPermissions implements IPermissions {

	PermissionList perms;

	public static int ACCESS_LEVEL = AccessLevel.VIEW_AS_INT;

	public FnPermissions(PermissionList perms) {	
		this.perms = perms;
	}

	public boolean authorize(String username) {
		boolean found;
		Iterator iter = perms.iterator();
		String granteeName;
		while(iter.hasNext()){
			AccessPermission perm = (AccessPermission)iter.next();
			
			if ((perm.get_AccessMask().intValue() & ACCESS_LEVEL) == ACCESS_LEVEL){
				granteeName = perm.get_GranteeName();
				
				if (perm.get_GranteeType() == SecurityPrincipalType.USER){
					if (granteeName.equalsIgnoreCase(username) 
							|| granteeName.split("@")[0].equalsIgnoreCase(username)) 
						return true;
				}
				else if(perm.get_GranteeType() == SecurityPrincipalType.GROUP){ //GROUP
					Connection conn = perm.getConnection();
					Group group = com.filenet.api.core.Factory.Group.fetchInstance(conn,perm.get_GranteeName(),null);
					found = searchUserInGroup(username,group);
					if (found) return true;
				}	
			}
		}
		return false;
	}

	private boolean searchUserInGroup(String username, Group group) {
		User user;
		Group subGroup;
		boolean found;
		UserSet us = group.get_Users();
		Iterator itUser = us.iterator();
		while (itUser.hasNext()){
			user = (User) itUser.next();
			if (user.get_Name().equalsIgnoreCase(username) 
					|| user.get_Name().split("@")[0].equalsIgnoreCase(username)) {
				return true;
			}					
		}
		GroupSet gs = group.get_Groups();
		Iterator itGroup = gs.iterator();
		while (itGroup.hasNext())
		{
			subGroup = (Group) itGroup.next();
			found = searchUserInGroup(username, subGroup);
			if (found) return true; 
		}
		return false;
	}

	
}