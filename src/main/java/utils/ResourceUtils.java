package utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ResourceUtils {
    public static Reader getReader(String resourceName) throws ResourceNotFoundException {
        return getReader(ResourceUtils.class, resourceName);
    }

    public static Reader getReader(Class clazz, String resourceName) throws ResourceNotFoundException {
        InputStream stream = clazz.getResourceAsStream(resourceName);
        if (stream == null) {
            throw new ResourceNotFoundException();
        }
        return new InputStreamReader(stream);
    }

    public static class ResourceNotFoundException extends Exception {
        public ResourceNotFoundException() {
            super();
        }

        public ResourceNotFoundException(String s) {
            super(s);
        }
    }
}
