package plugin.utility;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class SourceNavigator {
	private IJavaProject javaProject;
	private Map<String,String[]> sourceCache=new HashMap<>();
	
	public SourceNavigator(IProject project){
		javaProject=JavaCore.create(project);		
	}
	
	public String getSourceCode(String fullyQualifiedClassName,int linenumber) throws JavaModelException{
		String[] sourceCode=sourceCache.get(fullyQualifiedClassName);
		if(sourceCode==null){
			sourceCode=javaProject.findType(fullyQualifiedClassName).getCompilationUnit().getSource().split("\n");
			sourceCache.put(fullyQualifiedClassName, sourceCode);
		}
		if(linenumber+1>=sourceCode.length){
			return null;
		}
		
		return sourceCode[linenumber+1].trim();
	}
}
