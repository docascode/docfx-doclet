package com.microsoft.samples.agreements;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The AgreementMetaData provides metadata about the agreement type
 * that partner can provide confirmation of customer acceptance.
 */
public class AgreementMetaData
{
    /**
     * Gets or sets the unique identifier of an agreement template.
     */
    @JsonProperty( "templateId" )
    private String templateId;

    public String getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId( String value )
    {
        templateId = value;
    }

    /**
     * Gets or sets agreement type.
     */


    /**
     * Gets or sets URL to the agreement template.
     */
    @JsonProperty( "agreementLink" )
    private String agreementLink;

    public String getAgreementLink()
    {
        return agreementLink;
    }

    public void setAgreementLink( String value )
    {
        agreementLink = value;
    }

    /**
     * Gets or sets the version rank of an agreement template.
     */
    @JsonProperty( "versionRank" )
    private int versionRank;

    public int getVersionRank()
    {
        return versionRank;
    }

    public void setVersionRank( int value )
    {
        versionRank = value;
    }
}