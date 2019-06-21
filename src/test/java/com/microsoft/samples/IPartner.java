// -----------------------------------------------------------------------
// <copyright file="IPartner.java" company="Microsoft">
//      Copyright (c) Microsoft Corporation. All rights reserved.
// </copyright>
// -----------------------------------------------------------------------

package com.microsoft.samples;


/**
 * The main entry point into using the partner SDK functionality. Represents a partner and encapsulates all the behavior
 * attached to partners. Use this interface to get to the partner's customers, profiles, and customer orders, profiles
 * and subscriptions and more.
 */
public interface IPartner
{
    /**
     * Gets the partner credentials.
     * 
     * @return The partner credentials.
     */
   String getCredentials();

    /**
     * Gets the request context.
     *
     * @return The request context.
     */
  
}