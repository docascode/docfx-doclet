package com.microsoft.samples.noneascii;

/**
 * 代表客户可用的产品形式
 */
public class Offer {
    /**
     * 初始化Offer类的新实例。
     */
    public Offer() {
    }

    /**
     * 获取或设置合作伙伴要求的资格，以便为客户购买优惠。
     */
    private String[] __ResellerQualifications;

    public String[] getResellerQualifications() {
        return __ResellerQualifications;
    }

    public void setResellerQualifications(String[] value) {
        __ResellerQualifications = value;
    }

    /**
     * 获取或设置客户要求合作伙伴为客户购买的资格。
     */
    private String[] __ReselleeQualifications;

    public String[] getReselleeQualifications() {
        return __ReselleeQualifications;
    }

    public void setReselleeQualifications(String[] value) {
        __ReselleeQualifications = value;
    }
}