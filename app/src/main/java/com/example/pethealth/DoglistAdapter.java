package com.example.pethealth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static androidx.core.content.ContextCompat.startActivity;
import static java.sql.DriverManager.println;
import androidx.appcompat.app.AppCompatActivity;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class DoglistAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> array_mountain;
    private ViewHolder mViewHolder;

    private Activity activity;

    public DoglistAdapter(Context mContext, ArrayList<String> array_mountain , Activity activity) {
        this.mContext = mContext;
        this.array_mountain = array_mountain;
        this.activity = activity;
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
    public View getView( int position, View convertView, ViewGroup parent) {
        // ViewHolder 패턴
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.dog_list_item_layout, parent, false);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        // View에 Data 세팅
        final String name = array_mountain.get(position);
        final int index = position;
        mViewHolder.btn.setText(name);

        if(mViewHolder.btn != null){
            //buttons.add(btn);

            mViewHolder.btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(mContext, name, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(mContext, HomeActivity.class);
                    intent.putExtra("dog_index", index); // 송신

                    activity.setResult(HOME_RESULT.SELECT_DOG.getIdx(), intent);
                    activity.finish();
                }
            });

        }

        return convertView;
    }



    public class ViewHolder {
        private Button btn;
        public ViewHolder(View convertView) {
            btn = (Button) convertView.findViewById(R.id.tv_dog_name);

        }

    }
}