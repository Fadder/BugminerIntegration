package plugin.utility;

import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class TestLocator {
	

	public static List<String> findTestClasses(IProject project){
		List<String> fullyQualifiedTestClasses = new ArrayList<>();
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
		boolean found=false;
		try {
			for(IPackageFragment fragment:javaProject.getPackageFragments()){
				if(fragment.getKind()==IPackageFragmentRoot.K_SOURCE){
					for(ICompilationUnit unit:fragment.getCompilationUnits()){
						for(IType type: unit.getAllTypes()){
							for(IMethod method: type.getMethods()){
								for(IAnnotation annotation: method.getAnnotations()){
									if(annotation.getElementName().equals("Test")){
										found=true;
									}
									if(found){break;}
								}
								if(found){break;}
							}
							if(found){fullyQualifiedTestClasses.add(type.getFullyQualifiedName()) ;break;}
						}
						if(found){
							found=false;
						}
					}
				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fullyQualifiedTestClasses;
	}
}
