package com.example.pethealth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;



public class DoglistAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> array_mountain;
    private ViewHolder mViewHolder;

    public DoglistAdapter(Context mContext, ArrayList<String> array_mountain) {
        this.mContext = mContext;
        this.array_mountain = array_mountain;
    }

    @Override
    public int getCount() {
        return array_mountain.size();
    }

    @Override
    public Object getItem(int position) {
        return array_mountain.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder 패턴
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.dog_list_item_layout, parent, false);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        // View에 Data 세팅
        mViewHolder.txt_name.setText(array_mountain.get(position));

        return convertView;
    }



    public class ViewHolder {
        private TextView txt_name;

        public ViewHolder(View convertView) {
            txt_name = (TextView) convertView.findViewById(R.id.tv_dog_name);
        }
    }
}