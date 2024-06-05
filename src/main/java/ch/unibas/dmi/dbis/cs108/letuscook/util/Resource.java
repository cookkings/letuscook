package ch.unibas.dmi.dbis.cs108.letuscook.util;

import ch.unibas.dmi.dbis.cs108.letuscook.Main;
import java.net.URL;

/**
 * Resource provides utility methods for accessing resources within the project.
 */
public class Resource {

	/**
	 * Retrieves the URL of a resource located at the specified path.
	 *
	 * @param path The path to the resource, relative to the root of the project.
	 * @return The URL of the resource.
	 * @throws NullPointerException if the resource at the specified path is not found.
	 */
	public static String get(String path) {
		URL url = Main.class.getResource("/" + path);

		assert url != null : "Missing resource: \"" + path + "\"";

		return url.toExternalForm();
	}
}
