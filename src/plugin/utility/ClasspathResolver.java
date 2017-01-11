package plugin.utility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class ClasspathResolver {
	private static String separator = System.getProperty("os.name").toLowerCase().contains("windows") ? ";" : ":";

	/**
	 * Gibt den classpath der zum debuggen des Projekts notwendig ist zurück
	 * 
	 * @param project
	 *            das Projekt das debuggt werden soll
	 * @return der classpath zum project
	 */
	public static String getClasspath(IProject project) {
		IJavaProject javaProject = null;
		try {
			if (!project.getDescription().hasNature(JavaCore.NATURE_ID)) {
				throw new MalformedParametersException();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		javaProject = JavaCore.create(project);

		StringBuilder classpathBuilder = new StringBuilder();
		classpathBuilder.append('\"');

		for (String dependencies : getProjectDependencies(javaProject)) {
			classpathBuilder.append(dependencies).append(separator);
		}
		for (String dependencies : getExecutorDependencies()) {
			classpathBuilder.append(dependencies).append(separator);
		}

		classpathBuilder.deleteCharAt(classpathBuilder.length() - 1);

		classpathBuilder.append('\"');
		return classpathBuilder.toString();
	}

	/**
	 * Gibt alle Dependencies fuer das gewaehlte Projekt zurueck
	 */
	private static List<String> getProjectDependencies(IJavaProject javaProject) {
		List<String> projectDependencies = new ArrayList<>();

		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			for (IClasspathEntry entry : javaProject.getRawClasspath()) {
				// Variablen aufloesen
				if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
					entry = JavaCore.getResolvedClasspathEntry(entry);
				}
				// fuege zum Pfad hinzu, wenn es eine Bibliothek ist
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					projectDependencies.add(entry.getPath().toString());
				}

				// Wenn es eine source Ordnet, fuege den zugehoerigen Binary ordner hinzu
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath binaryFolder = entry.getOutputLocation();
					if (binaryFolder == null) {
						binaryFolder = javaProject.getOutputLocation();
					}
					projectDependencies.add(new File(root.getFolder(binaryFolder).getRawLocationURI()).getCanonicalPath());

				}
				// wenn es ein Container ist, gehe durch den Container und fuege alle Bibliotheken hinzu
				if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), javaProject);
					if (container.getKind() == IClasspathContainer.K_APPLICATION) {
						for (IClasspathEntry innerEntry : container.getClasspathEntries()) {
							if (innerEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
								projectDependencies.add(innerEntry.getPath().toString());
							}
						}
					}
				}

			}

		} catch (JavaModelException | IOException e1) {
			e1.printStackTrace();
		}

		return projectDependencies;
	}

	/**
	 * Gibt alle Dependencies aus unserem eigenen Plugin zurueck, die wir zum Starten des
	 * TestsuitExecutor brauchen
	 */
	private static List<String> getExecutorDependencies() {
		List<String> executorDependencies = new ArrayList<>();

		// hole das Bundle von unserem Plugin
		Bundle bundle = FrameworkUtil.getBundle(ClasspathResolver.class);
		if (bundle == null) {
			System.err.println("can't find bundle");
		}
		
		// Suche nach dem Plugin binary Ordner und der Bibliothek
		URL jarFile = bundle.getEntry("JavaExecutionMonitorPrototyp3.jar");
		URL binaryFolder = bundle.getEntry("bin");

		
		// Fuege es dem Pfad hinzu
		try {
			executorDependencies.add(new File(FileLocator.toFileURL(jarFile).getFile()).getCanonicalPath());
			executorDependencies.add(new File(FileLocator.toFileURL(binaryFolder).getFile()).getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return executorDependencies;
	}
}
