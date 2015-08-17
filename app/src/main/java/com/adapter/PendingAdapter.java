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
 * Created by Avishek on 6/20/2015.
 */
public class PendingAdapter extends ArrayAdapter<ShipmentItem> {

    private LayoutInflater inflater;
    private Context mContext;

    public PendingAdapter(Context context,
                               ArrayList<ShipmentItem> pendingList) {
        // TODO Auto-generated constructor stub
        super(context, R.layout.pending_row, R.id.tvShipmentTitle,
                pendingList);
        this.mContext = context;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        final ShipmentItem surveyList = (ShipmentItem) this
                .getItem(position);

        ViewHolder holder;
        holder = new ViewHolder();

        convertView = inflater.inflate(R.layout.pending_row, null);

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
        holder.tvShipmentType.setText(surveyList.getShipmentType().toUpperCase());

        if(surveyList.getShipmentType().equals("pickup")){

            holder.tvShipmentTitle.setText(surveyList.getPickupName());
            holder.tvShipmentAddress.setText(surveyList.getPickupHomeName() + ", " + surveyList.getPickupStreetName() + ", " + surveyList.getPickupLocationName() + ", " + "Near " + surveyList.getPickupLandmark() + ", " + surveyList.getPickupCityName() + " - " + surveyList.getPickupPincode());
            holder.llShipmentBack.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CB2D22")));

        }

        if(surveyList.getShipmentType().equals("cms")){

            holder.tvShipmentTitle.setText(surveyList.getPickupName());
            holder.tvShipmentAddress.setText(surveyList.getPickupHomeName() + ", " + surveyList.getPickupStreetName() + ", " + surveyList.getPickupLocationName() + ", " + "Near " + surveyList.getPickupLandmark() + ", " + surveyList.getPickupCityName() + " - " + surveyList.getPickupPincode());
            holder.llShipmentBack.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#08B6FF")));
        }

        if(surveyList.getShipmentType().equals("delivery")){

            holder.tvShipmentTitle.setText(surveyList.getDeliveryName());
            holder.tvShipmentAddress.setText(surveyList.getDeliveryHomeName() + ", " + surveyList.getDeliveryStreetName() + ", " + surveyList.getDeliveryLocationName() + ", " + "Near " + surveyList.getDeliveryLandmark() + ", " + surveyList.getDeliveryCityName() + " - " + surveyList.getDeliveryPincode());
            holder.llShipmentBack.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#49AC03")));
        }

        if(surveyList.getShipmentType().equals("cod")){

            holder.tvShipmentTitle.setText(surveyList.getDeliveryName());
            holder.tvShipmentAddress.setText(surveyList.getDeliveryHomeName() + ", " + surveyList.getDeliveryStreetName() + ", " + surveyList.getDeliveryLocationName() + ", " + "Near " + surveyList.getDeliveryLandmark() + ", " + surveyList.getDeliveryCityName() + " - " + surveyList.getDeliveryPincode());
            holder.llShipmentBack.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#AE6E02")));
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

