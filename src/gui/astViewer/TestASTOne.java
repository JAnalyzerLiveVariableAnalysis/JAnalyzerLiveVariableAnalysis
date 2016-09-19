package gui.astViewer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.resource.JFaceResources;

public class TestASTOne {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFaceResources.getFontRegistry();
	}

	@SuppressWarnings("deprecation")
	public static void testParseString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		pw.println("package example;");
		pw.println("public class HelloWord {");
		pw.println("\tpublic static void main(String[] args) {");
		pw.println("\t\tSystem.out.println(\"Hello World !\");");
		pw.println("\t}");
		pw.println("}");
		
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(sw.toString().toCharArray());
		
		CompilationUnit cu = (CompilationUnit)parser.createAST(null);
		System.out.println(cu);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public static void testCreateProgram() {
		
		AST ast = AST.newAST(AST.JLS4);
		CompilationUnit unit = ast.newCompilationUnit();
		PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
		packageDeclaration.setName(ast.newSimpleName("example"));
		unit.setPackage(packageDeclaration);

		ImportDeclaration importDeclaration = ast.newImportDeclaration();
		QualifiedName name = ast.newQualifiedName(ast.newSimpleName("java"), ast.newSimpleName("util"));
		importDeclaration.setName(name);
		importDeclaration.setOnDemand(true);
		unit.imports().add(importDeclaration);

		TypeDeclaration type = ast.newTypeDeclaration();
		type.setInterface(false);
		type.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		type.setName(ast.newSimpleName("HelloWorld"));

		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setConstructor(false);
		List<Modifier> modifiers = methodDeclaration.modifiers();
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
		methodDeclaration.setName(ast.newSimpleName("main"));
		methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		SingleVariableDeclaration variableDeclaration = ast.newSingleVariableDeclaration();
		variableDeclaration.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String"))));
		variableDeclaration.setName(ast.newSimpleName("args"));
		methodDeclaration.parameters().add(variableDeclaration);

		org.eclipse.jdt.core.dom.Block block = ast.newBlock();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		name = ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out"));
		methodInvocation.setExpression(name);
		methodInvocation.setName(ast.newSimpleName("println")); 
		InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		StringLiteral literal = ast.newStringLiteral();
		literal.setLiteralValue("Hello");
		infixExpression.setLeftOperand(literal);
		literal = ast.newStringLiteral();
		literal.setLiteralValue(" world");
		infixExpression.setRightOperand(literal);
		methodInvocation.arguments().add(infixExpression);
		ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);

		block.statements().add(expressionStatement);
		methodDeclaration.setBody(block);
		type.bodyDeclarations().add(methodDeclaration);

		unit.types().add(type);
		
		System.out.println(unit);
	}
}
