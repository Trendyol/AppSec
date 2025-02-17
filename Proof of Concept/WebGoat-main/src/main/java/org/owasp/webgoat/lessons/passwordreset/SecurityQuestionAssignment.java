/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset;

import static java.util.Optional.of;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.informationMessage;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import java.util.HashMap;
import java.util.Map;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Assignment for picking a good security question.
 *
 * @author Tobias Melzer
 * @since 11.12.18
 */
@RestController
public class SecurityQuestionAssignment implements AssignmentEndpoint {

  private final TriedQuestions triedQuestions;

  private static Map<String, String> questions;

  static {
    questions = new HashMap<>();
    questions.put(
        "What is your favorite animal?",
        "The answer can easily be guessed and figured out through social media.");
    questions.put("In what year was your mother born?", "Can  be easily guessed.");
    questions.put(
        "What was the time you were born?",
        "This may first seem like a good question, but you most likely dont know the exact time, so"
            + " it might be hard to remember.");
    questions.put(
        "What is the name of the person you first kissed?",
        "Can be figured out through social media, or even guessed by trying the most common"
            + " names.");
    questions.put(
        "What was the house number and street name you lived in as a child?",
        "Answer can be figured out through social media, or worse it might be your current"
            + " address.");
    questions.put(
        "In what town or city was your first full time job?",
        "In times of LinkedIn and Facebook, the answer can be figured out quite easily.");
    questions.put("In what city were you born?", "Easy to figure out through social media.");
    questions.put(
        "What was the last name of your favorite teacher in grade three?",
        "Most people would probably not know the answer to that.");
    questions.put(
        "What is the name of a college/job you applied to but didn't attend?",
        "It might not be easy to remember and an hacker could just try some company's/colleges in"
            + " your area.");
    questions.put(
        "What are the last 5 digits of your drivers license?",
        "Is subject to change, and the last digit of your driver license might follow a specific"
            + " pattern. (For example your birthday).");
    questions.put("What was your childhood nickname?", "Not all people had a nickname.");
    questions.put(
        "Who was your childhood hero?",
        "Most Heroes we had as a child where quite obvious ones, like Superman for example.");
    questions.put(
        "On which wrist do you wear your watch?",
        "There are only to possible real answers, so really easy to guess.");
    questions.put("What is your favorite color?", "Can easily be guessed.");
  }

  public SecurityQuestionAssignment(TriedQuestions triedQuestions) {
    this.triedQuestions = triedQuestions;
  }

  @PostMapping("/PasswordReset/SecurityQuestions")
  @ResponseBody
  public AttackResult completed(@RequestParam String question) {
    var answer = of(questions.get(question));
    if (answer.isPresent()) {
      triedQuestions.incr(question);
      if (triedQuestions.isComplete()) {
        return success(this).output("<b>" + answer + "</b>").build();
      }
    }
    return informationMessage(this)
        .feedback("password-questions-one-successful")
        .output(answer.orElse("Unknown question, please try again..."))
        .build();
  }
}
