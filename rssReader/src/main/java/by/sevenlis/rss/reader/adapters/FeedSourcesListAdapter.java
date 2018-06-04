package by.sevenlis.rss.reader.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.classes.FeedSource;
import by.sevenlis.rss.reader.utils.DBLocal;

public class FeedSourcesListAdapter extends RecyclerView.Adapter<FeedSourcesListAdapter.ViewHolder> {
    private List<FeedSource> feedSources;
    
    public FeedSourcesListAdapter(List<FeedSource> feedSources) {
        this.feedSources = feedSources;
    }
    
    @Override
    public FeedSourcesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_source_row_layout, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FeedSource feedSource = feedSources.get(position);
        
        holder.checkBox_source_enabled.setChecked(feedSource.isEnabled());
        holder.textView_source_name.setText(feedSource.getName());
        holder.textView_source_link.setText(feedSource.getSourceUrl());
        
        holder.source_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean newStatement = !feedSource.isEnabled();
                feedSource.setEnabled(newStatement);
                new DBLocal(holder.itemView.getContext()).setFeedSourceEnabled(feedSource,newStatement);
                holder.checkBox_source_enabled.setChecked(feedSource.isEnabled());
                notifyItemChanged(feedSources.indexOf(feedSource));
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return feedSources.size();
    }
    
    public FeedSource getItem(int position) {
        return feedSources.get(position);
    }
    
    public void setItem(int position, FeedSource feedSource) {
        feedSources.set(position,feedSource);
        notifyItemChanged(position);
    }
    
    public void addItem(FeedSource feedSource) {
        feedSources.add(feedSource);
        notifyItemInserted(feedSources.size());
    }
    
    public void removeItem(int position) {
        feedSources.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, feedSources.size());
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout source_row;
        private CheckBox checkBox_source_enabled;
        private TextView textView_source_name;
        private TextView textView_source_link;
        
        ViewHolder(View itemView) {
            super(itemView);
            source_row = LinearLayout.class.cast(itemView.findViewById(R.id.source_row));
            checkBox_source_enabled = CheckBox.class.cast(itemView.findViewById(R.id.checkBox_source_enabled));
            textView_source_name = TextView.class.cast(itemView.findViewById(R.id.textView_source_name));
            textView_source_link = TextView.class.cast(itemView.findViewById(R.id.textView_source_link));
        }
    }
}
