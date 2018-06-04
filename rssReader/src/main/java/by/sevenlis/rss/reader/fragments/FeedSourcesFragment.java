package by.sevenlis.rss.reader.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.adapters.FeedSourcesListAdapter;
import by.sevenlis.rss.reader.classes.FeedSource;
import by.sevenlis.rss.reader.classes.FeedSourceDialog;
import by.sevenlis.rss.reader.utils.DBLocal;

public class FeedSourcesFragment extends Fragment implements FeedSourceDialog.FeedSourceDialogOnClickListener {
    private final int MENU_SETTINGS_ITEM_ID = 0;
    
    private RecyclerView sourcesRecyclerView;
    private FeedSourcesListAdapter feedSourcesListAdapter;
    private Paint paint = new Paint();
    private DBLocal dbLocal;
    private List<FeedSource> feedSources;
    
    public FeedSourcesFragment() {}
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_sources_fragment_layout,container,false);
        setHasOptionsMenu(true);
    
        Context context = getActivity().getApplicationContext();
        
        dbLocal = new DBLocal(context);
    
        feedSources = dbLocal.getAllFeedSources();
    
        sourcesRecyclerView = RecyclerView.class.cast(view.findViewById(R.id.feed_sources_recycler_view));
        sourcesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        sourcesRecyclerView.setLayoutManager(layoutManager);
    
        feedSourcesListAdapter = new FeedSourcesListAdapter(feedSources);
        sourcesRecyclerView.setAdapter(feedSourcesListAdapter);
        feedSourcesListAdapter.notifyDataSetChanged();
        
        FloatingActionButton fab = FloatingActionButton.class.cast(view.findViewById(R.id.fab));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFeedSourceDialog(null);
            }
        });
        
        initSwipe();
    
        return view;
    }
    
    private void showFeedSourceDialog(Integer position) {
        FeedSource feedSource = new FeedSource();
        if (position != null) {
            feedSource = feedSourcesListAdapter.getItem(position);
        }
        DialogFragment dialogFragment = new FeedSourceDialog();
        FeedSourceDialog.class.cast(dialogFragment).setFeedSource(feedSource);
        dialogFragment.show(getFragmentManager(), FeedSourceDialog.TAG);
    }
    
    private void initSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                
                if (direction == ItemTouchHelper.LEFT){
                    dbLocal.deleteFeedSource(feedSourcesListAdapter.getItem(position));
                    feedSourcesListAdapter.removeItem(position);
                    
                } else if (direction == ItemTouchHelper.RIGHT) {
                    showFeedSourceDialog(position);
                }
            }
            
            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;
                    paint.setColor(getResources().getColor(R.color.dark_grey));
                    
                    if (dX > 0) {
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        canvas.drawRect(background,paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit_white);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        canvas.drawBitmap(icon,null,icon_dest,paint);
                    } else if (dX < 0) {
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        canvas.drawRect(background,paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        canvas.drawBitmap(icon,null,icon_dest,paint);
                    }
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
    
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(sourcesRecyclerView);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(0,MENU_SETTINGS_ITEM_ID,Menu.NONE,"Settings");
        item.setIcon(R.drawable.ic_settings_white);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_SETTINGS_ITEM_ID) {
            getFragmentManager().beginTransaction().replace(R.id.frame_content,new SettingsFragment()).commit();
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        FeedSource feedSource = FeedSourceDialog.class.cast(dialog).getFeedSource();
        if (dbLocal.getFeedSource(feedSource.getGuid()) == null) {
            dbLocal.addFeedSource(feedSource);
            feedSourcesListAdapter.addItem(feedSource);
        } else {
            dbLocal.updateFeedSource(feedSource);
            if (feedSources.contains(feedSource)) {
                feedSourcesListAdapter.setItem(feedSources.indexOf(feedSource),feedSource);
            }
        }
    }
    
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        feedSourcesListAdapter.notifyDataSetChanged();
    }
}
