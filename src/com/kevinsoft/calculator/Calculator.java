/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kevinsoft.calculator;


import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import de.timroes.android.listview.EnhancedListView;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class Calculator extends Activity implements PanelSwitcher.Listener, Logic.Listener,
        OnClickListener, OnMenuItemClickListener {
    EventListener mListener = new EventListener();
    private CalculatorDisplay mDisplay;
    private Persist mPersist;
    private History mHistory;
    private EnhancedListView mHistoryView;
    private HistoryAdapter mHistoryAdapter;
    private Logic mLogic;
    private ViewPager mPager;
    private View mClearButton;
    private View mBackspaceButton;
    private View mOverflowMenuButton;

    static final int BASIC_PANEL    = 0;
    static final int ADVANCED_PANEL = 1;

    private static final String LOG_TAG = "KevinCalculator";
    private static final boolean DEBUG  = false;
    private static final boolean LOG_ENABLED = true;
    private static final String STATE_CURRENT_VIEW = "state-current-view";

    public static final String SAVED_STATE_ACTION_BAR_HIDDEN = "saved_state_action_bar_hidden";
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Disable IME for this application
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        setContentView(R.layout.main);
        mPager = (ViewPager) findViewById(R.id.panelswitch);
        if (mPager != null) {
            mPager.setAdapter(new PageAdapter(mPager));
        } else {
            // Single page UI
            final TypedArray buttons = getResources().obtainTypedArray(R.array.buttons);
            for (int i = 0; i < buttons.length(); i++) {
                setOnClickListener(null, buttons.getResourceId(i, 0));
            }
            buttons.recycle();
        }

        if (mClearButton == null) {
            mClearButton = findViewById(R.id.clear);
            mClearButton.setOnClickListener(mListener);
            mClearButton.setOnLongClickListener(mListener);
        }
        if (mBackspaceButton == null) {
            mBackspaceButton = findViewById(R.id.del);
            mBackspaceButton.setOnClickListener(mListener);
            mBackspaceButton.setOnLongClickListener(mListener);
        }

        mPersist = new Persist(this);
        mPersist.load();

        mHistory = mPersist.history;

        mDisplay = (CalculatorDisplay) findViewById(R.id.display);

        mLogic = new Logic(this, mHistory, mDisplay);
        mLogic.setListener(this);

        mLogic.setDeleteMode(mPersist.getDeleteMode());
        mLogic.setLineLength(mDisplay.getMaxDigits());

        mHistoryAdapter = new HistoryAdapter(this, mHistory,mLogic);
        mHistory.setObserver(mHistoryAdapter);

        if (mPager != null) {
            mPager.setCurrentItem(state == null ? 0 : state.getInt(STATE_CURRENT_VIEW, 0));
        }

        mListener.setHandler(mLogic, mPager);
        mDisplay.setOnKeyListener(mListener);

        if (!ViewConfiguration.get(this).hasPermanentMenuKey()) {
            createFakeMenu();
        }

        mLogic.resumeWithHistory();
        updateDeleteMode();
        KevinSoundManager.getInstance().initSounds(this);
        
        SlidingUpPanelLayout layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        layout.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
        
        layout.setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(LOG_TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.i(LOG_TAG, "onPanelExpanded");

            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(LOG_TAG, "onPanelCollapsed");

            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(LOG_TAG, "onPanelAnchored");

            }
        });
        
        mHistoryView = (EnhancedListView)findViewById(R.id.historyList);
        
        setUpHistory();
        
        ImageButton ib = (ImageButton)findViewById(R.id.clear_all_history);
        ib.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mHistoryAdapter.removeAll();
				mDisplay.setText("0", CalculatorDisplay.Scroll.RIGHT);
				
			}
		});
        
        ImageButton addAllb = (ImageButton)findViewById(R.id.add_all_history);
        addAllb.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mHistoryAdapter.addAll();
			}
		});
        
    }

    private void setUpHistory() {
        registerForContextMenu(mHistoryView);
        mHistoryView.setAdapter(mHistoryAdapter);
        mHistoryView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mHistoryView.setStackFromBottom(true);
        mHistoryView.setFocusable(false);

        
        mHistoryView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
            /**
             * This method will be called when the user swiped a way or deleted it via
             * {@link de.timroes.android.listview.EnhancedListView#delete(int)}.
             *
             * @param listView The {@link EnhancedListView} the item has been deleted from.
             * @param position The position of the item to delete from your adapter.
             * @return An {@link de.timroes.android.listview.EnhancedListView.Undoable}, if you want
             *      to give the user the possibility to undo the deletion.
             */
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

                final HistoryEntry item = (HistoryEntry) mHistoryAdapter.getItem(position);
                mHistoryAdapter.remove(position);
                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                    	mHistoryAdapter.insert(position, item);
                    }
                };
            }
        });
   	 	
        mHistoryView.enableSwipeToDismiss();
        mHistoryView.setSwipeDirection(EnhancedListView.SwipeDirection.BOTH);
        mHistoryView.setUndoStyle(EnhancedListView.UndoStyle.MULTILEVEL_POPUP);
        mHistoryView.setSwipingLayout(R.id.bg_holder);
    }
    
    private void updateDeleteMode() {
        if (mLogic.getDeleteMode() == Logic.DELETE_MODE_BACKSPACE) {
            mClearButton.setVisibility(View.GONE);
            mBackspaceButton.setVisibility(View.VISIBLE);
        } else {
            mClearButton.setVisibility(View.VISIBLE);
            mBackspaceButton.setVisibility(View.GONE);
        }
    }

    void setOnClickListener(View root, int id) {
        final View target = root != null ? root.findViewById(id) : findViewById(id);
        target.setOnClickListener(mListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.basic).setVisible(!getBasicVisibility());
        menu.findItem(R.id.advanced).setVisible(!getAdvancedVisibility());
        return true;
    }

    /**
     * This is a tricky method to show overflow menu icon
     * on the action bar.
     */
    private void createFakeMenu() {
        mOverflowMenuButton = findViewById(R.id.overflow_menu);
        if (mOverflowMenuButton != null) {
            mOverflowMenuButton.setVisibility(View.VISIBLE);
            mOverflowMenuButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.overflow_menu:

                break;
        }
    }



    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onOptionsItemSelected(item);
    }

    private boolean getBasicVisibility() {
        return mPager != null && mPager.getCurrentItem() == BASIC_PANEL;
    }

    private boolean getAdvancedVisibility() {
        return mPager != null && mPager.getCurrentItem() == ADVANCED_PANEL;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_history:
                mHistory.clear();
                mLogic.onClear();
                break;

            case R.id.basic:
                if (!getBasicVisibility() && mPager != null) {
                    mPager.setCurrentItem(BASIC_PANEL, true);
                }
                break;

            case R.id.advanced:
                if (!getAdvancedVisibility() && mPager != null) {
                    mPager.setCurrentItem(ADVANCED_PANEL, true);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (mPager != null) {
            state.putInt(STATE_CURRENT_VIEW, mPager.getCurrentItem());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mLogic.updateHistory();
        mPersist.setDeleteMode(mLogic.getDeleteMode());
        mPersist.save();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getAdvancedVisibility()
                && mPager != null) {
            mPager.setCurrentItem(BASIC_PANEL);
            return true;
        } else {
            return super.onKeyDown(keyCode, keyEvent);
        }
    }

    static void log(String message) {
        if (LOG_ENABLED) {
            Log.v(LOG_TAG, message);
        }
    }

    @Override
    public void onChange() {
        invalidateOptionsMenu();
    }

    @Override
    public void onDeleteModeChange() {
        updateDeleteMode();
    }

    class PageAdapter extends PagerAdapter {
        private View mSimplePage;
        private View mAdvancedPage;

        public PageAdapter(ViewPager parent) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View simplePage = inflater.inflate(R.layout.simple_pad, parent, false);
            final View advancedPage = inflater.inflate(R.layout.advanced_pad, parent, false);
            mSimplePage = simplePage;
            mAdvancedPage = advancedPage;

            final Resources res = getResources();
            final TypedArray simpleButtons = res.obtainTypedArray(R.array.simple_buttons);
            for (int i = 0; i < simpleButtons.length(); i++) {
                setOnClickListener(simplePage, simpleButtons.getResourceId(i, 0));
            }
            simpleButtons.recycle();

            final TypedArray advancedButtons = res.obtainTypedArray(R.array.advanced_buttons);
            for (int i = 0; i < advancedButtons.length(); i++) {
                setOnClickListener(advancedPage, advancedButtons.getResourceId(i, 0));
            }
            advancedButtons.recycle();

            final View clearButton = simplePage.findViewById(R.id.clear);
            if (clearButton != null) {
                mClearButton = clearButton;
            }

            final View backspaceButton = simplePage.findViewById(R.id.del);
            if (backspaceButton != null) {
                mBackspaceButton = backspaceButton;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public void startUpdate(View container) {
        }

        @Override
        public Object instantiateItem(View container, int position) {
            final View page = position == 0 ? mSimplePage : mAdvancedPage;
            ((ViewGroup) container).addView(page);
            return page;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewGroup) container).removeView((View) object);
        }

        @Override
        public void finishUpdate(View container) {
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }
    }
}
