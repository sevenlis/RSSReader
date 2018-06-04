package by.sevenlis.rss.reader.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.activities.MainActivity;
import by.sevenlis.rss.reader.adapters.FeedEntitiesListAdapter;
import by.sevenlis.rss.reader.classes.FeedEntity;
import by.sevenlis.rss.reader.classes.FeedEntitySearchDialog;
import by.sevenlis.rss.reader.intents.FeedUpdateServiceIntents;
import by.sevenlis.rss.reader.service.FeedDownloadService;
import by.sevenlis.rss.reader.utils.DBLocal;

public class FeedEntitiesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FeedEntitySearchDialog.FeedEntitySearchDialogListener {
    public static final String ENTITY_STATE_KEY = "ENTITY_STATE_KEY";
    public static final int ENTITY_STATE_UNREAD = 0;
    public static final int ENTITY_STATE_READ = 1;
    public static final int ENTITY_STATE_ALL = -1;
    
    private static final int MENU_SEARCH = Menu.FIRST;
    private static final int MENU_RENEW = Menu.FIRST + 1;
    private static final int MENU_ACTION = Menu.FIRST + 2;
    
    private RecyclerView feedEntitiesRecyclerView;
    private FeedEntitiesListAdapter feedEntitiesListAdapter;
    private List<FeedEntity> feedEntities;
    private Paint paint = new Paint();
    private DBLocal dbLocal;
    private int entitiesState;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context ctx;
    
