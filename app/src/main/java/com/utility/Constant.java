package com.utility;

import com.bean.ShipmentItem;
import java.util.ArrayList;

/**
 * Created by Avishek on 6/19/2015.
 */
public class Constant{

    public static final String baseUrl = "http://traxnew.bluehorse.in/index.php/api/v1/agents/";

    public static final String JSON_REJECTED_REASON_FILE_NAME = "reason.json";

    public static ArrayList<ShipmentItem> pendingList;
    public static ArrayList<ShipmentItem> intransitList;
    public static ArrayList<ShipmentItem> completedList;
    public static ArrayList<ShipmentItem> rejectedList;
    public static ArrayList<ShipmentItem> searchList;

}
