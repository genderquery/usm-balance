package com.github.genderquery.usmbalance.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Phone line data scraped from the US Mobile site.
 */
public class UsmLine implements Parcelable {

  public static final Creator<UsmLine> CREATOR = new Creator<UsmLine>() {
    @Override
    public UsmLine createFromParcel(Parcel in) {
      return new UsmLine(in);
    }

    @Override
    public UsmLine[] newArray(int size) {
      return new UsmLine[size];
    }
  };

  public String id;
  public String name;
  public String phoneNumber;
  public Bitmap avatar;

  public UsmLine() {
  }

  protected UsmLine(Parcel in) {
    id = in.readString();
    name = in.readString();
    phoneNumber = in.readString();
    avatar = Bitmap.CREATOR.createFromParcel(in);
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(name);
    dest.writeString(phoneNumber);
    dest.writeParcelable(avatar, 0);
  }

  @Override
  public int describeContents() {
    return 0;
  }
}
