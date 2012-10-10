package org.dcache.webdav;

import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.Resource;

/**
 * Corresponds to a 403 Forbidden response.
 */
public class ForbiddenException extends WebDavException
{
    private static final long serialVersionUID = 2424942763825032362L;

    public ForbiddenException(Resource resource)
    {
        super(resource);
    }

    public ForbiddenException(String message, Resource resource)
    {
        super(message, resource);
    }

    public ForbiddenException(String message, Throwable cause, Resource resource)
    {
        super(message, cause, resource);
    }
}