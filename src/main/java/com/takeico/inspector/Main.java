package com.takeico.inspector;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A CLI application that lists all class files in a specified JAR file
 * and displays their field names and method names using ASM.
 */
public class Main {

    /**
     * A ClassVisitor implementation that extracts and prints field and method information.
     */
    private static class ClassInspectorVisitor extends ClassVisitor {
        private String className;

        public ClassInspectorVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, 
                          String superName, String[] interfaces) {
            this.className = name.replace('/', '.');
            System.out.println("\nClass: " + className);
            if (superName != null) {
                System.out.println("  Superclass: " + superName.replace('/', '.'));
            }
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, 
                                      String signature, Object value) {
            System.out.println("  Field: " + name + " (" + descriptor + ")");
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                        String signature, String[] exceptions) {
            System.out.println("  Method: " + name + " " + descriptor);
            return null;
        }
    }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar class-inspector.jar <path-to-jar-file> [<path-to-jar-file> ...]");
            System.exit(1);
        }

        boolean hasErrors = false;

        for (String jarPath : args) {
            File jarFile = new File(jarPath);

            if (!jarFile.exists() || !jarFile.isFile()) {
                System.err.println("Error: The specified JAR file does not exist or is not a file: " + jarPath);
                hasErrors = true;
                continue;
            }

            try {
                listClassesInJar(jarPath);
            } catch (IOException e) {
                System.err.println("Error reading JAR file " + jarPath + ": " + e.getMessage());
                hasErrors = true;
            }
        }

        if (hasErrors) {
            System.exit(1);
        }
    }

    /**
     * Lists all class files in the specified JAR file and inspects their fields and methods using ASM.
     *
     * @param jarPath Path to the JAR file
     * @throws IOException If there's an error reading the JAR file
     */
    private static void listClassesInJar(String jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath)) {
            System.out.println("Inspecting classes in " + jarPath + ":");

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Only process .class files and skip directories
                if (name.endsWith(".class") && !entry.isDirectory()) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        // Use ASM to read the class file
                        ClassReader reader = new ClassReader(is);
                        ClassInspectorVisitor visitor = new ClassInspectorVisitor();

                        // Accept the visitor to extract field and method information
                        reader.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    } catch (Exception e) {
                        System.err.println("Error inspecting class " + name + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}
