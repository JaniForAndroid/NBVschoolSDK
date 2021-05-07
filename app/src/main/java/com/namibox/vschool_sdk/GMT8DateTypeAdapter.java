package com.namibox.vschool_sdk;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.bind.util.ISO8601Utils;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * author : feng
 * description ：
 * creation time : 20-7-23下午4:51
 */
public class GMT8DateTypeAdapter implements JsonDeserializer<Date> {

  private final SimpleDateFormat dateFormat, dateFormat2;

  public GMT8DateTypeAdapter(String datePattern) {
    dateFormat = new SimpleDateFormat(datePattern, Locale.CHINA);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

    dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
  }

  @Override
  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (!(json instanceof JsonPrimitive)) {
      throw new JsonParseException("The date should be a string value");
    }
    Date date = deserializeToDate(json);
    if (typeOfT == Date.class) {
      return date;
    } else if (typeOfT == Timestamp.class) {
      return new Timestamp(date.getTime());
    } else if (typeOfT == java.sql.Date.class) {
      return new java.sql.Date(date.getTime());
    } else {
      throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
    }
  }

  private Date deserializeToDate(JsonElement json) {
    synchronized (dateFormat) {
      try {
        return dateFormat.parse(json.getAsString());
      } catch (ParseException ignored) {
      }
      try {
        return dateFormat.parse(json.getAsString());
      } catch (ParseException ignored) {
        try {
          return dateFormat2.parse(json.getAsString());
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }
      try {
        return ISO8601Utils.parse(json.getAsString(), new ParsePosition(0));
      } catch (ParseException e) {
        throw new JsonSyntaxException(json.getAsString(), e);
      }
    }
  }
}
