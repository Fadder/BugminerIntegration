package plugin.utility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class ClasspathResolver {
	private static String separator = System.getProperty("os.name").toLowerCase().contains("windows") ? ";" : ":";

	public static String getClasspath(IProject project) {
		IJavaProject javaProject = null;
		try {
			if (!project.getDescription().hasNature(JavaCore.NATURE_ID)) {
				throw new MalformedParametersException();
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		javaProject = JavaCore.create(project);

		StringBuilder classpathBuilder = new StringBuilder();
		classpathBuilder.append('\"');

		for (String dependencies : getExternalDependencies(javaProject)) {
			classpathBuilder.append(dependencies).append(separator);
		}
		for (String dependencies : getExecutorDependencies()) {
			classpathBuilder.append(dependencies).append(separator);
		}
		for (String dependencies : BinaryLocator.getBinaryFolders(javaProject)) {
			classpathBuilder.append(dependencies).append(separator);
		}
		classpathBuilder.deleteCharAt(classpathBuilder.length() - 1);

		classpathBuilder.append('\"');
		return classpathBuilder.toString();
	}

	private static List<String> getExternalDependencies(IJavaProject javaProject) {
		List<String> externalDependencies = new ArrayList<>();

		try {
			for (IClasspathEntry entry : javaProject.getRawClasspath()) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
					entry = JavaCore.getResolvedClasspathEntry(entry);
				}
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					externalDependencies.add(entry.getPath().toString());
				}
				if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), javaProject);
					if (container.getKind() == IClasspathContainer.K_APPLICATION) {
						for (IClasspathEntry innerEntry : container.getClasspathEntries()) {
							if (innerEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
								externalDependencies.add(innerEntry.getPath().toString());
							}
						}
					}
				}

			}

		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// try {
		// IClasspathEntry[] classpathEntries =
		// javaProject.getResolvedClasspath(true);
		// for (IClasspathEntry entry : classpathEntries) {
		// if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
		// externalDependencies.add(entry.getPath().toString());
		// }
		// }
		// } catch (JavaModelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return externalDependencies;
	}

	private static List<String> getExecutorDependencies() {
		List<String> executorDependencies = new ArrayList<>();

		Bundle bundle = FrameworkUtil.getBundle(ClasspathResolver.class);
		if (bundle == null) {
			System.err.println("can't find bundle");
		}
		URL jarFile = bundle.getEntry("JavaExecutionMonitorPrototyp3.jar");
		URL binaryFolder = bundle.getEntry("bin");

		try {
			executorDependencies.add(new File(FileLocator.toFileURL(jarFile).getFile()).getCanonicalPath());
			executorDependencies.add(new File(FileLocator.toFileURL(binaryFolder).getFile()).getCanonicalPath());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return executorDependencies;
	}
}
