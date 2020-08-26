package com.microsoft.model.sdp;

import com.microsoft.model.MetadataFileItem;

public class FieldModel extends BaseModel implements Comparable<FieldModel> {

    //<editor-fold desc="Properties">
    private String syntax;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public FieldModel(MetadataFileItem item) {
        super(item.getUid(), item.getName(), item.getFullName(), item.getNameWithType());
        if (!(item.getSummary() == null || item.getSummary().isEmpty())) {
            this.setSummary(item.getSummary());
        }
        if (!(item.getSyntax() == null || item.getSyntax().getContent().isEmpty())) {
            this.syntax = item.getSyntax().getContent();
        }

    }
    //</editor-fold>

    @Override
    public int compareTo(FieldModel item) {
        return this.getUid().compareTo(item.getUid());
    }
}