    public FeedEntitiesFragment() {}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_entities_fragment_layout,container,false);
        setHasOptionsMenu(true);
    
        ctx = getActivity();
        
        dbLocal = new DBLocal(ctx);
        
        feedEntities = getFeedEntities("");
        
        entitiesState = ENTITY_STATE_ALL;
        if (getArguments() != null) {
            entitiesState = getArguments().getInt(ENTITY_STATE_KEY);
        }
        
        progressBar = ProgressBar.class.cast(view.findViewById(R.id.progressBar));
        progressBar.setVisibility(View.GONE);
        tvProgress = TextView.class.cast(view.findViewById(R.id.tv_progress));
        tvProgress.setVisibility(View.GONE);
        
        feedEntitiesRecyclerView = RecyclerView.class.cast(view.findViewById(R.id.feed_entities_recycler_view));
        feedEntitiesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ctx);
        feedEntitiesRecyclerView.setLayoutManager(layoutManager);
    
        feedEntitiesListAdapter = new FeedEntitiesListAdapter(feedEntities, ctx);
        feedEntitiesRecyclerView.setAdapter(feedEntitiesListAdapter);
        
        swipeRefreshLayout = SwipeRefreshLayout.class.cast(view.findViewById(R.id.feed_entities_swipe_refresh_layout));
        swipeRefreshLayout.setOnRefreshListener(FeedEntitiesFragment.this);
        
        if (entitiesState != ENTITY_STATE_ALL) {
            initSwipe();
        }
    
        return view;
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getFeedEntitiesAsync(true);
    }
    
    private List<FeedEntity> getFeedEntities(String search) {
        if (feedEntities == null) feedEntities = new ArrayList<>();
        
        feedEntities.clear();
        if (search.isEmpty()) {
            if (entitiesState == ENTITY_STATE_ALL) {
                feedEntities.addAll(dbLocal.getAllEntities());
            } else {
                feedEntities.addAll(dbLocal.getEntities(entitiesState == ENTITY_STATE_READ));
            }
        } else {
            feedEntities.addAll(dbLocal.searchEntities(search));
        }
        return feedEntities;
    }
    
    private void getFeedEntitiesAsync(final boolean showProgress) {
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (showProgress) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            feedEntitiesRecyclerView.setVisibility(View.GONE);
                            tvProgress.setText(R.string.loading);
                            tvProgress.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setIndeterminate(true);
                        }
                    });
                }

                feedEntities = getFeedEntities("");
                
                if (showProgress) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvProgress.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            feedEntitiesRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });
                }
                
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        feedEntitiesListAdapter.setFeedEntities(feedEntities);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }
    
    private void performOptionsActionAsync() {
        final Handler mHandler = new Handler();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        feedEntitiesRecyclerView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setIndeterminate(true);
                    }
                });
    
                feedEntities = feedEntitiesListAdapter.getFeedEntities();
                for (FeedEntity feedEntity : feedEntities) {
                    if (entitiesState == ENTITY_STATE_UNREAD) {
                        dbLocal.setFeedEntityRead(feedEntity,true);
                    } else if (entitiesState == ENTITY_STATE_ALL || entitiesState == ENTITY_STATE_READ) {
                        dbLocal.deleteFeedEntity(feedEntity);
                    }
                }
                
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        feedEntitiesListAdapter.removeAll();
                        
                        progressBar.setVisibility(View.GONE);
                        feedEntitiesRecyclerView.setVisibility(View.VISIBLE);
                    }
                });
                
            }
        }).start();
    }
    
    private void performOnSwiped(int position, int direction) {
        if (direction == ItemTouchHelper.LEFT) {
            if (entitiesState == ENTITY_STATE_UNREAD) {
                dbLocal.setFeedEntityRead(feedEntitiesListAdapter.getItem(position),true);
            } else if (entitiesState == ENTITY_STATE_READ || entitiesState == ENTITY_STATE_ALL) {
                dbLocal.deleteFeedEntity(feedEntitiesListAdapter.getItem(position));
            }
        } else if (direction == ItemTouchHelper.RIGHT) {
            FeedEntity feedEntity = feedEntitiesListAdapter.getItem(position);
            Uri link = Uri.parse(feedEntity.getStringLink());
            Intent intent = new Intent(Intent.ACTION_VIEW,link);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
    
            dbLocal.setFeedEntityRead(feedEntitiesListAdapter.getItem(position),true);
        }
        feedEntitiesListAdapter.removeItem(position);
    }
    
    private Bitmap getSwipeIcon() {
        if (entitiesState == ENTITY_STATE_UNREAD) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_read_white);
        } else if (entitiesState == ENTITY_STATE_READ || entitiesState == ENTITY_STATE_ALL) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
        } else {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_error);
        }
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
                performOnSwiped(position, direction);
            }
            
            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    View itemView = viewHolder.itemView;
                    //float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = MainActivity.convertDpToPixel(ctx,32); //height / 3;
                    paint.setColor(getResources().getColor(R.color.dark_grey));
                    
                    if (dX > 0) {
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        canvas.drawRect(background,paint);
                        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_browser_white);
                        //RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft() + 2 * width,(float)itemView.getBottom() - width);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width * 0.5f ,(float) itemView.getTop() + width * 0.5f,(float) itemView.getLeft() + width * 1.5f,(float)itemView.getTop() + width * 1.5f);
                        canvas.drawBitmap(icon,null,icon_dest,paint);
                    } else if (dX < 0) {
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        canvas.drawRect(background,paint);
                        //RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        RectF icon_dest = new RectF((float) itemView.getRight() - width * 1.5f ,(float) itemView.getTop() + width * 0.5f,(float) itemView.getRight() - width * 0.5f,(float)itemView.getTop() + width * 1.5f);
                        canvas.drawBitmap(getSwipeIcon(),null,icon_dest,paint);
                    }
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(feedEntitiesRecyclerView);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        menu.add(0,MENU_SEARCH,Menu.NONE, R.string.search)
                .setIcon(R.drawable.ic_search_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(1, MENU_RENEW, Menu.NONE, R.string.action_renew)
                .setIcon(R.drawable.ic_renew_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        if (entitiesState == ENTITY_STATE_UNREAD) {
            menu.add(2,MENU_ACTION,Menu.NONE,R.string.mark_all_read)
                    .setIcon(R.drawable.ic_read_white)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        } else if (entitiesState == ENTITY_STATE_ALL || entitiesState == ENTITY_STATE_READ) {
            menu.add(2,MENU_ACTION,Menu.NONE,R.string.remove_all)
                    .setIcon(R.drawable.ic_delete_white)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ACTION) {
            performOptionsActionAsync();
        } else if (item.getItemId() == MENU_RENEW) {
            performFeedDownload();
        } else if (item.getItemId() == MENU_SEARCH) {
            showEntitySearchDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void performFeedDownload() {
        Intent intent = FeedUpdateServiceIntents.getIntent(getActivity().getBaseContext());
        intent.setAction(FeedDownloadService.FEED_UPDATE_ACTION_START);
        intent.putExtra("receiver",new FeedDownloadResultReceiver(new Handler()));
        getActivity().getBaseContext().startService(intent);
    }
    
    @Override
    public void onRefresh() {
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                getFeedEntitiesAsync(false);
            }
        },0L);
    }
    
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {}
    
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        getFeedEntitiesAsync(true);
    }
    
    @Override
    public void onSearchStringChanged(DialogFragment dialog) {
        feedEntities = getFeedEntities(((FeedEntitySearchDialog)dialog).getSearchString());
        feedEntitiesListAdapter.setFeedEntities(feedEntities);
    }
    
    private void showEntitySearchDialog() {
        DialogFragment dialogFragment = new FeedEntitySearchDialog();
        dialogFragment.show(getActivity().getFragmentManager(),FeedEntitySearchDialog.TAG);
    }
    
    private class FeedDownloadResultReceiver extends ResultReceiver {
        FeedDownloadResultReceiver(Handler handler) {
            super(handler);
        }
    
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == FeedDownloadService.SERVICE_STARTED) {
                Snackbar.make(feedEntitiesRecyclerView,"Feed updates started...",Snackbar.LENGTH_LONG).show();
            } else if (resultCode == FeedDownloadService.SERVICE_STOPPED) {
                Snackbar.make(feedEntitiesRecyclerView,"Feed updates complete...",Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
