package com.utility;

import com.bean.ShipmentItem;
import java.util.ArrayList;

/**
 * Created by Avishek on 6/19/2015.
 */
public class Constant{

    public static final String baseUrl = "http://traxeservices.co.in/api/v1/agents/";

    public static final String GOOGLE_PROJECT_ID = "867049677390";

    public static final String JSON_REJECTED_REASON_FILE_NAME = "reason.json";

    public static ArrayList<ShipmentItem> pendingList;
    public static ArrayList<ShipmentItem> intransitList;
    public static ArrayList<ShipmentItem> completedList;
    public static ArrayList<ShipmentItem> rejectedList;
    public static ArrayList<ShipmentItem> searchList;

}
