package by.sevenlis.rss.reader.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.activities.MainActivity;
import by.sevenlis.rss.reader.classes.FeedEntity;
import by.sevenlis.rss.reader.fragments.ViewEntityFragment;
import by.sevenlis.rss.reader.utils.DBLocal;

public class FeedEntitiesListAdapter extends RecyclerView.Adapter<FeedEntitiesListAdapter.ViewHolder> {
    private List<FeedEntity> feedEntities;
    private Context context;
    
    public FeedEntitiesListAdapter(List<FeedEntity> feedEntities, Context context) {
        this.feedEntities = feedEntities;
        this.context = context;
    }
    
    @Override
    public FeedEntitiesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_entity_row_layout, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FeedEntity feedEntity = this.feedEntities.get(position);
        
        holder.tvDate.setText(feedEntity.getDatePublishedFormatted());
        holder.tvSource.setText(feedEntity.getFeedSource().getName());
        holder.tvTitle.setText(Html.fromHtml(feedEntity.getTitle()).toString());
        String description = Html.fromHtml(feedEntity.getDescription()).toString();
        if (feedEntity.getImageUrl().isEmpty()) {
            holder.entityImage.setVisibility(View.GONE);
            holder.tvDescription.setText(description);
        } else {
            holder.entityImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(feedEntity.getImageUrl()).into(holder.entityImage);
            
            SpannableString spannableString = new SpannableString(description);
            int leftMargin = ((int) MainActivity.convertDpToPixel(context, 110)) + 20;
            spannableString.setSpan(new EntityLeadingMarginSpan(8,leftMargin),0,spannableString.length(),0);
            holder.tvDescription.setText(spannableString);
        }
        holder.entityRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!feedEntity.isRead()) {
                    new DBLocal(context).setFeedEntityRead(getItem(holder.getAdapterPosition()),true);
                    removeItem(holder.getAdapterPosition());
    
                    Bundle args = new Bundle();
                    args.putString("entityUrl",feedEntity.getStringLink());
                    ViewEntityFragment fragment = new ViewEntityFragment();
                    fragment.setArguments(args);
                    ((Activity) context).getFragmentManager().beginTransaction().replace(R.id.frame_content,fragment).addToBackStack(MainActivity.FEED_ENTITIES_FRAGMENT_TAG).commit();
                }
            }
        });
        if (feedEntity.isRead()) {
            holder.tvTitle.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
            holder.rlContent.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
        }
    }
    
    @Override
    public int getItemCount() {
        return this.feedEntities.size();
    }
    
    public void setItem(int position, FeedEntity feedEntity) {
        this.feedEntities.set(position,feedEntity);
        notifyItemChanged(position);
    }
    
    public FeedEntity getItem(int position) {
        return this.feedEntities.get(position);
    }
    
    public void addItem(FeedEntity feedEntity) {
        this.feedEntities.add(feedEntity);
        notifyItemInserted(feedEntities.size());
    }
    
    public void removeItem(int position) {
        this.feedEntities.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, feedEntities.size());
    }
    
    public void removeAll() {
        this.feedEntities.clear();
        notifyDataSetChanged();
    }
    
    public void setFeedEntities(List<FeedEntity> feedEntities) {
        this.feedEntities = feedEntities;
        notifyDataSetChanged();
    }
    
    public List<FeedEntity> getFeedEntities() {
        return this.feedEntities;
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout entityRow;
        TextView tvDate;
        TextView tvSource;
        TextView tvTitle;
        TextView tvDescription;
        ImageView entityImage;
        RelativeLayout rlContent;
        
        ViewHolder(View itemView) {
            super(itemView);
            entityRow = itemView.findViewById(R.id.entity_row);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            entityImage = itemView.findViewById(R.id.entityImage);
            rlContent = itemView.findViewById(R.id.rlContent);
        }
    }
    
    class EntityLeadingMarginSpan implements LeadingMarginSpan.LeadingMarginSpan2 {
        private int margin;
        private int lines;
        
        EntityLeadingMarginSpan(int lines, int margin) {
            this.margin = margin;
            this.lines = lines;
        }
        
        @Override
        public int getLeadingMargin(boolean first) {
            if (first) {
                return margin;
            } else {
                return 0;
            }
        }
        
        @Override
        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                      int top, int baseline, int bottom, CharSequence text,
                                      int start, int end, boolean first, Layout layout) {}
        
        @Override
        public int getLeadingMarginLineCount() {
            return lines;
        }
    }
}
