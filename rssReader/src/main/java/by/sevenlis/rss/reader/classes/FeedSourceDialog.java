package by.sevenlis.rss.reader.classes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.activities.MainActivity;

public class FeedSourceDialog extends DialogFragment {
    public static final String TAG = "FeedSourceDialog.TAG";
    private final String FEED_SOURCE_KEY = "FeedSourceDialog.feedSource";
    private FeedSourceDialogOnClickListener clickListener;
    private FeedSource feedSource = new FeedSource();
    private EditText etName;
    private EditText etLink;
    private CheckBox enable;
    
    public interface FeedSourceDialogOnClickListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clickListener = (FeedSourceDialogOnClickListener) getFragmentManager().findFragmentByTag(MainActivity.FEED_SOURCES_FRAGMENT_TAG);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (savedInstanceState != null) {
            this.feedSource = savedInstanceState.getParcelable(FEED_SOURCE_KEY);
        }
    
        if (this.feedSource != null) {
            etName.setText(this.feedSource.getName());
            etLink.setText(this.feedSource.getSourceUrl());
            enable.setChecked(this.feedSource.isEnabled());
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FEED_SOURCE_KEY,feedSource);
    }
    
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.feed_source_layout,null);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string._OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                feedSource.setName(etName.getText().toString());
                feedSource.setSourceUrl(etLink.getText().toString());
                feedSource.setEnabled(enable.isChecked());
                clickListener.onDialogPositiveClick(FeedSourceDialog.this);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clickListener.onDialogNegativeClick(FeedSourceDialog.this);
            }
        });
        
        etName = view.findViewById(R.id.et_source_name);
        etLink = view.findViewById(R.id.et_source_link);
        enable = view.findViewById(R.id.cb_source_enabled);
        
        return builder.create();
    }
    
    public FeedSource getFeedSource() {
        return feedSource;
    }
    
    public void setFeedSource(FeedSource feedSource) {
        this.feedSource = feedSource;
    }
}
