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

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Vector;

import org.javia.arity.SyntaxException;


class HistoryAdapter extends BaseAdapter {
    private final Vector<HistoryEntry> mEntries;
    private final LayoutInflater mInflater;
    private final EquationFormatter mEquationFormatter;
    private final History mHistory;
    private final Logic mLogic ;

    HistoryAdapter(Context context, History history ,Logic logic) {
        mEntries = history.mEntries;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mEquationFormatter = new EquationFormatter();
        mHistory = history;
        mLogic = logic;
    }

    @Override
    public int getCount() {
        return mEntries.size() - 1;
    }

    @Override
    public Object getItem(int position) {
        return mEntries.elementAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void insert(int pos ,HistoryEntry he){
    	mHistory.insert(pos, he);
    }
    
    public void remove(int pos){
    	mHistory.remove(pos);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.list_item_history, parent, false);
        }
        else {
            view = convertView;
        }

        View bg = view.findViewById(R.id.bg_holder);
        TextView expr = (TextView) view.findViewById(R.id.historyExpr);
        TextView result = (TextView) view.findViewById(R.id.historyResult);
        if(getCount()==1){
        	bg.setBackgroundResource(R.drawable.history_item_bg_single);
        }else if(position == getCount()-1){
        	bg.setBackgroundResource(R.drawable.history_item_bg_bottom);
        }else if(position == 0){
        	bg.setBackgroundResource(R.drawable.history_item_bg_top);
        }else {
        	bg.setBackgroundResource(R.drawable.history_item_bg_middle);
        }

        HistoryEntry entry = mEntries.elementAt(position);
        expr.setText(Html.fromHtml(mEquationFormatter.insertSupscripts(entry.getBase())));
        result.setText(entry.getEdited());
//        view.setHistoryEntry(entry);
//        view.setHistory(mHistory);
//        view.setAdapter(this);

        return view;
    }

	public void removeAll() {
		mHistory.clear();
	}

	public void addAll() {
		StringBuilder sb = new StringBuilder();
		for(HistoryEntry entry :mEntries){
			if(entry.getEdited().equals("")){
				break;
			}
			sb.append(entry.getEdited()).append("+");
		}
		String strToEvalute = sb.toString();
		while(strToEvalute.endsWith("+")){
			strToEvalute = strToEvalute.substring(0,sb.length()-1);
		}
		Log.d("TCL", "strToEvalute :" + strToEvalute);
	   mLogic.evaluateAndShowResult(strToEvalute, CalculatorDisplay.Scroll.RIGHT);
	}
}

