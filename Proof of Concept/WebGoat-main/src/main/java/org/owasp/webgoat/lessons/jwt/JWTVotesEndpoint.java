/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt;

import static java.util.Comparator.comparingLong;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.TextCodec;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.lessons.jwt.votes.Views;
import org.owasp.webgoat.lessons.jwt.votes.Vote;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AssignmentHints({
  "jwt-change-token-hint1",
  "jwt-change-token-hint2",
  "jwt-change-token-hint3",
  "jwt-change-token-hint4",
  "jwt-change-token-hint5"
})
public class JWTVotesEndpoint implements AssignmentEndpoint {

  public static final String JWT_PASSWORD = TextCodec.BASE64.encode("victory");
  private static String validUsers = "TomJerrySylvester";

  private static int totalVotes = 38929;
  private final Map<String, Vote> votes = new HashMap<>();

  @PostConstruct
  public void initVotes() {
    votes.put(
        "Admin lost password",
        new Vote(
            "Admin lost password",
            "In this challenge you will need to help the admin and find the password in order to"
                + " login",
            "challenge1-small.png",
            "challenge1.png",
            36000,
            totalVotes));
    votes.put(
        "Vote for your favourite",
        new Vote(
            "Vote for your favourite",
            "In this challenge ...",
            "challenge5-small.png",
            "challenge5.png",
            30000,
            totalVotes));
    votes.put(
        "Get it for free",
        new Vote(
            "Get it for free",
            "The objective for this challenge is to buy a Samsung phone for free.",
            "challenge2-small.png",
            "challenge2.png",
            20000,
            totalVotes));
    votes.put(
        "Photo comments",
        new Vote(
            "Photo comments",
            "n this challenge you can comment on the photo you will need to find the flag"
                + " somewhere.",
            "challenge3-small.png",
            "challenge3.png",
            10000,
            totalVotes));
  }

  @GetMapping("/JWT/votings/login")
  public void login(@RequestParam("user") String user, HttpServletResponse response) {
    if (validUsers.contains(user)) {
      Claims claims = Jwts.claims().setIssuedAt(Date.from(Instant.now().plus(Duration.ofDays(10))));
      claims.put("admin", "false");
      claims.put("user", user);
      String token =
          Jwts.builder()
              .setClaims(claims)
              .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, JWT_PASSWORD)
              .compact();
      Cookie cookie = new Cookie("access_token", token);
      response.addCookie(cookie);
      response.setStatus(HttpStatus.OK.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    } else {
      Cookie cookie = new Cookie("access_token", "");
      response.addCookie(cookie);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
  }

  @GetMapping("/JWT/votings")
  @ResponseBody
  public MappingJacksonValue getVotes(
      @CookieValue(value = "access_token", required = false) String accessToken) {
    MappingJacksonValue value =
        new MappingJacksonValue(
            votes.values().stream()
                .sorted(comparingLong(Vote::getAverage).reversed())
                .collect(toList()));
    if (StringUtils.isEmpty(accessToken)) {
      value.setSerializationView(Views.GuestView.class);
    } else {
      try {
        Jwt jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken);
        Claims claims = (Claims) jwt.getBody();
        String user = (String) claims.get("user");
        if ("Guest".equals(user) || !validUsers.contains(user)) {
          value.setSerializationView(Views.GuestView.class);
        } else {
          value.setSerializationView(Views.UserView.class);
        }
      } catch (JwtException e) {
        value.setSerializationView(Views.GuestView.class);
      }
    }
    return value;
  }

  @PostMapping(value = "/JWT/votings/{title}")
  @ResponseBody
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity<?> vote(
      @PathVariable String title,
      @CookieValue(value = "access_token", required = false) String accessToken) {
    if (StringUtils.isEmpty(accessToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } else {
      try {
        Jwt jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken);
        Claims claims = (Claims) jwt.getBody();
        String user = (String) claims.get("user");
        if (!validUsers.contains(user)) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
          ofNullable(votes.get(title)).ifPresent(v -> v.incrementNumberOfVotes(totalVotes));
          return ResponseEntity.accepted().build();
        }
      } catch (JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
    }
  }

  @PostMapping("/JWT/votings")
  @ResponseBody
  public AttackResult resetVotes(
      @CookieValue(value = "access_token", required = false) String accessToken) {
    if (StringUtils.isEmpty(accessToken)) {
      return failed(this).feedback("jwt-invalid-token").build();
    } else {
      try {
        Jwt jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken);
        Claims claims = (Claims) jwt.getBody();
        boolean isAdmin = Boolean.valueOf(String.valueOf(claims.get("admin")));
        if (!isAdmin) {
          return failed(this).feedback("jwt-only-admin").build();
        } else {
          votes.values().forEach(vote -> vote.reset());
          return success(this).build();
        }
      } catch (JwtException e) {
        return failed(this).feedback("jwt-invalid-token").output(e.toString()).build();
      }
    }
  }
}
