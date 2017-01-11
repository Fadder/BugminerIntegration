package plugin.utility;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class BinaryLocator {

	
	public static String resolveIPath(IPath path) throws IOException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return new File(root.getFolder(path).getRawLocationURI()).getCanonicalPath();
	}
	
	
	public static Set<String> getBinaryFolders(IJavaProject javaProject){
		Set<String> binaryFolders= new HashSet<>();
		try {
		    binaryFolders.add(resolveIPath(javaProject.getOutputLocation()));
			IClasspathEntry[] classpathEntries = javaProject.getResolvedClasspath(true);
			for (IClasspathEntry entry : classpathEntries) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath binaryFolder= entry.getOutputLocation();
					if(binaryFolder!=null){
					binaryFolders.add(resolveIPath(binaryFolder));
			   	}
				}
			}
		} catch (JavaModelException| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return binaryFolders;
	}
	
	
	
}
