package com.microsoft.util;

import com.sun.source.doctree.DocTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;

public class CommentHelper {
    public Element element;
    public List<? extends DocTree> inlineTags = Collections.emptyList();
    private Utils utils;
    private boolean hasInheritDocTag = false;

    public CommentHelper(Element element, Utils utils) {
        this.element = element;
        this.utils = utils;
        this.inlineTags = utils.getFullBody(element);
        this.hasInheritDocTag = utils.hasInlineTag(inlineTags, DocTree.Kind.INHERIT_DOC);
    }

    public CommentHelper(Element element, Utils utils, List<? extends DocTree> inlineTags) {
        this.element = element;
        this.utils = utils;
        this.inlineTags = inlineTags;
        this.hasInheritDocTag = utils.hasInlineTag(inlineTags, DocTree.Kind.INHERIT_DOC);
    }

    /**
     * Returns true if the method has no comments, or a lone &commat;inheritDoc.
     *
     * @return true if there are no comments, false otherwise
     */
    public boolean isSimpleOverride() {
        return inlineTags.isEmpty() ||
                (inlineTags.size() == 1 && hasInheritDocTag);
    }

    public boolean hasInheritDocTag(){
        return this.hasInheritDocTag;
    }

    public CommentHelper copy() {
        if (this.element == null) {
            throw new NullPointerException();
        }
        CommentHelper clone = new CommentHelper(this.element, this.utils);
        return clone;
    }

    public CommentHelper inherit(CommentHelper chInheritFrom) {
        List<? extends DocTree> mergedTags = new ArrayList<>();

        if (this.isSimpleOverride())
            mergedTags = chInheritFrom.inlineTags;
        else {
            mergedTags = inheritInlineTags(this, chInheritFrom);
        }

        return new CommentHelper(this.element, this.utils, mergedTags);
    }

    List<? extends DocTree> inheritInlineTags(CommentHelper origin, CommentHelper chInheritFrom) {
        List<DocTree> mergedTags = new ArrayList<>();
        if (!origin.isSimpleOverride() && !origin.hasInheritDocTag) {
            return origin.inlineTags;
        }

        // Get the index of "{@inheritedDoc}".
        int index = origin.inlineTags.stream().map(e -> e.getKind())
                .collect(Collectors.toList())
                .indexOf(DocTree.Kind.INHERIT_DOC);

        // Replace the "{@inheritedDoc}" with inherited inlineTags.
        mergedTags = origin.inlineTags.stream().collect(Collectors.toList());
        mergedTags.remove(index);

        for (DocTree d : chInheritFrom.inlineTags
        ) {
            mergedTags.add(index, d);
            index++;
        }

        return mergedTags;
    }
}
