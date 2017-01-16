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
	
	/**
	 * Findet alle Testklassen f�r das Projekt
	 * @param project das Projekt
	 * @return alle Testklassen mit vollqualifizierten Namen
	 */
	public static List<String> findTestClasses(IProject project){
		List<String> fullyQualifiedTestClasses = new ArrayList<>();
		IJavaProject javaProject=null;
		// Wir untersuchen nur java Projekte
		try {
			if (!project.getDescription().hasNature(JavaCore.NATURE_ID)) {
				throw new MalformedParametersException();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		javaProject=JavaCore.create(project);
		
		
		boolean found=false;
		
		
		// Findet �ber die Eclipse API alle Klassen, die eine Methode haben, die mit @Test annotiert ist
		// TODO das hier evtl aufl�sen bzw einen besseren Weg finden die Testklassen sicher zu differenzieren??
		try {
			for(IPackageFragment fragment:javaProject.getPackageFragments()){
				if(fragment.getKind()==IPackageFragmentRoot.K_SOURCE){
					//gehe durch alle Package Fragmente, die source dateien enthalten
					for(ICompilationUnit unit:fragment.getCompilationUnits()){
						for(IType type: unit.getAllTypes()){
							// durch alle Klassen
							for(IMethod method: type.getMethods()){
								// durch alle Methoden
								for(IAnnotation annotation: method.getAnnotations()){
									// testen, ob eine @Test Annotation besteht
									if(annotation.getElementName().equals("Test")){
										found=true;
										fullyQualifiedTestClasses.add(type.getFullyQualifiedName());
									}
									if(found){break;}
								}
								if(found){break;}
							}
							if(found){break;}
						}
						if(found){	found=false;}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return fullyQualifiedTestClasses;
	}
}
