package com.github.genderquery.usmbalance.sync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import com.github.genderquery.usmbalance.data.UsmBalance;
import com.github.genderquery.usmbalance.data.UsmLine;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Methods to scrape and parse US Mobile pages.
 *
 * Call {@link #login(String, String)} before calling other methods.
 */
public class UsmApi {

  private static final String TAG = "UsmApi";

  private static final String LOGIN_URL = "https://www.usmobile.com/auth/signin";
  private static final String ACCOUNT_URL = "https://www.usmobile.com/my/account";
  private static final String USAGE_URL = "https://www.usmobile.com/my/line";

  /**
   * Date format used for the service end date
   */
  private static final SimpleDateFormat dateFormat =
      new SimpleDateFormat("MMM d, yyyy", Locale.US);

  /**
   * Cookie store to persist session key across calls.
   */
  private static Map<String, String> cookies;

  /**
   * Login and store session cookie.
   *
   * @param username may be an email address or phone number
   * @param password password
   * @throws HttpUnauthorizedException thrown if the username and/or password was incorrect
   * @throws HttpStatusException thrown if an unexpected status was returned (like a server error)
   * @throws IOException thrown if there is a connection or parse error
   */
  public static void login(String username, String password) throws IOException {
    Connection connection = Jsoup.connect(LOGIN_URL)
        .method(Connection.Method.POST)
        .followRedirects(false)
        .data("username", username)
        .data("password", password);
    Connection.Response response;
    try {
      response = connection.execute();
    } catch (HttpStatusException e) {
      int statusCode = e.getStatusCode();
      if (statusCode == 401) {
        throw new HttpUnauthorizedException(statusCode, e.getUrl());
      } else {
        throw e;
      }
    }
    cookies = response.cookies();
  }

  /**
   * Get a list of phone lines for the currently logged in account.
   *
   * Call {@link #login(String, String)} first.
   *
   * @return a list of lines for the current account
   * @throws HttpUnauthorizedException thrown if the session is no longer valid
   * @throws HttpStatusException thrown if an unexpected status was returned (like a server error)
   * @throws IOException thrown if there is a connection or parse error
   */
  public static ArrayList<UsmLine> getLines() throws IOException {
    Connection connection = Jsoup.connect(ACCOUNT_URL)
        .method(Connection.Method.GET)
        .followRedirects(false)
        .cookies(cookies);
    Connection.Response response = connection.execute();
    int statusCode = response.statusCode();
    if (statusCode == 302) {
      // 302 is sent if not signed in
      String url = connection.request().url().toString();
      throw new HttpUnauthorizedException(statusCode, url);
    }
    Document document = response.parse();
    return parseLines(document);
  }

  /**
   * Gets the balance data for the given line.
   *
   * Call {@link #login(String, String)} first.
   *
   * @param lineId the line ID as given by {@link #getLines()}
   * @return a list of lines for the current account
   * @throws HttpUnauthorizedException thrown if the session is no longer valid
   * @throws HttpStatusException thrown if an unexpected status was returned (like a server error)
   * @throws IOException thrown if there is a connection or parse error
   */
  public static UsmBalance getBalance(String lineId) throws IOException {
    Connection connection = Jsoup.connect(USAGE_URL)
        .method(Connection.Method.GET)
        .followRedirects(false)
        .cookies(cookies)
        .data("id", lineId);
    Connection.Response response = connection.execute();
    int statusCode = response.statusCode();
    if (statusCode == 302) {
      // 302 is sent if not signed in or line id is invalid
      String url = connection.request().url().toString();
      throw new HttpUnauthorizedException(statusCode, url);
    }
    Document document = response.parse();
    return parseBalance(document);
  }

  private static UsmBalance parseBalance(Document document) {
    UsmBalance balance = new UsmBalance();

    String expirationDate = document.select(".expirationDate").text();
    try {
      balance.serviceEndDate = dateFormat.parse(expirationDate).getTime();
    } catch (ParseException e) {
      Log.e(TAG, e.getLocalizedMessage(), e);
    }

    Elements usages = document.select(".account-usage");
    for (Element element : usages) {

      Element remainingElement = element.selectFirst("h3");
      if (remainingElement != null) {
        int remaining = 0;
        try {
          remaining = Integer.parseInt(remainingElement.text());
        } catch (NumberFormatException e) {
          Log.e(TAG, e.getLocalizedMessage(), e);
        }
        String type = remainingElement.className();
        switch (type) {
          case "talk":
            balance.talkRemaining = remaining;
            break;
          case "text":
            balance.textRemaining = remaining;
            break;
          case "data":
            balance.dataRemaining = remaining;
            break;
        }
      }

      Element percentElement = element.selectFirst("canvas");
      if (percentElement != null) {
        int percent = 0;
        try {
          percent = Integer.parseInt(percentElement.attr("data-used"));
        } catch (NumberFormatException e) {
          Log.e(TAG, e.getLocalizedMessage(), e);
        }
        String type = percentElement.attr("data-type");
        switch (type) {
          case "talk":
            balance.talkPercent = percent;
            break;
          case "text":
            balance.textPercent = percent;
            break;
          case "data":
            balance.dataPercent = percent;
            break;
        }
      }
    }

    return balance;
  }

  private static ArrayList<UsmLine> parseLines(Document document) {
    ArrayList<UsmLine> lines = new ArrayList<>();

    Elements accounts = document.select(".card-user.active");
    for (Element account : accounts) {
      UsmLine line = new UsmLine();

      line.id = account.attr("data-sim");

      Element name = account.selectFirst("h5");
      if (name != null) {
        line.name = name.text();
      }

      Element phoneNumber = account.selectFirst("h6");
      if (phoneNumber != null) {
        line.phoneNumber = phoneNumber.text();
      }

      Element avatar = account.selectFirst("img.avatar");
      if (avatar != null) {
        String src = avatar.absUrl("src");
        try {
          line.avatar = getImage(src);
        } catch (IOException e) {
          Log.e(TAG, e.getLocalizedMessage(), e);
        }
      }

      lines.add(line);
    }

    return lines;
  }

  private static Bitmap getImage(@NonNull String src) throws IOException {
    Connection connection = Jsoup.connect(src)
        .method(Method.GET)
        .followRedirects(false)
        .cookies(UsmApi.cookies)
        .ignoreContentType(true);
    Connection.Response response = connection.execute();
    int statusCode = response.statusCode();
    if (statusCode == 302) {
      // 302 is sent if not signed in or line id is invalid
      String url = connection.request().url().toString();
      throw new HttpUnauthorizedException(statusCode, url);
    }
    // TODO compress/resize image?
    return BitmapFactory.decodeStream(response.bodyStream());
  }

  public static class HttpUnauthorizedException extends HttpStatusException {

    private int originalStatusCode;

    public HttpUnauthorizedException(int statusCode, String url) {
      super("Unauthorized", 401, url);
      originalStatusCode = statusCode;
    }

    public int getOriginalStatusCode() {
      return originalStatusCode;
    }
  }

}
