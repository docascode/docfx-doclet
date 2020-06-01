package com.microsoft.build;

import com.microsoft.lookup.BaseLookup;
import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.*;
import com.microsoft.util.ElementUtil;
import com.microsoft.util.FileUtil;
import com.microsoft.util.Utils;
import com.microsoft.util.YamlUtil;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class YmlFilesBuilder {

    private final static String[] LANGS = {"java"};
    private final Pattern XREF_LINK_PATTERN = Pattern.compile("<xref uid=\".*?\" .*?>.*?</xref>");
    private final Pattern XREF_LINK_CONTENT_PATTERN = Pattern.compile("(?<=<xref uid=\").*?(?=\" .*?>.*?</xref>)");
    private final Pattern XREF_LINK_RESOLVE_PATTERN = Pattern.compile("(?<class>\\w+)\\#(?<member>\\w+)(\\((?<param>.*)\\))?");

    private DocletEnvironment environment;
    private String outputPath;
    private ElementUtil elementUtil;
    private PackageLookup packageLookup;
    private ClassItemsLookup classItemsLookup;
    private ClassLookup classLookup;

    public YmlFilesBuilder(DocletEnvironment environment, String outputPath,
                           String[] excludePackages, String[] excludeClasses) {
        this.environment = environment;
        this.outputPath = outputPath;
        this.elementUtil = new ElementUtil(excludePackages, excludeClasses);
        this.packageLookup = new PackageLookup(environment);
        this.classItemsLookup = new ClassItemsLookup(environment);
        this.classLookup = new ClassLookup(environment);
    }

    public boolean build() {
        List<MetadataFile> packageMetadataFiles = new ArrayList<>();
        List<MetadataFile> classMetadataFiles = new ArrayList<>();

        TocFile tocFile = new TocFile(outputPath);
        for (PackageElement packageElement : elementUtil.extractPackageElements(environment.getIncludedElements())) {
            String uid = packageLookup.extractUid(packageElement);
            packageMetadataFiles.add(buildPackageMetadataFile(packageElement));

            TocItem packageTocItem = new TocItem(uid, uid);
            buildFilesForInnerClasses(packageElement, packageTocItem.getItems(), classMetadataFiles);
            tocFile.addTocItem(packageTocItem);
        }

        for (MetadataFile packageFile : packageMetadataFiles) {
            String packageFileName = packageFile.getFileName();
            for (MetadataFile classFile : classMetadataFiles) {
                String classFileName = classFile.getFileName();
                if (packageFileName.equalsIgnoreCase(classFileName)) {
                    packageFile.setFileName(packageFileName.replaceAll("\\.yml$", "(package).yml"));
                    classFile.setFileName(classFileName.replaceAll("\\.yml$", "(class).yml"));
                    break;
                }
            }
        }

        populateUidValues(packageMetadataFiles, classMetadataFiles);

        packageMetadataFiles.forEach(FileUtil::dumpToFile);
        classMetadataFiles.forEach(FileUtil::dumpToFile);
        FileUtil.dumpToFile(tocFile);

        return true;
    }

    void buildFilesForInnerClasses(Element element, List<TocItem> listToAddItems, List<MetadataFile> container) {
        for (TypeElement classElement : elementUtil.extractSortedElements(element)) {
            String uid = classLookup.extractUid(classElement);
            String name = classLookup.extractTocName(classElement);

            listToAddItems.add(new TocItem(uid, name));

            container.add(buildClassYmlFile(classElement));
            buildFilesForInnerClasses(classElement, listToAddItems, container);
        }
    }

    MetadataFile buildPackageMetadataFile(PackageElement packageElement) {
        String fileName = packageLookup.extractHref(packageElement);
        MetadataFile packageMetadataFile = new MetadataFile(outputPath, fileName);
        MetadataFileItem packageItem = new MetadataFileItem(LANGS, packageLookup.extractUid(packageElement));
        packageItem.setId(packageLookup.extractId(packageElement));
        addChildrenReferences(packageElement, packageItem.getChildren(), packageMetadataFile.getReferences());
        populateItemFields(packageItem, packageLookup, packageElement);
        packageMetadataFile.getItems().add(packageItem);
        return packageMetadataFile;
    }

    void addChildrenReferences(Element element, List<String> packageChildren,
                               Set<MetadataFileItem> referencesCollector) {
        for (TypeElement classElement : elementUtil.extractSortedElements(element)) {
            referencesCollector.add(buildClassReference(classElement));

            packageChildren.add(classLookup.extractUid(classElement));
            addChildrenReferences(classElement, packageChildren, referencesCollector);
        }
    }

    MetadataFileItem buildClassReference(TypeElement classElement) {
        MetadataFileItem referenceItem = new MetadataFileItem(classLookup.extractUid(classElement));
        referenceItem.setName(classLookup.extractName(classElement));
        referenceItem.setNameWithType(classLookup.extractNameWithType(classElement));
        referenceItem.setFullName(classLookup.extractFullName(classElement));
        return referenceItem;
    }

    <T extends Element> void populateItemFields(MetadataFileItem item, BaseLookup<T> lookup, T element) {
        item.setName(lookup.extractName(element));
        item.setNameWithType(lookup.extractNameWithType(element));
        item.setFullName(lookup.extractFullName(element));
        item.setType(lookup.extractType(element));
        item.setSummary(lookup.extractSummary(element));
        item.setContent(lookup.extractContent(element));
    }

    MetadataFile buildClassYmlFile(TypeElement classElement) {
        String fileName = classLookup.extractHref(classElement);
        MetadataFile classMetadataFile = new MetadataFile(outputPath, fileName);
        addClassInfo(classElement, classMetadataFile);
        addConstructorsInfo(classElement, classMetadataFile);
        addMethodsInfo(classElement, classMetadataFile);
        addFieldsInfo(classElement, classMetadataFile);
        addReferencesInfo(classElement, classMetadataFile);
        applyPostProcessing(classMetadataFile);
        return classMetadataFile;
    }

    void addClassInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        MetadataFileItem classItem = new MetadataFileItem(LANGS, classLookup.extractUid(classElement));
        classItem.setId(classLookup.extractId(classElement));
        classItem.setParent(classLookup.extractParent(classElement));
        addChildren(classElement, classItem.getChildren());
        populateItemFields(classItem, classLookup, classElement);
        classItem.setPackageName(classLookup.extractPackageName(classElement));
        classItem.setTypeParameters(classLookup.extractTypeParameters(classElement));
        classItem.setInheritance(classLookup.extractSuperclass(classElement));
        classItem.setInterfaces(classLookup.extractInterfaces(classElement));
        classItem.setInheritedMethods(classLookup.extractInheritedMethods(classElement));
        classMetadataFile.getItems().add(classItem);
    }

    void addChildren(TypeElement classElement, List<String> children) {
        collect(classElement, children, ElementFilter::constructorsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::methodsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::fieldsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::typesIn, String::valueOf);
    }

    List<? extends Element> filterPrivateElements(List<? extends Element> elements) {
        return elements.stream()
                .filter(element -> !Utils.isPrivateOrPackagePrivate(element)).collect(Collectors.toList());
    }

    void collect(TypeElement classElement, List<String> children,
                 Function<Iterable<? extends Element>, List<? extends Element>> selectFunc,
                 Function<? super Element, String> mapFunc) {

        List<? extends Element> elements = selectFunc.apply(classElement.getEnclosedElements());
        children.addAll(filterPrivateElements(elements).stream()
                .map(mapFunc).collect(Collectors.toList()));
    }

    void addConstructorsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            MetadataFileItem constructorItem = buildMetadataFileItem(constructorElement);
            constructorItem.setOverload(classItemsLookup.extractOverload(constructorElement));
            constructorItem.setContent(classItemsLookup.extractConstructorContent(constructorElement));
            constructorItem.setParameters(classItemsLookup.extractParameters(constructorElement));
            classMetadataFile.getItems().add(constructorItem);

            addParameterReferences(constructorItem, classMetadataFile);
            addOverloadReferences(constructorItem, classMetadataFile);
        }
    }

    void addMethodsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        ElementFilter.methodsIn(classElement.getEnclosedElements()).stream()
                .filter(methodElement -> !Utils.isPrivateOrPackagePrivate(methodElement))
                .forEach(methodElement -> {
                    MetadataFileItem methodItem = buildMetadataFileItem(methodElement);
                    methodItem.setOverload(classItemsLookup.extractOverload(methodElement));
                    methodItem.setContent(classItemsLookup.extractMethodContent(methodElement));
                    methodItem.setExceptions(classItemsLookup.extractExceptions(methodElement));
                    methodItem.setParameters(classItemsLookup.extractParameters(methodElement));
                    methodItem.setReturn(classItemsLookup.extractReturn(methodElement));
                    methodItem.setOverridden(classItemsLookup.extractOverridden(methodElement));

                    classMetadataFile.getItems().add(methodItem);
                    addExceptionReferences(methodItem, classMetadataFile);
                    addParameterReferences(methodItem, classMetadataFile);
                    addReturnReferences(methodItem, classMetadataFile);
                    addOverloadReferences(methodItem, classMetadataFile);
                });
    }

    void addFieldsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        ElementFilter.fieldsIn(classElement.getEnclosedElements()).stream()
                .filter(fieldElement -> !Utils.isPrivateOrPackagePrivate(fieldElement))
                .forEach(fieldElement -> {
                    MetadataFileItem fieldItem = buildMetadataFileItem(fieldElement);
                    fieldItem.setContent(classItemsLookup.extractFieldContent(fieldElement));
                    fieldItem.setReturn(classItemsLookup.extractReturn(fieldElement));
                    classMetadataFile.getItems().add(fieldItem);
                    addReturnReferences(fieldItem, classMetadataFile);
                });
    }

    void addReferencesInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        MetadataFileItem classReference = new MetadataFileItem(classLookup.extractUid(classElement));
        classReference.setParent(classLookup.extractParent(classElement));
        populateItemFields(classReference, classLookup, classElement);
        classReference.setTypeParameters(classLookup.extractTypeParameters(classElement));

        addTypeParameterReferences(classReference, classMetadataFile);
        addSuperclassAndInterfacesReferences(classElement, classMetadataFile);
        addInnerClassesReferences(classElement, classMetadataFile);
    }

    MetadataFileItem buildMetadataFileItem(Element element) {
        return new MetadataFileItem(LANGS, classItemsLookup.extractUid(element)) {{
            setId(classItemsLookup.extractId(element));
            setParent(classItemsLookup.extractParent(element));
            setName(classItemsLookup.extractName(element));
            setNameWithType(classItemsLookup.extractNameWithType(element));
            setFullName(classItemsLookup.extractFullName(element));
            setType(classItemsLookup.extractType(element));
            setPackageName(classItemsLookup.extractPackageName(element));
            setSummary(classItemsLookup.extractSummary(element));
        }};
    }

    void addParameterReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
                methodItem.getSyntax().getParameters().stream()
                        .map(parameter -> buildRefItem(parameter.getType()))
                        .filter(o -> !classMetadataFile.getItems().contains(o))
                        .collect(Collectors.toList()));
    }

    void addReturnReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
                Stream.of(methodItem.getSyntax().getReturnValue())
                        .filter(Objects::nonNull)
                        .map(returnValue -> buildRefItem(returnValue.getReturnType()))
                        .filter(o -> !classMetadataFile.getItems().contains(o))
                        .collect(Collectors.toList()));
    }

    void addExceptionReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
                methodItem.getExceptions().stream()
                        .map(exceptionItem -> buildRefItem(exceptionItem.getType()))
                        .filter(o -> !classMetadataFile.getItems().contains(o))
                        .collect(Collectors.toList()));
    }

    void addTypeParameterReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
                methodItem.getSyntax().getTypeParameters().stream()
                        .map(typeParameter -> {
                            String id = typeParameter.getId();
                            return new MetadataFileItem(id, id, false);
                        }).collect(Collectors.toList()));
    }

    void addSuperclassAndInterfacesReferences(TypeElement classElement, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(classLookup.extractReferences(classElement));
    }

    void addInnerClassesReferences(TypeElement classElement, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
                ElementFilter.typesIn(elementUtil.extractSortedElements(classElement)).stream()
                        .map(this::buildClassReference)
                        .collect(Collectors.toList()));
    }

    void addOverloadReferences(MetadataFileItem item, MetadataFile classMetadataFile) {
        MetadataFileItem overloadRefItem = new MetadataFileItem(item.getOverload()) {{
            setName(RegExUtils.removeAll(item.getName(), "\\(.*\\)$"));
            setNameWithType(RegExUtils.removeAll(item.getNameWithType(), "\\(.*\\)$"));
            setFullName(RegExUtils.removeAll(item.getFullName(), "\\(.*\\)$"));
            setPackageName(item.getPackageName());
        }};
        classMetadataFile.getReferences().add(overloadRefItem);
    }

    void applyPostProcessing(MetadataFile classMetadataFile) {
        expandComplexGenericsInReferences(classMetadataFile);
    }

    /**
     * Replace one record in 'references' with several records in this way:
     * <pre>
     * a.b.c.List<df.mn.ClassOne<tr.T>> ->
     *     - a.b.c.List
     *     - df.mn.ClassOne
     *     - tr.T
     * </pre>
     */
    void expandComplexGenericsInReferences(MetadataFile classMetadataFile) {
        Set<MetadataFileItem> additionalItems = new LinkedHashSet<>();
        Iterator<MetadataFileItem> iterator = classMetadataFile.getReferences().iterator();
        while (iterator.hasNext()) {
            MetadataFileItem item = iterator.next();
            String uid = item.getUid();
            if (!uid.endsWith("*") && uid.contains("<")) {
                List<String> classNames = splitUidWithGenericsIntoClassNames(uid);
                additionalItems.addAll(classNames.stream()
                        .map(s -> new MetadataFileItem(s, classLookup.makeTypeShort(s), false))
                        .collect(Collectors.toSet()));
            }
        }
        // Remove items which already exist in 'items' section (compared by 'uid' field)
        additionalItems.removeAll(classMetadataFile.getItems());

        classMetadataFile.getReferences().addAll(additionalItems);
    }

    void populateUidValues(List<MetadataFile> packageMetadataFiles, List<MetadataFile> classMetadataFiles) {
        Lookup lookup = new Lookup(packageMetadataFiles, classMetadataFiles);

        classMetadataFiles.forEach(classMetadataFile -> {
            LookupContext lookupContext = lookup.buildContext(classMetadataFile);

            for (MetadataFileItem item : classMetadataFile.getItems()) {
                item.setSummary(YamlUtil.convertHtmlToMarkdown(
                        populateUidValues(item.getSummary(), lookupContext)
                ));

                Optional.ofNullable(item.getSyntax()).ifPresent(syntax -> {
                            Optional.ofNullable(syntax.getParameters()).ifPresent(
                                    methodParams -> methodParams.forEach(
                                            param -> {
                                                param.setDescription(populateUidValues(param.getDescription(), lookupContext));
                                            })
                            );
                            Optional.ofNullable(syntax.getReturnValue()).ifPresent(returnValue ->
                                    returnValue.setReturnDescription(
                                            populateUidValues(syntax.getReturnValue().getReturnDescription(), lookupContext)
                                    )
                            );
                        }
                );
            }
        });
    }

    String populateUidValues(String text, LookupContext lookupContext) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        Matcher linkMatcher = XREF_LINK_PATTERN.matcher(text);
        while (linkMatcher.find()) {
            String link = linkMatcher.group();
            Matcher linkContentMatcher = XREF_LINK_CONTENT_PATTERN.matcher(link);
            if (!linkContentMatcher.find()) {
                continue;
            }

            String linkContent = linkContentMatcher.group();
            String uid = resolveUidFromLinkContent(linkContent, lookupContext);
            String updatedLink = linkContentMatcher.replaceAll(uid);
            text = StringUtils.replace(text, link, updatedLink);
        }
        return text;
    }

    /**
     * The linkContent could be in following format
     * #memeber
     * Class#member
     * Class#method()
     * Class#method(params)
     */
    String resolveUidFromLinkContent(String linkContent, LookupContext lookupContext) {
        if (StringUtils.isBlank(linkContent)) {
            return "";
        }

        linkContent = linkContent.trim();

        // complete class name for class internal link
        if (linkContent.startsWith("#")) {
            String firstKey = lookupContext.getOwnerUid();
            linkContent = firstKey + linkContent;
        }

        // fuzzy resolve, target for items from project external references
        String fuzzyResolvedUid = resolveUidFromReference(linkContent, lookupContext);

        // exact resolve in lookupContext
        linkContent = linkContent.replace("#", ".");
        String exactResolveUid = resolveUidByLookup(linkContent, lookupContext);
        return exactResolveUid.isEmpty() ? fuzzyResolvedUid : exactResolveUid;
    }

    List<String> splitUidWithGenericsIntoClassNames(String uid) {
        uid = RegExUtils.removeAll(uid, "[>]+$");
        return Arrays.asList(StringUtils.split(uid, "<"));
    }

    MetadataFileItem buildRefItem(String uid) {
        if (!uid.endsWith("*") && (uid.contains("<") || uid.contains("[]"))) {
            return new MetadataFileItem(uid, getJavaSpec(replaceUidAndSplit(uid)));
        } else {
            List<String> fullNameList = new ArrayList<>();

            this.environment.getIncludedElements().forEach(
                    element -> elementUtil.extractSortedElements(element).forEach(
                            typeElement -> fullNameList.add(classLookup.extractFullName(typeElement)))
            );

            if (fullNameList.contains(uid)) {
                return new MetadataFileItem(uid, classLookup.makeTypeShort(uid), false);
            } else {
                return new MetadataFileItem(uid, getJavaSpec(replaceUidAndSplit(uid)));
            }
        }
    }

    List<String> replaceUidAndSplit(String uid) {
        String retValue = RegExUtils.replaceAll(uid, "\\<", "//<//");
        retValue = RegExUtils.replaceAll(retValue, "\\>", "//>//");
        retValue = RegExUtils.replaceAll(retValue, ",", "//,//");
        retValue = RegExUtils.replaceAll(retValue, "\\[\\]", "//[]//");

        return Arrays.asList(StringUtils.split(retValue, "//"));
    }

    List<SpecViewModel> getJavaSpec(List<String> references) {
        List<SpecViewModel> specList = new ArrayList<>();

        Optional.ofNullable(references).ifPresent(
                ref -> references.forEach(
                        uid -> {
                            if (uid.equalsIgnoreCase("<")
                                    || uid.equalsIgnoreCase(">")
                                    || uid.equalsIgnoreCase(",")
                                    || uid.equalsIgnoreCase("[]"))
                                specList.add(new SpecViewModel(null, uid));
                            else if (uid != "")
                                specList.add(new SpecViewModel(uid, uid));
                        })
        );

        return specList;
    }

    /**
     * this method is used to do fuzzy resolve
     * "*" will be added at the end of uid for method for xerf service resolve purpose
     */
    String resolveUidFromReference(String linkContent, LookupContext lookupContext) {
        String uid = "";
        Matcher matcher = XREF_LINK_RESOLVE_PATTERN.matcher(linkContent);

        if (matcher.find()) {
            String className = matcher.group("class");
            String memberName = matcher.group("member");
            uid = resolveUidByLookup(className, lookupContext);
            if (!uid.isEmpty()) {
                uid = uid.concat(".").concat(memberName);

                // linkContent targets a method
                if (!StringUtils.isBlank(matcher.group(3))) {
                    uid = uid.concat("*");
                }
            }
        }
        return uid;
    }

    String resolveUidByLookup(String signature, LookupContext lookupContext) {
        if (StringUtils.isBlank(signature) || lookupContext == null) {
            return "";
        }
        return lookupContext.containsKey(signature) ? lookupContext.resolve(signature) : "";
    }
}
