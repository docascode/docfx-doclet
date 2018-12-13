package com.microsoft.model;

public class TypeParameter {

    public static class Builder {

        private TypeParameter value;

        public Builder() {
            this.value = new TypeParameter();
        }

        public Builder addId(String id) {
            value.setId(id);
            return this;
        }

        public Builder addType(String type) {
            value.setType(type);
            return this;
        }

        public Builder addDescription(String description) {
            value.setDescription(description);
            return this;
        }

        public TypeParameter build() {
            if (value.type != null && (value.id != null || value.description != null)) {
                return value;
            }
            throw new IllegalStateException("Not enough data to create TypeParameter object!");
        }
    }

    private TypeParameter() {
    }

    private String id;
    private String type;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
