package com.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bean.ShipmentItem;
import com.trax.R;
import java.util.ArrayList;

/**
 * Created by Avishek on 7/23/2015.
 */
public class SearchAdapter extends ArrayAdapter<ShipmentItem> {

    private LayoutInflater inflater;
    private Context mContext;

    public SearchAdapter(Context context,
                         ArrayList<ShipmentItem> SearchList) {
        // TODO Auto-generated constructor stub
        super(context, R.layout.search_row, R.id.tvShipmentTitle,
                SearchList);
        this.mContext = context;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        final ShipmentItem surveyList = (ShipmentItem) this
                .getItem(position);

        ViewHolder holder;
        holder = new ViewHolder();

        convertView = inflater.inflate(R.layout.search_row, null);

        holder.tvShipmentTime = (TextView) convertView
                .findViewById(R.id.tvShipmentTime);
        holder.tvShipmentTitle = (TextView) convertView
                .findViewById(R.id.tvShipmentTitle);
        holder.tvShipmentAddress = (TextView) convertView
                .findViewById(R.id.tvShipmentAddress);
        holder.tvShipmentType = (TextView) convertView
                .findViewById(R.id.tvShipmentType);
        holder.llShipmentBack = (LinearLayout) convertView
                .findViewById(R.id.llShipmentBack);

        convertView.setTag(holder);
        holder = (ViewHolder) convertView.getTag();

        holder.tvShipmentTime.setText(surveyList.getShipmentPickupTime());

        if(surveyList.getShipmentStatus().equals("702")){

            holder.tvShipmentTitle.setText(surveyList.getPickupName());
            holder.tvShipmentAddress.setText(surveyList.getPickupHomeName() + ", " + surveyList.getPickupStreetName() + ", " + surveyList.getPickupLocationName() + ", " + "Near " + surveyList.getPickupLandmark() + ", " + surveyList.getPickupCityName() + " - " + surveyList.getPickupPincode());
            holder.llShipmentBack.setBackgroundDrawable(new ColorDrawable(Color.RED));
            holder.tvShipmentType.setText("PENDING");

        }

        if(surveyList.getShipmentStatus().equals("703") || surveyList.getShipmentStatus().equals("705")){

            holder.tvShipmentTitle.setText(surveyList.getPickupName());
            holder.tvShipmentAddress.setText(surveyList.getPickupHomeName() + ", " + surveyList.getPickupStreetName() + ", " + surveyList.getPickupLocationName() + ", " + "Near " + surveyList.getPickupLandmark() + ", " + surveyList.getPickupCityName() + " - " + surveyList.getPickupPincode());
            holder.llShipmentBack.setBackgroundDrawable(new ColorDrawable(Color.RED));
            holder.tvShipmentType.setText("INTRANSIT");
        }

        if(surveyList.getShipmentStatus().equals("704")){

            holder.tvShipmentTitle.setText(surveyList.getDeliveryName());
            holder.tvShipmentAddress.setText(surveyList.getDeliveryHomeName() + ", " + surveyList.getDeliveryStreetName() + ", " + surveyList.getDeliveryLocationName() + ", " + "Near " + surveyList.getDeliveryLandmark() + ", " + surveyList.getDeliveryCityName() + " - " + surveyList.getDeliveryPincode());
            holder.llShipmentBack.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#49AC03")));
            holder.tvShipmentType.setText("COMPLETED");
        }

        if(surveyList.getShipmentStatus().equals("700")){

            holder.tvShipmentTitle.setText(surveyList.getDeliveryName());
            holder.tvShipmentAddress.setText(surveyList.getDeliveryHomeName() + ", " + surveyList.getDeliveryStreetName() + ", " + surveyList.getDeliveryLocationName() + ", " + "Near " + surveyList.getDeliveryLandmark() + ", " + surveyList.getDeliveryCityName() + " - " + surveyList.getDeliveryPincode());
            holder.llShipmentBack.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            holder.tvShipmentType.setText("REJECTED");
        }


        return convertView;
    }

    public class ViewHolder {

        TextView tvShipmentTime;
        TextView tvShipmentTitle;
        TextView tvShipmentAddress;
        TextView tvShipmentType;
        LinearLayout llShipmentBack;

    }
}
