/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.user.internal.document;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceProvider;
import org.xwiki.user.internal.GuestUser;
import org.xwiki.user.internal.SuperAdminUser;
import org.xwiki.user.User;
import org.xwiki.user.UserResolver;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiRightService;

/**
 * Helps implement Document-based User Resolvers.
 *
 * @param <T> the type of the user representation
 * @version $Id$
 * @since 12.2RC1
 */
public abstract class AbstractDocumentUserResolver<T> implements UserResolver<T>
{
    @Inject
    @Named("user")
    protected ConfigurationSource userConfigurationSource;

    @Inject
    @Named("superadminuser")
    protected ConfigurationSource superAdminConfigurationSource;

    @Inject
    @Named("guestuser")
    protected ConfigurationSource guestConfigurationSource;

    @Inject
    protected EntityReferenceProvider entityReferenceProvider;

    @Inject
    protected Provider<XWikiContext> contextProvider;

    /**
     * @param userDocumentReference the reference to the user profile page. If null then consider it's pointing to the
     *                              Guest user.
     * @return the User object
     */
    protected User resolveUser(DocumentReference userDocumentReference)
    {
        return resolveUser(new DocumentUserReference(userDocumentReference, this.entityReferenceProvider));
    }

    /**
     * @param documentUserReference the reference to the user. If null then consider it's pointing to the Guest user.
     * @return the User object
     */
    protected User resolveUser(DocumentUserReference documentUserReference)
    {
        User user;
        // Backward compatibility: recognize guest and superadmin users since they're currently stored as normal users
        // in the XWikiContext
        DocumentReference documentReference = documentUserReference.getReference();
        if (documentReference == null || XWikiRightService.isGuest(documentReference)) {
            user = new GuestUser(this.guestConfigurationSource);
        } else if (XWikiRightService.isSuperAdmin(documentReference)) {
            user = new SuperAdminUser(this.superAdminConfigurationSource);
        } else {
            user = new DocumentUser(documentUserReference, this.contextProvider, this.userConfigurationSource);
        }
        return user;
    }
}