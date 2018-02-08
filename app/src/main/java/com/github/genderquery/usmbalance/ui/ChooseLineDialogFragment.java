package com.github.genderquery.usmbalance.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.github.genderquery.usmbalance.R;
import com.github.genderquery.usmbalance.data.UsmLine;
import java.util.ArrayList;

/**
 * A dialog that shows a list of lines to choose from after logging in.
 */
public class ChooseLineDialogFragment extends DialogFragment implements OnClickListener {

  private static final String EXTRA_LINES = "lines";

  private LineAdaptor adaptor;
  private ChooseLineDialogListener chooseLineDialogListener;

  /**
   * Create a dialog that shows the given lines.
   *
   * @param lines a list of lines to show
   */
  public static ChooseLineDialogFragment createInstance(ArrayList<UsmLine> lines) {
    Bundle arguments = new Bundle();
    arguments.putParcelableArrayList(EXTRA_LINES, lines);
    ChooseLineDialogFragment chooseLineDialogFragment = new ChooseLineDialogFragment();
    chooseLineDialogFragment.setArguments(arguments);
    return chooseLineDialogFragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    ArrayList<UsmLine> lines = getArguments().getParcelableArrayList(EXTRA_LINES);
    adaptor = new LineAdaptor(lines);
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.dialog_title_choose_line)
        .setAdapter(adaptor, this)
        .create();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    // make sure the activity/context that created the dialog is listening
    try {
      chooseLineDialogListener = (ChooseLineDialogListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context + "must implement ChooseLineDialogListener");
    }
  }

  @Override
  public void onClick(DialogInterface dialog, int position) {
    if (chooseLineDialogListener != null) {
      chooseLineDialogListener.onLineSelected(adaptor.getItem(position));
    }
  }

  /**
   * A callback for when a line is selected in the list.
   */
  public interface ChooseLineDialogListener {

    void onLineSelected(UsmLine line);
  }

  class LineAdaptor extends BaseAdapter implements ListAdapter {

    private final ArrayList<UsmLine> lines;

    LineAdaptor(ArrayList<UsmLine> lines) {
      this.lines = lines;
    }

    @Override
    public int getCount() {
      return lines.size();
    }

    @Override
    public UsmLine getItem(int position) {
      return lines.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view;
      // reuse the old view if possible
      if (convertView == null) {
        view = getLayoutInflater().inflate(R.layout.line_list_item, parent, false);
      } else {
        view = convertView;
      }

      UsmLine line = getItem(position);
      ImageView imageView = view.findViewById(R.id.image);
      TextView nameTextView = view.findViewById(R.id.name);
      TextView phoneTextView = view.findViewById(R.id.phone);
      imageView.setImageBitmap(line.avatar);
      nameTextView.setText(line.name);
      phoneTextView.setText(line.phoneNumber);

      return view;
    }
  }
}
