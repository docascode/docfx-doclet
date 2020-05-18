package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TypeParameter;
import com.microsoft.util.Utils;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

public class ClassLookup extends BaseLookup<TypeElement> {

    private static final String JAVA_LANG_OBJECT = "java.lang.Object";

    public ClassLookup(DocletEnvironment environment) {
        super(environment);
    }

    @Override
    protected ExtendedMetadataFileItem buildMetadataFileItem(TypeElement classElement) {
        List<ExtendedMetadataFileItem> inheritedMethods = new ArrayList<>();

        String packageName = determinePackageName(classElement);
        String classQName = String.valueOf(classElement.getQualifiedName());
        String classSName = String.valueOf(classElement.getSimpleName());
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        String classSNameWithGenericsSupport = classQNameWithGenericsSupport.replace(packageName.concat("."), "");

        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem(classQName);
        result.setId(classSName);
        result.setParent(packageName);
        result.setHref(classQName + ".yml");
        result.setName(classSNameWithGenericsSupport);
        result.setNameWithType(classSNameWithGenericsSupport);
        result.setFullName(classQNameWithGenericsSupport);
        result.setType(determineType(classElement));
        result.setPackageName(packageName);
        result.setSummary(determineComment(classElement));
        result.setSuperclass(determineNestedSuperclass(classElement, result, inheritedMethods));
        result.setTypeParameters(determineTypeParameters(classElement));
        result.setInheritedMethods(determineInheritedMembers(inheritedMethods));
        populateContent(classElement, classSNameWithGenericsSupport, result);
        result.setTocName(classQName.replace(packageName.concat("."), ""));

        return result;
    }

    void populateContent(TypeElement classElement, String shortNameWithGenericsSupport,
                         ExtendedMetadataFileItem container) {
        String type = elementKindLookup.get(classElement.getKind());
        String result = String.format("%s %s %s",
                classElement.getModifiers().stream().map(String::valueOf)
                        .filter(modifier -> !("Interface".equals(type) && "abstract".equals(modifier)))
                        .filter(modifier -> !("Enum".equals(type) && ("static".equals(modifier) || "final".equals(modifier))))
                        .collect(Collectors.joining(" ")),
                StringUtils.lowerCase(type), shortNameWithGenericsSupport);

        String superclass = determineSuperclass(classElement);
        if (superclass != null && !JAVA_LANG_OBJECT.equals(superclass)) {
            result += " extends " + makeTypeShort(superclass);

            addSuperclassToReferencesMap(superclass, container);
        }

        List<? extends TypeMirror> interfaces = classElement.getInterfaces();
        if (CollectionUtils.isNotEmpty(interfaces)) {
            String prefix = (classElement.getKind() == ElementKind.INTERFACE) ? " extends " : " implements ";
            result += prefix + interfaces.stream().map(String::valueOf).map(this::makeTypeShort)
                    .collect(Collectors.joining(", "));

            container.setInterfaces(interfaces.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList()));

            addInterfacesToReferencesMap(interfaces, container);

        }
        addInheritedMethodsToReferencesMap(container);
        container.setContent(result);
    }

    void addSuperclassToReferencesMap(String superclass, ExtendedMetadataFileItem container) {
        container.addReferences(Set.of(new MetadataFileItem(superclass, makeTypeShort(superclass), false)));
    }

    void addInheritedMethodsToReferencesMap(ExtendedMetadataFileItem container) {
        container.addReferences(container.getInheritedMethods().stream()
                .map(o -> new MetadataFileItem(o, makeTypeShort(o), false))
                .collect(Collectors.toSet())
        );
    }

    void addInterfacesToReferencesMap(List<? extends TypeMirror> interfaces, ExtendedMetadataFileItem container) {
        container.addReferences(interfaces.stream()
                .map(String::valueOf)
                .map(o -> new MetadataFileItem(o, makeTypeShort(o), false))
                .collect(Collectors.toSet())
        );
    }

    String determineSuperclass(TypeElement classElement) {
        TypeMirror superclass = classElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return null;
        }
        return String.valueOf(superclass);
    }

    List<String> determineNestedSuperclass(TypeElement classElement, ExtendedMetadataFileItem result, List<ExtendedMetadataFileItem> inheritedMethods) {
        List<String> nestedList = new ArrayList<>();

        if (result.getSuperclass() != null) {
            nestedList = result.getSuperclass();
        }

        TypeMirror superclass = classElement.getSuperclass();
        if (superclass.getKind() != TypeKind.NONE) {
            TypeElement superClassElement = (TypeElement) environment.getTypeUtils().asElement(superclass);

            nestedList.add(superClassElement.getQualifiedName().toString());
            result.setSuperclass(nestedList);
            appendInheritedMethods(superClassElement, inheritedMethods);

            determineNestedSuperclass(superClassElement, result, inheritedMethods);
        }
        return nestedList;
    }

    List<TypeParameter> determineTypeParameters(TypeElement element) {
        return element.getTypeParameters().stream()
                .map(typeParameter -> new TypeParameter(String.valueOf(typeParameter)))
                .collect(Collectors.toList());
    }

    void appendInheritedMethods(TypeElement element, List<ExtendedMetadataFileItem> inheritedMethods) {
        List<? extends Element> members = element.getEnclosedElements();
        Integer level = Optional.ofNullable(getMaxNestedLevel(inheritedMethods))
                .orElse(0);

        for (Element m : members) {
            if (m.getKind() == ElementKind.METHOD && !Utils.isPrivateOrPackagePrivate(m)) {
                String uid = element.getQualifiedName().toString().concat(".") + String.valueOf(m);

                ExtendedMetadataFileItem item = new ExtendedMetadataFileItem(uid);
                item.setName(String.valueOf(m));
                item.setNestedLevel(level + 1);

                inheritedMethods.add(item);
            }
        }
    }

    Integer getMaxNestedLevel(List<ExtendedMetadataFileItem> inheritedMethods) {
        Integer level = 0;

        if (inheritedMethods.size() > 0) {
            level = inheritedMethods
                    .stream()
                    .mapToInt(v -> v.getNestedLevel())
                    .max().orElseThrow(NoSuchElementException::new);
        }
        return level;
    }

    List<String> determineInheritedMembers(List<ExtendedMetadataFileItem> inheritedMethods) {

        if (inheritedMethods.size() > 0) {
            HashMap<String, ExtendedMetadataFileItem> map = new HashMap<>();
            for (ExtendedMetadataFileItem item : inheritedMethods
            ) {
                String key = item.getName();

                if (map.containsKey(key) && map.get(key).getNestedLevel() > item.getNestedLevel()) {
                    // child class will have smaller than superclass, we only need the nearest methods inherited with same signature
                    map.put(key, item);
                } else if (!map.containsKey(key)) {
                    map.put(key, item);
                }
            }
            List<String> methods = map.values()
                    .stream()
                    .map(x -> x.getUid())
                    .collect(Collectors.toList());

            return methods;
        }
        return new ArrayList<>();
    }
}
