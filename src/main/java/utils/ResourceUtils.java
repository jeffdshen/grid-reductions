package utils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

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

    public static File getRelativeFile(Class clazz, String resourceName) throws ResourceNotFoundException {
        try{
            return new File(Paths.get(clazz.getResource(".").toURI()).toFile(), resourceName);
        } catch (URISyntaxException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    public static File getAbsoluteFile(Class clazz, String resourceName) throws ResourceNotFoundException {
        try{
            return new File(Paths.get(clazz.getResource("/").toURI()).toFile(), resourceName);
        } catch (URISyntaxException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    public static class ResourceNotFoundException extends IOException {
        public ResourceNotFoundException() {
            super();
        }

        public ResourceNotFoundException(String s) {
            super(s);
        }

        public ResourceNotFoundException(Exception e) {
            super(e);
        }
    }
}
