package com.rest4j;

import com.rest4j.impl.model.Endpoint;

/**
 * @author Rinat Gainullin (grif@ecwid.com)
 */
public interface PermissionChecker {
	void check(Endpoint endpoint, ApiRequest request) throws ApiException;
}
