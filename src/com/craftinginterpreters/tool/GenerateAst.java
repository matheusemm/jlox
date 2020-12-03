package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output directory>");
      System.exit(64);
    }

    String outputDir = args[0];

    defineAst(
        outputDir,
        "Expr",
        Arrays.asList(
            "Assign: Token name, Expr value",
            "Binary: Expr left, Token operator, Expr right",
            "Call: Expr callee, Token paren, List<Expr> arguments",
            "Grouping: Expr expression",
            "Literal: Object value",
            "Logical: Expr left, Token operator, Expr right",
            "Unary: Token operator, Expr right",
            "Variable: Token name"));

    defineAst(
        outputDir,
        "Stmt",
        Arrays.asList(
            "Block: List<Stmt> statements",
            "Expression: Expr expression",
            "Function: Token name, List<Token> params, List<Stmt> body",
            "If: Expr condition, Stmt thenBranch, Stmt elseBranch",
            "Print: Expr expression",
            "Return: Token keyword, Expr value",
            "Var: Token name, Expr initializer",
            "While: Expr condition, Stmt body"));
  }

  private static void defineAst(String outputDir, String baseName, List<String> types)
      throws IOException {

    String path = outputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);
    writer.println("package com.craftinginterpreters.lox;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println(String.format("abstract class %s {", baseName));
    writer.println();

    // The base accept() method.
    writer.println("  abstract <R> R accept(Visitor<R> visitor);");
    writer.println();

    defineVisitor(writer, baseName, types);

    for (String type : types) {
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();

      defineType(writer, baseName, className, fields);
    }

    writer.println("}");
    writer.close();
  }

  private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
    writer.println("  interface Visitor<R> {");
    writer.println();

    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      writer.println(
          String.format(
              "    R visit%s%s(%s %s);", typeName, baseName, typeName, baseName.toLowerCase()));
    }
    writer.println("  }");
    writer.println();
  }

  private static void defineType(
      PrintWriter writer, String baseName, String className, String fieldList) {

    writer.println(String.format("  static class %s extends %s {", className, baseName));
    writer.println();

    // Fields.
    String[] fields = fieldList.split(", ");
    for (String field : fields) {
      writer.println(String.format("    final %s;", field));
    }
    writer.println();

    // Constructor.
    writer.println(String.format("    %s(%s) {", className, fieldList));

    // Store parameters in fields.
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println(String.format("      this.%s = %s;", name, name));
    }
    writer.println("    }");
    writer.println();

    writer.println("    @Override");
    writer.println("    <R> R accept(Visitor<R> visitor) {");
    writer.println(String.format("      return visitor.visit%s%s(this);", className, baseName));
    writer.println("    }");
    writer.println();

    writer.println("  }");
    writer.println();
  }
}
