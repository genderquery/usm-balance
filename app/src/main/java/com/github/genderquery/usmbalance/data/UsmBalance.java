package com.github.genderquery.usmbalance.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Balance data scraped from the US Mobile site.
 */
public class UsmBalance implements Parcelable {

  public static final Creator<UsmBalance> CREATOR = new Creator<UsmBalance>() {
    @Override
    public UsmBalance createFromParcel(Parcel in) {
      return new UsmBalance(in);
    }

    @Override
    public UsmBalance[] newArray(int size) {
      return new UsmBalance[size];
    }
  };

  /**
   * Date the current service ends
   */
  public long serviceEndDate;

  /**
   * Minutes remaining for talk
   */
  public int talkRemaining;

  /**
   * Percent of purchased talk remaining
   */
  public int talkPercent;

  /**
   * Texts remaining
   */
  public int textRemaining;

  /**
   * Percent of purchased texts remaining
   */
  public int textPercent;

  /**
   * Mb remaining of data
   */
  public int dataRemaining;

  /**
   * Percent of purchased data remaining
   */
  public int dataPercent;

  protected UsmBalance(Parcel in) {
    serviceEndDate = in.readLong();
    talkRemaining = in.readInt();
    talkPercent = in.readInt();
    textRemaining = in.readInt();
    textPercent = in.readInt();
    dataRemaining = in.readInt();
    dataPercent = in.readInt();
  }

  public UsmBalance() {
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(serviceEndDate);
    dest.writeInt(talkRemaining);
    dest.writeInt(talkPercent);
    dest.writeInt(textRemaining);
    dest.writeInt(textPercent);
    dest.writeInt(dataRemaining);
    dest.writeInt(dataPercent);
  }

  @Override
  public int describeContents() {
    return 0;
  }
}
