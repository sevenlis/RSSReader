package by.sevenlis.rss.reader.classes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.activities.MainActivity;

public class FeedEntitySearchDialog extends DialogFragment {
    public static final String TAG = "FeedEntitySearchDialog.TAG";
    private EditText etSearch;
    private FeedEntitySearchDialogListener mListener;
    
    public interface FeedEntitySearchDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
        void onSearchStringChanged(DialogFragment dialog);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener = (FeedEntitySearchDialogListener) getActivity().getFragmentManager().findFragmentByTag(MainActivity.FEED_ENTITIES_FRAGMENT_TAG);
    }
    
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.feed_entities_search_dialog,null);
    
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string._OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onDialogPositiveClick(FeedEntitySearchDialog.this);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onDialogNegativeClick(FeedEntitySearchDialog.this);
            }
        });
    
        etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mListener.onSearchStringChanged(FeedEntitySearchDialog.this);
            }
    
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        
        AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            etSearch.requestFocus();
            alertDialog.getWindow().setGravity(Gravity.TOP);
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    
        return alertDialog;
    }
    
    public String getSearchString() {
        return etSearch.getText().toString();
    }
}
