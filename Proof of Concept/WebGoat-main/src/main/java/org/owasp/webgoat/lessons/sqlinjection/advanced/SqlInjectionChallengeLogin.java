/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AssignmentHints(
    value = {
      "SqlInjectionChallengeHint1",
      "SqlInjectionChallengeHint2",
      "SqlInjectionChallengeHint3",
      "SqlInjectionChallengeHint4"
    })
public class SqlInjectionChallengeLogin implements AssignmentEndpoint {
  private final LessonDataSource dataSource;

  public SqlInjectionChallengeLogin(LessonDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @PostMapping("/SqlInjectionAdvanced/challenge_Login")
  @ResponseBody
  public AttackResult login(
      @RequestParam String username_login, @RequestParam String password_login) throws Exception {
    try (var connection = dataSource.getConnection()) {
      var statement =
          connection.prepareStatement(
              "select password from sql_challenge_users where userid = ? and password = ?");
      statement.setString(1, username_login);
      statement.setString(2, password_login);
      var resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return ("tom".equals(username_login))
            ? success(this).build()
            : failed(this).feedback("ResultsButNotTom").build();
      } else {
        return failed(this).feedback("NoResultsMatched").build();
      }
    }
  }
}
