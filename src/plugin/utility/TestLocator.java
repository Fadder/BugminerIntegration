package plugin.utility;

import java.lang.reflect.MalformedParametersException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
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
	 * 
	 * @param project das Projekt
	 * @return alle Testklassen mit vollqualifizierten Namen
	 */
	public static Collection<String> findTestClasses(IProject project) { 
		try {
			if (!project.getDescription().hasNature(JavaCore.NATURE_ID)) {
				throw new MalformedParametersException();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		IJavaProject javaProject = JavaCore.create(project);
		Set<String> collectedTestClasses = new TreeSet<>();

		try {
			for (IPackageFragment fragment : javaProject.getPackageFragments()) {
				if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit : fragment.getCompilationUnits()) {
						if(containsJUnitImport(unit)){
							String testClass=getTestClass(unit);
							if(unit!=null){
								collectedTestClasses.add(testClass);
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return collectedTestClasses;
	}

	/**
	 * Pr�ft, ob der JUnit Test in die Klasse importiert wurde
	 */
	private static boolean containsJUnitImport(ICompilationUnit unit) throws JavaModelException {
		for (IImportDeclaration imp : unit.getImports()) {
			if (imp.getElementName().equals("org.junit.Test")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt den Namen der Klasse zur�ck,
	 * wenn sie einen Test beinhaltet.
	 * 
	 * @return vollqualfizierter Namen der Klasse, die einen Test beinhaltet, null wenn kein Test vorhanden
	 */
	private static String getTestClass(ICompilationUnit unit)
			throws JavaModelException {

		for (IType type : unit.getAllTypes()) {
			for (IMethod method : type.getMethods()) {
				for (IAnnotation annotation : method.getAnnotations()) {

					String annotationName= annotation.getElementName();
					if (annotationName.equals("Test")||annotationName.equals("org.junit.Test")){
						return type.getFullyQualifiedName();
					}

				}
			}

		}
		return null;
	}
}
