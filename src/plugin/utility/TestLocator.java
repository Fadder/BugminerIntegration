package plugin.utility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class TestLocator {
	

	
	private static List<String> searchFoldersForTests(Set<String> foldersToCheck){
		List<String> testClasses = new ArrayList<>();
		
		for(String folder: foldersToCheck){
			Path sourceFolder= new File(folder).toPath();
		try {
			Files.walk(sourceFolder).filter(file -> file.getFileName().toString().endsWith(".class"))
			.map(path -> sourceFolder.relativize(path).toString())
			.filter(x-> !x.contains("$"))
			.map(path -> path.replace('/', '.')).map(path -> path.replace('\\', '.'))
			.map(name -> name.substring(0, name.length() - 6)).forEach(x -> testClasses.add(x));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		

		return testClasses;
	}
	

	public static List<String> findTestClasses(IProject project){
		
		IJavaProject javaProject=null;
		try {
			if (!project.getDescription().hasNature(JavaCore.NATURE_ID)) {
				throw new MalformedParametersException();
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		javaProject=JavaCore.create(project);
		Set<String> foldersToCheck=BinaryLocator.getBinaryFolders(javaProject);	
		
		return searchFoldersForTests(foldersToCheck);
	}
}
