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
    private ArrayList<DogInfo> array_mountain;
    private ViewHolder mViewHolder;

    private Activity activity;
    private String id;
    private  ArrayList<DogInfo> dogInfo;
    public DoglistAdapter(Context mContext, ArrayList<DogInfo> array_mountain , Activity activity, String id) {
        this.mContext = mContext;
        this.array_mountain = array_mountain;
        this.activity = activity;
        this.id= id;
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

        final DogInfo dogInfo = array_mountain.get(position);
        final int index = position;
        mViewHolder.btn_select.setText(dogInfo.getD_name());

        if(mViewHolder.btn_select != null){
            //buttons.add(btn);

            mViewHolder.btn_select.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(mContext, dogInfo.getD_name(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(mContext, HomeActivity.class);
                    intent.putExtra("dog_index", index); // 송신
                    intent.putExtra("dog_list",array_mountain);

                    activity.setResult(HOME_RESULT.SELECT_DOG.getIdx(), intent);
                    activity.finish();
                }
            });

        }
        if(mViewHolder.btn_modify != null){
            //buttons.add(btn);

            mViewHolder.btn_modify.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(mContext, dogInfo.getD_name(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(mContext, NewDogActivity.class);
                    intent.putExtra("user_id",id);
                    intent.putExtra("dname",dogInfo.getD_name());
                    intent.putExtra("dbreed",dogInfo.getD_breed());
                    intent.putExtra("dheight",dogInfo.getD_height());
                    intent.putExtra("dlength",dogInfo.getD_length());
                    intent.putExtra("dweight",dogInfo.getD_weight());
                    intent.putExtra("dage",dogInfo.getD_age());
                    intent.putExtra("dgoal",dogInfo.getD_goal_activity()+"");
                    intent.putExtra("d_id",dogInfo.getD_id());

                    activity.startActivityForResult(intent, 1);


                }
            });

        }
        return convertView;
    }



    public class ViewHolder {
        private Button btn_select ,btn_modify;
        public ViewHolder(View convertView) {
            btn_select = (Button) convertView.findViewById(R.id.tv_dog_name);
            btn_modify = convertView.findViewById(R.id.btn_dog_modify);

        }

    }
}