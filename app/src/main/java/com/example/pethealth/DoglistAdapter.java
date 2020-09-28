package com.example.pethealth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;


public class DoglistAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<DogInfo> array_mountain;
    private ViewHolder mViewHolder;

    private Activity activity;


    public DoglistAdapter(Context mContext, ArrayList<DogInfo> array_mountain , Activity activity) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
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

                    HomeActivity.SetDogIndex(index);

                    activity.setResult(HOME_RESULT.SELECT_DOG.getIdx());
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
                    intent.putExtra("user_id",HomeActivity.user_id);
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

        if(mViewHolder.btn_remove != null){
            //buttons.add(btn);

            mViewHolder.btn_remove.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void onClick(View v) {
                    ConnectToDB data = new ConnectToDB();
                    LoginData login_info = new LoginData( HomeActivity.user_id, "");


                    data.login_data = login_info;
                    data.dog_data = new DogInfo(
                            login_info ,dogInfo.getD_id(), dogInfo.getD_name(),  dogInfo.getD_breed(),
                            dogInfo.getD_height(), dogInfo.getD_length(), dogInfo.getD_weight(),
                            dogInfo.getD_age(),dogInfo.getD_goal_activity()
                    );
                    data.type = DB_MODES.DDEL;
                   //TODO:제거 버튼 :: Error android.os.NetworkOnMainThreadException

                }




            });

        }

        return convertView;
    }






    public class ViewHolder {
        private Button btn_select ,btn_modify,btn_remove;
        public ViewHolder(View convertView) {
            btn_select = (Button) convertView.findViewById(R.id.tv_dog_name);
            btn_modify = convertView.findViewById(R.id.btn_dog_modify);
            btn_remove = convertView.findViewById(R.id.btn_dog_remove);
        }

        @SuppressLint("WrongConstant")
        public void Destroy(){
            btn_select.setVisibility(0);
            btn_modify.setVisibility(0);
            btn_remove.setVisibility(0);
        }

    }
}