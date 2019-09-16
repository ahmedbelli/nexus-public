/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.common.entity;

import javax.annotation.Nonnull;

/**
 * Entity identifier.
 *
 * @since 3.0
 */
public interface EntityId
{
  /**
   * External identifier for entity.
   */
  @Nonnull
  String getValue();

  /**
   * Returns human-readable representation for logging.
   *
   * @see #getValue() for the external representation.
   */
  @Override
  String toString();

  /**
   * Shorthand for creating a detached {@link EntityId}.
   */
  @SuppressWarnings("deprecation")
  static EntityId of(String value) {
    return new DetachedEntityId(value);
  }
}