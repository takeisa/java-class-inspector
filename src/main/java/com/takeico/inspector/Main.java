package com.takeico.inspector;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A CLI application that analyzes class files in a specified JAR file
 * and detects whether methods and fields are referenced by any class, including the class itself.
 */
public class Main {

    // Data structures to store class information and references
    private static final Map<String, ClassInfo> classInfoMap = new HashMap<>();
    private static final Map<String, Set<String>> methodReferences = new HashMap<>();
    private static final Map<String, Set<String>> fieldReferences = new HashMap<>();

    /**
     * Represents information about a class, including its methods and fields.
     */
    private static class ClassInfo {
        String className;
        Set<String> methods = new HashSet<>();
        Set<String> fields = new HashSet<>();

        public ClassInfo(String className) {
            this.className = className;
        }

        public void addMethod(String methodName, String descriptor) {
            methods.add(methodName + descriptor);
        }

        public void addField(String fieldName, String descriptor) {
            fields.add(fieldName + ":" + descriptor);
        }
    }

    /**
     * A ClassVisitor implementation that collects information about classes, methods, and fields.
     */
    private static class ClassInfoCollector extends ClassVisitor {
        private String className;
        private ClassInfo classInfo;
        private String superName;

        public ClassInfoCollector() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, 
                          String superName, String[] interfaces) {
            this.className = name.replace('/', '.');
            this.classInfo = new ClassInfo(className);
            this.superName = superName != null ? superName.replace('/', '.') : null;
            classInfoMap.put(className, classInfo);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, 
                                      String signature, Object value) {
            // Skip private fields as they can't be referenced from outside
            if ((access & Opcodes.ACC_PRIVATE) == 0) {
                classInfo.addField(name, descriptor);
            }
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                        String signature, String[] exceptions) {
            // Skip private methods as they can't be referenced from outside
            if ((access & Opcodes.ACC_PRIVATE) == 0) {
                classInfo.addMethod(name, descriptor);
            }
            return null;
        }
    }

    /**
     * A ClassVisitor implementation that detects references to methods and fields from any class, including the class itself.
     */
    private static class ReferenceDetector extends ClassVisitor {
        private String className;
        private String superName;
        private String[] interfaces;

        public ReferenceDetector() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, 
                          String superName, String[] interfaces) {
            this.className = name.replace('/', '.');
            this.superName = superName != null ? superName.replace('/', '.') : null;

            if (interfaces != null) {
                this.interfaces = new String[interfaces.length];
                for (int i = 0; i < interfaces.length; i++) {
                    this.interfaces[i] = interfaces[i].replace('/', '.');
                }
            } else {
                this.interfaces = new String[0];
            }
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                        String signature, String[] exceptions) {
            return new MethodReferenceVisitor(className, superName, interfaces);
        }
    }

    /**
     * A MethodVisitor implementation that detects references to methods and fields from any class, including the class itself.
     */
    private static class MethodReferenceVisitor extends MethodVisitor {
        private final String className;
        private final String superName;
        private final String[] interfaces;

        public MethodReferenceVisitor(String className, String superName, String[] interfaces) {
            super(Opcodes.ASM9);
            this.className = className;
            this.superName = superName;
            this.interfaces = interfaces;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            String ownerClass = owner.replace('/', '.');

            // Skip java.lang.Object methods and constructors
            if (!ownerClass.equals("java.lang.Object") && 
                !name.equals("<init>") && 
                !name.equals("<clinit>")) {

                // Check if the method exists in the target class
                String methodKey = ownerClass + "#" + name + descriptor;
                methodReferences.computeIfAbsent(methodKey, k -> new HashSet<>()).add(className);
            }
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            String ownerClass = owner.replace('/', '.');

            // Skip references to java.lang.Object fields
            if (!ownerClass.equals("java.lang.Object")) {
                String fieldKey = ownerClass + "#" + name + ":" + descriptor;
                fieldReferences.computeIfAbsent(fieldKey, k -> new HashSet<>()).add(className);
            }
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            // Track instantiation of classes (new operator)
            if (opcode == Opcodes.NEW) {
                String typeName = type.replace('/', '.');
                // Consider constructor reference as a method reference
                String methodKey = typeName + "#<init>()V";
                methodReferences.computeIfAbsent(methodKey, k -> new HashSet<>()).add(className);
            }
            super.visitTypeInsn(opcode, type);
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
                // First pass: collect information about all classes, methods, and fields
                collectClassInfo(jarPath);

                // Second pass: detect references between classes
                detectReferences(jarPath);

                // Print results
                printResults(jarPath);
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
     * Collects information about all classes, methods, and fields in the specified JAR file.
     *
     * @param jarPath Path to the JAR file
     * @throws IOException If there's an error reading the JAR file
     */
    private static void collectClassInfo(String jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath)) {
            System.out.println("Collecting class information from " + jarPath + "...");

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Only process .class files and skip directories
                if (name.endsWith(".class") && !entry.isDirectory()) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        // Use ASM to read the class file
                        ClassReader reader = new ClassReader(is);
                        ClassInfoCollector collector = new ClassInfoCollector();

                        // Accept the visitor to collect class information
                        reader.accept(collector, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    } catch (Exception e) {
                        System.err.println("Error collecting information from class " + name + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Detects references to methods and fields between classes in the specified JAR file.
     *
     * @param jarPath Path to the JAR file
     * @throws IOException If there's an error reading the JAR file
     */
    private static void detectReferences(String jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath)) {
            System.out.println("Detecting references between classes in " + jarPath + "...");

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Only process .class files and skip directories
                if (name.endsWith(".class") && !entry.isDirectory()) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        // Use ASM to read the class file
                        ClassReader reader = new ClassReader(is);
                        ReferenceDetector detector = new ReferenceDetector();

                        // Accept the visitor to detect references
                        reader.accept(detector, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    } catch (Exception e) {
                        System.err.println("Error detecting references in class " + name + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Prints the results of the analysis, showing which methods and fields are referenced by other classes.
     *
     * @param jarPath Path to the JAR file
     */
    private static void printResults(String jarPath) {
        System.out.println("\nResults for " + jarPath + ":");
        System.out.println("=".repeat(80));

        // Process each class
        for (ClassInfo classInfo : classInfoMap.values()) {
            // Skip java.* and javax.* classes to reduce noise
            if (classInfo.className.startsWith("java.") || classInfo.className.startsWith("javax.")) {
                continue;
            }

            System.out.println("\nClass: " + classInfo.className);
            System.out.println("-".repeat(80));

            // Check methods
            if (!classInfo.methods.isEmpty()) {
                System.out.println("  Methods:");

                int referencedCount = 0;
                int unreferencedCount = 0;

                for (String method : classInfo.methods) {
                    String methodKey = classInfo.className + "#" + method;
                    Set<String> referencingClasses = methodReferences.get(methodKey);

                    if (referencingClasses != null && !referencingClasses.isEmpty()) {
                        referencedCount++;
                        // Format method name and descriptor for better readability
                        String formattedMethod = formatMethod(method);
                        System.out.println("    ✓ " + formattedMethod);
                        System.out.println("      Referenced by: " + formatReferencingClasses(referencingClasses));
                    } else {
                        unreferencedCount++;
                        // Format method name and descriptor for better readability
                        String formattedMethod = formatMethod(method);
                        System.out.println("    ✗ " + formattedMethod);
                        System.out.println("      Not referenced by any class");
                    }
                }

                System.out.println("  Summary: " + referencedCount + " methods referenced, " + 
                                  unreferencedCount + " methods not referenced");
            } else {
                System.out.println("  No public/protected methods found");
            }

            // Check fields
            if (!classInfo.fields.isEmpty()) {
                System.out.println("  Fields:");

                int referencedCount = 0;
                int unreferencedCount = 0;

                for (String field : classInfo.fields) {
                    String fieldKey = classInfo.className + "#" + field;
                    Set<String> referencingClasses = fieldReferences.get(fieldKey);

                    if (referencingClasses != null && !referencingClasses.isEmpty()) {
                        referencedCount++;
                        // Format field name and descriptor for better readability
                        String formattedField = formatField(field);
                        System.out.println("    ✓ " + formattedField);
                        System.out.println("      Referenced by: " + formatReferencingClasses(referencingClasses));
                    } else {
                        unreferencedCount++;
                        // Format field name and descriptor for better readability
                        String formattedField = formatField(field);
                        System.out.println("    ✗ " + formattedField);
                        System.out.println("      Not referenced by any class");
                    }
                }

                System.out.println("  Summary: " + referencedCount + " fields referenced, " + 
                                  unreferencedCount + " fields not referenced");
            } else {
                System.out.println("  No public/protected fields found");
            }
        }

        // Print overall statistics
        int totalClasses = classInfoMap.size();
        int totalMethods = 0;
        int totalFields = 0;
        int referencedMethods = 0;
        int referencedFields = 0;

        for (ClassInfo classInfo : classInfoMap.values()) {
            totalMethods += classInfo.methods.size();
            totalFields += classInfo.fields.size();

            for (String method : classInfo.methods) {
                String methodKey = classInfo.className + "#" + method;
                Set<String> referencingClasses = methodReferences.get(methodKey);
                if (referencingClasses != null && !referencingClasses.isEmpty()) {
                    referencedMethods++;
                }
            }

            for (String field : classInfo.fields) {
                String fieldKey = classInfo.className + "#" + field;
                Set<String> referencingClasses = fieldReferences.get(fieldKey);
                if (referencingClasses != null && !referencingClasses.isEmpty()) {
                    referencedFields++;
                }
            }
        }

        System.out.println("\nOverall Statistics:");
        System.out.println("=".repeat(80));
        System.out.println("Total classes: " + totalClasses);
        System.out.println("Total methods: " + totalMethods + " (Referenced: " + referencedMethods + 
                          ", Not referenced: " + (totalMethods - referencedMethods) + ")");
        System.out.println("Total fields: " + totalFields + " (Referenced: " + referencedFields + 
                          ", Not referenced: " + (totalFields - referencedFields) + ")");

        // Clear data structures for the next JAR file
        classInfoMap.clear();
        methodReferences.clear();
        fieldReferences.clear();
    }

    /**
     * Formats a method name and descriptor for better readability.
     *
     * @param methodNameAndDescriptor Method name and descriptor in the format "name(descriptor)"
     * @return Formatted method name and descriptor
     */
    private static String formatMethod(String methodNameAndDescriptor) {
        int descriptorStart = methodNameAndDescriptor.indexOf('(');
        if (descriptorStart == -1) {
            return methodNameAndDescriptor;
        }

        String name = methodNameAndDescriptor.substring(0, descriptorStart);
        String descriptor = methodNameAndDescriptor.substring(descriptorStart);

        // Try to convert the descriptor to a more readable format
        try {
            Type methodType = Type.getMethodType(descriptor);
            Type returnType = methodType.getReturnType();
            Type[] argumentTypes = methodType.getArgumentTypes();

            StringBuilder formattedDescriptor = new StringBuilder();
            formattedDescriptor.append(name).append("(");

            for (int i = 0; i < argumentTypes.length; i++) {
                if (i > 0) {
                    formattedDescriptor.append(", ");
                }
                formattedDescriptor.append(getSimpleTypeName(argumentTypes[i].getClassName()));
            }

            formattedDescriptor.append("): ").append(getSimpleTypeName(returnType.getClassName()));

            return formattedDescriptor.toString();
        } catch (Exception e) {
            // If there's an error parsing the descriptor, return the original
            return name + descriptor;
        }
    }

    /**
     * Formats a field name and descriptor for better readability.
     *
     * @param fieldNameAndDescriptor Field name and descriptor in the format "name:descriptor"
     * @return Formatted field name and descriptor
     */
    private static String formatField(String fieldNameAndDescriptor) {
        int descriptorStart = fieldNameAndDescriptor.indexOf(':');
        if (descriptorStart == -1) {
            return fieldNameAndDescriptor;
        }

        String name = fieldNameAndDescriptor.substring(0, descriptorStart);
        String descriptor = fieldNameAndDescriptor.substring(descriptorStart + 1);

        // Try to convert the descriptor to a more readable format
        try {
            Type fieldType = Type.getType(descriptor);
            return name + ": " + getSimpleTypeName(fieldType.getClassName());
        } catch (Exception e) {
            // If there's an error parsing the descriptor, return the original
            return name + ": " + descriptor;
        }
    }

    /**
     * Gets the simple name of a class (without package).
     *
     * @param className Fully qualified class name
     * @return Simple class name
     */
    private static String getSimpleTypeName(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot == -1) {
            return className;
        }
        return className.substring(lastDot + 1);
    }

    /**
     * Formats a set of referencing classes for better readability.
     *
     * @param referencingClasses Set of referencing classes
     * @return Formatted string of referencing classes
     */
    private static String formatReferencingClasses(Set<String> referencingClasses) {
        if (referencingClasses.size() <= 3) {
            return String.join(", ", referencingClasses);
        } else {
            return referencingClasses.size() + " classes including " + 
                   String.join(", ", referencingClasses.stream().limit(3).toArray(String[]::new)) + ", ...";
        }
    }
}
