/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.bypassrestrictions;

import org.owasp.webgoat.container.lessons.Category;
import org.owasp.webgoat.container.lessons.Lesson;
import org.springframework.stereotype.Component;

@Component
public class BypassRestrictions extends Lesson {
  @Override
  public Category getDefaultCategory() {
    return Category.CLIENT_SIDE;
  }

  @Override
  public String getTitle() {
    return "bypass-restrictions.title";
  }
}
