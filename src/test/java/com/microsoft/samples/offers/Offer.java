package com.microsoft.samples.offers;

import java.net.URI;
import java.util.List;

    /**
     * Represents a form of product availability to customer
     */
    public class Offer
        {
        /**
         * Initializes a new instance of the Offer class.
         */
        public Offer()
        {
        }

        /**
         * Gets or sets qualifications required by the Partner in order to purchase the offer for a customer.
         */
        private String[] __ResellerQualifications;

        public String[] getResellerQualifications()
        {
            return __ResellerQualifications;
        }

        public void setResellerQualifications(String[] value)
        {
            __ResellerQualifications = value;
        }

        /**
         * Gets or sets qualifications required by the customer for the partner to purchase it for the customer.
         */
        private String[] __ReselleeQualifications;

        public String[] getReselleeQualifications()
        {
            return __ReselleeQualifications;
        }

        public void setReselleeQualifications(String[] value)
        {
            __ReselleeQualifications = value;
        }
    }