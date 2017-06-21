/////////////////////////////////////////////////////////////////////
//
//	TestInAppBilling
//	The TestInAppBilling activity is a demo application to
//	test the InAppBilling class.
//
//	InAppBilling
//	The InAppBilling class handles the in-app billing flow for
//	purchasing of one item. The purchased item can be "inapp" for
//	one-time purchases or "subs" for subscription. The program
//	logic steps are:
//	1. Instantiate InAppBilling object.
//	2. Call startServiceConnection method. This method will create
//	a serviceConnection and bind it to Google Play Store service
//  on the device. The binding process is asynchronous. When
//  the binding process is done the system will call serviceConnected
//  method.
//  3. The serviceConnected method will test if in-app billing
//  service is available on this device.
//  4. If in-app billing is supported, the method will check if
//  the item to be purchased is available for sale.
//	5. If the item is available for sale, the method will check if
//	it is already owned by the customer.
//	6. If the item is not owned by the customer, the program will
//  send an asynchronous request to purchase the item.
//	7. When the request was processed by Google Play Store
//  The system calls onActivityResult method in that parent
//  activity class This call is transferred to onActivityResult
//	method in this class.
//	8. The InAppBilling class checks the result. There are four
//	possible outcomes: (1) User canceled (2) Error (3) Result is
//	ok but verifying returned data failed (4) Purchase is successful.
//	9. In all of these cases, the class will un-bind from the service
//	and call either inAppBillingCancel, inAppBillingError or
//	inAppBillingSuccess.
//	10. After step 5 if the item is owned and the program is running
//	in production mode, the class will unbind the service
//	connection to Google Play Store and call the
//	inAppBillingItemAlreadyOwned call back method.
//	11. If the item is owned by the customer and the program is
//	running in debug mode, the class will consume the item. Un-bind
//	the service connection to Google Play Store and call the
//	inAppBillingItemAlreadyOwned call back method.
//
//	Granotech Limited
//	Author: Uzi Granot
//	Version: 1.0
//	Date: January 16, 2014
//	Copyright (C) 2014 Granotech Limited. All Rights Reserved
//
//  TestInAppBilling is an open source software. You can
//	redistribute it and/or modify as part of your software under
//	the terms of the Eclipse Public License Version 1.0 (EPL-1.0)
//	as published by the Open Source Initiative organization.
//	TestInAppBilling is distributed in the hope that it will
//	be useful, but WITHOUT ANY WARRANTY; without even the implied
//	warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//	Go to Open Source Initiative web site for more details.
//	http://opensource.org/licenses/EPL-1.0
//
//	Version History:
//
//	Version 1.0 2014/01/16
//	Original revision
//
/////////////////////////////////////////////////////////////////////

// NOTE: You must replace this package name with your own package name
package com.omplusm.selfsurveyFREE;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

/////////////////////////////////////////////////////////////////////
// InAppBilling class
/////////////////////////////////////////////////////////////////////

public class InAppBilling
{
// InAppBillingService is the service that provides in-app billing version 3 and beyond.
// All api calls will give a response code with the following possible values
private static final int		RESULT_INVALID_RESPONSE = -1;		// invalid response code
private static final int		RESULT_OK = 0;						// success
private static final int		RESULT_USER_CANCELED = 1;			// user pressed back or canceled a dialog
private static final int		RESULT_UNKNOWN_RESULT_CODE = 2;		// unknown result code
private static final int		RESULT_BILLING_UNAVAILABLE = 3;		// this billing API version is not supported for the type requested
private static final int		RESULT_ITEM_UNAVAILABLE = 4;		// requested SKU is not available for purchase
private static final int		RESULT_DEVELOPER_ERROR = 5;			// invalid arguments provided to the API
private static final int		RESULT_ERROR = 6;					// fatal error during the API action
private static final int		RESULT_ITEM_ALREADY_OWNED = 7;		// failure to purchase since item is already owned
private static final int		RESULT_ITEM_NOT_OWNED = 8;			// failure to consume since item is not owned
private static final String		RESULT_TEXT_ITEM_ALREADY_OWNED = "ITEM_ALREADY_OWNED";
private static final String		RESULT_TEXT_ITEM_NOT_OWNED = "ITEM_NOT_OWNED";

// response code text
private static final String[]	resultText = new String[]
									{
									"Invalid response from Google Play",
									"Success",
									"User canceled the purchase",
									"Unknown result code",
									"This device does not support In App Billing service",
									"The requested item is not available for purchase",
									"Program error",
									"Communicating with Google Play failed",
									"You have already purchased this item",
									"Consume failed since item is not owned",
									};

// translate response code to text message
private static String resultMessage(int result)
	{
	if(result >= RESULT_INVALID_RESPONSE && result <= RESULT_ITEM_NOT_OWNED && result != RESULT_UNKNOWN_RESULT_CODE)
		return(resultText[result + 1]);
	return("Unknown response code from Google Play Store. Code: " + Integer.toString(result));
	}

// user error messages
private static final String		PLAY_STORE_UNAVAILABLE = "Google Play Store service unavailable on this device.";
private static final String		PLAY_STORE_SERVICE_DISCONNECTED = "Google Play Store service was disconnected.";
private static final String		PLAY_STORE_PURCHASE_NOT_SUPPORTED = "Google Play Store purchases not supported on this device.";
private static final String		PLAY_STORE_PURCHASE_FAILED = "Google Play Store purchase failed.";
private static final String		PLAY_STORE_INVALID_PURCHASE_RESPONSE = "Google Play Store invalid purchase response.";
private static final String		PLAY_STORE_INVALID_PRODUCT_ID = "Google Play Store invalid product ID.";
private static final String		PLAY_STORE_LOAD_OWNED_PRODUCT_FAILED = "Google Play Store loading owned products failed.";
private static final String		PLAY_STORE_CONSUME_PRODUCT_FAILED = "Consume product failed.";

// Keys for the responses from InAppBillingService
private static final String		RESPONSE_CODE = "RESPONSE_CODE";
private static final String		RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
private static final String		RESPONSE_BUY_INTENT = "BUY_INTENT";
private static final String		RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
private static final String		RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
private static final String		RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
private static final String		RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
private static final String		RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
private static final String		INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

// some fields on the getSkuDetails response bundle
private static final String		GET_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";

// service intent strings
private static final String		BIND_SERVICE_INTENT = "com.android.vending.billing.InAppBillingService.BIND";
private static final String		SERVICE_INTENT_PACKAGE = "com.android.vending";

private static final String		KEY_PRODUCT_ID = "productId";
private static final String		KEY_PURCHASE_TOKEN = "purchaseToken";
private static final String		PURCHASE_EXTRA_DATA = "ColorSelector";

// InAppBilling version 3
private static final int		IN_APP_BILLING_API_VERSION = 3;

// signature security
private static final String		KEY_FACTORY_ALGORITHM = "RSA";
private static final String		SIGNATURE_ALGORITHM = "SHA1withRSA";

// Translates encoded base64 characters to binary value 0 to 63 is valid value
private static final int		PD = 64;	// padding
private static final int		WS = 65;	// white space
private static final int		ER = 66;	// error
private final static byte[] DecodeArray =
		{
	//   0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F
		ER, ER, ER, ER, ER, ER, ER, ER, ER, WS, WS, ER, ER, WS, ER, ER, // 0x00 (0x09=\t, 0x0A=\n, 0x0D=\r)
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0x10
		WS, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, 62, ER, ER, ER, 63, // 0x20 (0x20=space, 0x2B=+, 0x2F=/)
		52, 53, 54, 55, 56, 57, 58, 59, 60, 61, ER, ER, ER, PD, ER, ER, // 0x30 (0x30-0x39 for 0-9, 0x3D for =)
		ER,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, // 0x40 (0x41-0x4F for A-O)
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, ER, ER, ER, ER, ER, // 0x50 (0x50-0x5A for P-Z)
		ER, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, // 0x60 (0x61-0x6F for a-o)
		41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, ER, ER, ER, ER, ER, // 0x70 (0x70-0x7A for p-z)
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0x80
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0x90
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0xA0
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0xB0
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0xC0
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0xD0
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0xE0
		ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, // 0xF0
		};

//itemType values
public  static final String		ITEM_TYPE_ONE_TIME_PURCHASE = "inapp";
public  static final String		ITEM_TYPE_SUBSCRIPTION = "subs";

// activity type values
public  static final boolean	ACTIVITY_TYPE_PURCHASE = false;
public  static final boolean	ACTIVITY_TYPE_CONSUME = true;

// item types and SKU and intent request code
private String					itemType;
private String					itemSku;
private boolean					consumeActivity;

// application context
private Activity				parentActivity;
private Context					applicationContext;
private InAppBillingListener	inAppBillingListener;
private String					packageName;
private int						purchaseRequestCode;

// application public key for verifying signature, in base64 encoding
private String					appPublicKeyStr;
private PublicKey				appPublicKey;

// Connection to the service
private IInAppBillingService inAppBillingService;
private ServiceConnection		serviceConnection;

// class is active
private boolean					active;
public  boolean					isActive() {return(active);}

// my own hack to decouple cheking for items from buying items
public boolean					checkOnly = true;

/////////////////////////////////////////////////////////////////////
// Callback that notifies when a purchase is done
/////////////////////////////////////////////////////////////////////

public interface InAppBillingListener
	{
	public void inAppBillingBuySuccsess();
	public void inAppBillingItemAlreadyOwned();
	public void inAppBillingCanceled();
	public void inAppBillingConsumeSuccsess();
	public void inAppBillingItemNotOwned();
	public void inAppBillingFailure(String errorMessage);
	}

/////////////////////////////////////////////////////////////////////
// constructor
// parentActivity: the activity that instantiate this class
// appPublicKeyStr: the public key assigned by Google Play Store to
//				this application in base64 format
// inAppBillingListener: a class implementing the four call back
//				methods to deliver results
// purchaseRequestCode: request code for onActivityResult
/////////////////////////////////////////////////////////////////////

public InAppBilling(Activity parentActivity,
					final InAppBillingListener inAppBillingListener, String appPublicKeyStr, int purchaseRequestCode)
	{
	// context initialization
	this.parentActivity = parentActivity;
    this.applicationContext = parentActivity.getApplicationContext();
	this.packageName = applicationContext.getPackageName();

	// listening class
    this.inAppBillingListener = inAppBillingListener;
	
    // google provided public key for this application
    this.appPublicKeyStr = appPublicKeyStr;
    
    // request code
    this.purchaseRequestCode = purchaseRequestCode;
    return;
	}

/////////////////////////////////////////////////////////////////////
// Dispose InAppBilling service
/////////////////////////////////////////////////////////////////////

public void dispose()
	{
	// un-bind service connection
    if(serviceConnection != null)
    	{
   		applicationContext.unbindService(serviceConnection);
    	serviceConnection = null;
    	}
    active = false;
    return;
	}

/////////////////////////////////////////////////////////////////////
//	Start in-app billing service connection
//	itemType: "inapp" or "subs"
//	itemSku: product ID
//	consumeActivity: Purchase = false, Consume = true
/////////////////////////////////////////////////////////////////////

public boolean startServiceConnection(String itemType, String itemSku, boolean consumeActivity)
	{

	showToast("started startServiceConnection, consumeActivity " + consumeActivity + "item " + itemSku);
	// already active
	if(active) return(false);
	
	// set active
	active = true;
	
	// save item type inapp or Subs
	this.itemType = itemType;
	
	// save requested item SKU
	this.itemSku = itemSku;

	// buy or consume the item
	this.consumeActivity = consumeActivity;
	
    // create bind service intent object
    Intent serviceIntent = new Intent(BIND_SERVICE_INTENT);
    serviceIntent.setPackage(SERVICE_INTENT_PACKAGE);

	// make sure there is at least one service matching the intent
    if(applicationContext.getPackageManager().resolveService(serviceIntent, 0) == null)
    	{
    	// no service available to handle that Intent
        // call purchase listener with error message
        active = false;
        inAppBillingListener.inAppBillingFailure(errorMessage(PLAY_STORE_UNAVAILABLE));
        return(true);
      	}

	// define connect and disconnect methods to handle call back from InAppBillingService
    serviceConnection = new ServiceConnection()
    	{
    	// service was disconnected call back
        @Override
        public void onServiceDisconnected(ComponentName name)
        	{
			// reset serviceConnection to make sure that dispose method will not un-bind the service
			serviceConnection = null;
        	active = false;

			// report service is disconnected to listener
        	inAppBillingListener.inAppBillingFailure(errorMessage(PLAY_STORE_SERVICE_DISCONNECTED));
            return;
        	}

        // service was connected call back
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        	{
        	// process service connected
        	serviceConnected(service);
        	return;
        	}
    	};

    try
	    {
	    // bind the service to our context and 
		// pass the service connection object defining the connect and disconnect call back methods
		if(!applicationContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE))
			{
			// reset serviceConnection to make sure that dispose method will not un-bind the service
			serviceConnection = null;
	        active = false;
	        inAppBillingListener.inAppBillingFailure(errorMessage(PLAY_STORE_UNAVAILABLE));
	        return(true);
		    }
	    }

    // binding to in app billing service failed
    catch(Exception e)
	    {
		// reset serviceConnection to make sure that dispose method will not un-bind the service
		serviceConnection = null;
    	active = false;
    	inAppBillingListener.inAppBillingFailure(exceptionMessage(PLAY_STORE_UNAVAILABLE, e));
    	return(true);
	    }

    // exit while waiting for service connection
    return(true);
	}

/////////////////////////////////////////////////////////////////////
// InAppBilling service is now connected to our application
/////////////////////////////////////////////////////////////////////

private void serviceConnected(IBinder service)
   	{
	// get service pointer
	inAppBillingService = IInAppBillingService.Stub.asInterface(service);

	// make sure billing is supported for itemType (one-time or subscription)
	try
		{
		// check for in-app billing v3 support
	    int result = inAppBillingService.isBillingSupported(IN_APP_BILLING_API_VERSION, packageName, itemType);
	    if(result != RESULT_OK)
			{
			// unbind service
	    	dispose();

	    	// call purchase listener with error message
			inAppBillingListener.inAppBillingFailure(errorMessage(PLAY_STORE_PURCHASE_NOT_SUPPORTED, result));
			return;
			}
		}

	// system exception executing isBillingSupported method
    catch(Exception e)
       	{
		// unbind service
    	dispose();

    	// call purchase listener with error message
		inAppBillingListener.inAppBillingFailure(exceptionMessage(PLAY_STORE_PURCHASE_NOT_SUPPORTED, e));
		return;
        }

		showToast("service connected ok");

	// test to make sure item sku is valid
	String resultMsg = testItemSku();
	if(resultMsg != null)
		{
		// unbind service
    	dispose();

    	// call purchase listener with error message
		inAppBillingListener.inAppBillingFailure(resultMsg);
		return;
		}

		showToast("sku is valid");
	
	// Get all items owned by this user for this application
	// and test if user already owns this item.
	// If we are in consume mode and item is owned, consume the item
	resultMsg = testItemOwned();
		showToast("test item owned done, result: " + resultMsg);

	if(!resultMsg.equals(RESULT_TEXT_ITEM_ALREADY_OWNED) && !resultMsg.equals(RESULT_TEXT_ITEM_NOT_OWNED))
	//if(!resultMsg.equals(RESULT_TEXT_ITEM_ALREADY_OWNED) || !resultMsg.equals(RESULT_TEXT_ITEM_NOT_OWNED))
		{
		// unbind service
    	dispose();

    	// call purchase listener with error message
		showToast("some error happend other then item already owned or not owned");
		inAppBillingListener.inAppBillingFailure(resultMsg);
		return;
		}

	// user owns this item
	if(resultMsg.equals(RESULT_TEXT_ITEM_ALREADY_OWNED))
		{
		// unbind service
    	dispose();

    	// for consume mode
    	if(consumeActivity)
		{showToast("item consumed");
			inAppBillingListener.inAppBillingConsumeSuccsess();}
    	
    	// for buy mode 
    	else
			{
				showToast("item alraedy owned");
				inAppBillingListener.inAppBillingItemAlreadyOwned();
			}
    	
    	// exit
		return;
		}

	// consume and user does not own this item
	if(consumeActivity)
		{
		// unbind service
    	dispose();

    	// call listener
		inAppBillingListener.inAppBillingItemNotOwned();
		return;
		}

		// my very own hack to decouple buying item from checking an item.
		// if checkonly flag is set we return from this function before implementing the real purchase
		if (checkOnly) {checkOnly=false;
			showToast("check only");
			dispose();
			showToast("disposed from the connection after the check");
			inAppBillingListener.inAppBillingItemNotOwned();
			return;}
	
	try
		{
		// build intent bundle for the purpose of purchase itemSku
        Bundle buyIntentBundle = inAppBillingService.getBuyIntent(IN_APP_BILLING_API_VERSION, packageName, itemSku, itemType, PURCHASE_EXTRA_DATA);

        // buyIntentBundle has two key value pairs
        // RESPONSE_CODE with standard response code
        // BUY_INTENT with PendingIntent to start the purchase flow
        int buyResult = getResponseCodeFromBundle(buyIntentBundle);
        if(buyResult != RESULT_OK)
        	{
			// unbind service
	    	dispose();

	    	// error message
	    	inAppBillingListener.inAppBillingFailure(errorMessage(PLAY_STORE_PURCHASE_FAILED + resultMessage(buyResult)));
    		return;
        	}

		// send Google Play Store request to purchase itemSku
        // when the purchase process is completed, the system will call onActivityResult method in parent activity
        PendingIntent pendingIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT);
        parentActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), purchaseRequestCode, new Intent(),
        		Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
		}

	// trap exceptions
	catch (Exception e)
		{
		// unbind service
    	dispose();

    	// error message
    	inAppBillingListener.inAppBillingFailure(exceptionMessage(PLAY_STORE_PURCHASE_FAILED, e));
		return;
		}

	// exit
	return;
   	}

/////////////////////////////////////////////////////////////////////
//	onActivityResult method is called when in-app billing purchase
//	process is done. The billing process calla onActivityResult
//	method in the parentActivity class. The parent activity calls
//	directly or indirectly this method.
/////////////////////////////////////////////////////////////////////

public void onActivityResult(int activityResultCode, Intent data)
	{
    // any result but OK we will assume user cancelled the activity
    if(activityResultCode != Activity.RESULT_OK)
	    {
		// unbind service
    	dispose();

    	// user canceled
		inAppBillingListener.inAppBillingCanceled();
		return;
	    }
    	
    int result = getResponseCodeFromBundle(data.getExtras());
    if(result != RESULT_OK)
    	{
		// unbind service
    	dispose();

    	// user canceled the purchase
    	if(result == RESULT_USER_CANCELED)
        	{
        	inAppBillingListener.inAppBillingCanceled();
        	}
    	
    	// purchase was canceled for other reason
    	else
    		{
    		inAppBillingListener.inAppBillingFailure(errorMessage(PLAY_STORE_PURCHASE_FAILED, result));
    		}
        return;
    	}

    // extract purchased data and signature
    String purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
    String signature = data.getStringExtra(RESPONSE_INAPP_SIGNATURE);

    // invalid response
    if(purchaseData == null || signature == null || !verifySignature(purchaseData, signature))
	    {
		// unbind service
    	dispose();

    	// invalid response
    	inAppBillingListener.inAppBillingFailure(errorMessage(PLAY_STORE_INVALID_PURCHASE_RESPONSE));
        return;
	    }

    // final test make sure we received the same sku we wanted
	try
		{
		// create json object
		JSONObject jsonObject = new JSONObject(purchaseData);
		
		// verify product id
		if(!jsonObject.optString(KEY_PRODUCT_ID).equals(itemSku))
    		{
			// unbind service
	    	dispose();

	    	// invalid response
	    	inAppBillingListener.inAppBillingFailure(errorMessage(PLAY_STORE_INVALID_PURCHASE_RESPONSE));
			return;
    		}

		// NOTE TO PROGRAMMERS
		// the jsonObject contains the following information
		// "orderId":"12999763169054705758.1371079406387615"
		// "packageName":"com.example.app",
		// "productId":"exampleSku",
		// "purchaseTime":1345678900000, (msec since 1970/01/01)
		// "purchaseState":0,  // 0-purchased, 1 canceled, 2 refunded
		// "developerPayload":"example developer payload"
		// "purchaseToken" : "122333444455555",

		}

	catch(Exception e)
		{
		// unbind service
    	dispose();

    	// invalid response
    	inAppBillingListener.inAppBillingFailure(exceptionMessage(PLAY_STORE_INVALID_PURCHASE_RESPONSE, e));
		return;
		}

	// unbind service
	dispose();

	// success
	inAppBillingListener.inAppBillingBuySuccsess();
    return;
	}

/////////////////////////////////////////////////////////////////////
// Test if item is available for sale
/////////////////////////////////////////////////////////////////////

private String testItemSku()
	{
	// create empty array of requested item SKU
    ArrayList<String> itemSkuArray = new ArrayList<String>();
    
    // add our one item to the list
    itemSkuArray.add(itemSku);
    
    // convert the string array to input bundle
    Bundle itemSkuBundle = new Bundle();
    itemSkuBundle.putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, itemSkuArray);
    
    // query response bundle
	Bundle itemDataBundle = null;
	try
		{
		// get info for the one item sku
		itemDataBundle = inAppBillingService.getSkuDetails(IN_APP_BILLING_API_VERSION, packageName, itemType, itemSkuBundle);
		}
	
	// catch exceptions
	catch(Exception e)
		{
		return(exceptionMessage(PLAY_STORE_INVALID_PRODUCT_ID, e));
		}

	// extract the response code from the response bundle
	int result = getResponseCodeFromBundle(itemDataBundle);
	if(result != RESULT_OK)
		{
		return(errorMessage(PLAY_STORE_INVALID_PRODUCT_ID, result));
		}

	// response must have the following key value 
	if(!itemDataBundle.containsKey(RESPONSE_GET_SKU_DETAILS_LIST)) return(errorMessage(PLAY_STORE_INVALID_PRODUCT_ID));

	// extract response array
	ArrayList<String> itemDataArray = itemDataBundle.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST);
	
	// this array must have one item
	if(itemDataArray.size() != 1) return(errorMessage(PLAY_STORE_INVALID_PRODUCT_ID));
	
    // get the returned product ID and compare it to our itemSku
	try
		{
		// get JSON object
		JSONObject jsonObject = new JSONObject(itemDataArray.get(0));
		
		// test product id
		if(!(jsonObject.optString(KEY_PRODUCT_ID)).equals(itemSku))
			return(errorMessage(PLAY_STORE_INVALID_PRODUCT_ID));
		
		// NOTE TO PROGRAMMERS
		// the jsonObject contains the following information
		// "productId" : "exampleSku"
		// "type" : "inapp"
		// "price" : "$5.00"
		// "title : "Example Title"
		// "description" : "This is an example description"
     
		}

	// JSON extraction failed
	catch(Exception e)
		{
		return(exceptionMessage(PLAY_STORE_INVALID_PRODUCT_ID, e));
		}
	
	// Google play has our item
	return(null);
	}

/////////////////////////////////////////////////////////////////////
//Test if item is owned by user
/////////////////////////////////////////////////////////////////////

private String testItemOwned()
	{
	// assume item is not owned
	boolean itemOwned = false;
	String jsonItemData = null;
	
	// Continuation token
	// This will be used if user owns many items and the system will
	// break it into multiple blocks.
	String continueToken = null;
	
	// loop in case of large numbers of owned items
	for(;;)
		{
		// define owned items bundle
		Bundle ownedItems = null;
	
		// load next block of owned items
		try
			{
			// get all items owned by the user
			ownedItems = inAppBillingService.getPurchases(IN_APP_BILLING_API_VERSION, packageName, itemType, continueToken);
			}
		
		// system error
		catch(Exception e)
	    	{
	    	return(exceptionMessage(PLAY_STORE_LOAD_OWNED_PRODUCT_FAILED, e));
	    	}

		// extract the response code from the bundle
		int result = getResponseCodeFromBundle(ownedItems);
		if(result != RESULT_OK)
			{
			return(errorMessage(PLAY_STORE_LOAD_OWNED_PRODUCT_FAILED, result));
			}
		
		// response must have the following three key value pairs 
		if(!ownedItems.containsKey(RESPONSE_INAPP_ITEM_LIST) ||
			!ownedItems.containsKey(RESPONSE_INAPP_PURCHASE_DATA_LIST) ||
			!ownedItems.containsKey(RESPONSE_INAPP_SIGNATURE_LIST)) return(errorMessage(PLAY_STORE_LOAD_OWNED_PRODUCT_FAILED));

		// get all items
		ArrayList<String> itemSkuArray = ownedItems.getStringArrayList(RESPONSE_INAPP_ITEM_LIST);
		ArrayList<String> itemDataArray = ownedItems.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
		ArrayList<String> signatureArray = ownedItems.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST);

		// make sure all three arrays have the same size
		int arraySize = itemSkuArray.size();
		if(itemDataArray.size() != arraySize || signatureArray.size() != arraySize)
			return(errorMessage(PLAY_STORE_LOAD_OWNED_PRODUCT_FAILED));

		// verify signatures of all items
		for(int Index = 0; Index < arraySize; Index++)
			{
			// item information
		    String ownedItemSku = itemSkuArray.get(Index);
		    String ownedItemData = itemDataArray.get(Index);
		    String signature = signatureArray.get(Index);

		    // verify signature
			// tmp removing this check
		    //if(!verifySignature(ownedItemData, signature)) return(errorMessage(PLAY_STORE_LOAD_OWNED_PRODUCT_FAILED));

		    // test for our item sku
		    if(ownedItemSku.equals(itemSku))
		    	{
		    	// our item is owned by user
		    	itemOwned = true;
		    	jsonItemData = ownedItemData;
		    	}

		    // NOTE TO PROGRAMMERS
			// the ownedItemData contains the following information
		    // it must be converted to JSON object
		    // JSONOBJECT jsonObject = new JSONOBJECT(ownedItemData);
			// "orderId":"12999763169054705758.1371079406387615"
			// "packageName":"com.example.app",
			// "productId":"exampleSku",
			// "purchaseTime":1345678900000, (msec since 1970/01/01)
			// "purchaseState":0,  // 0-purchased, 1 canceled, 2 refunded
			// "developerPayload":"example developer payload"
			// "purchaseToken" : "122333444455555",
			}

		// get continuation token
		continueToken = ownedItems.getString(INAPP_CONTINUATION_TOKEN);
		
		// if continuation token is blank or empty, break out of the loop
		if(TextUtils.isEmpty(continueToken)) break;
		}

	// item is not owned
	if(!itemOwned) return(RESULT_TEXT_ITEM_NOT_OWNED);

	// purchase item
	if(!consumeActivity) return(RESULT_TEXT_ITEM_ALREADY_OWNED);

	// consume item
	// get the purchase token
	try
		{
		// create json object for item data
    	JSONObject json = new JSONObject(jsonItemData);
    	
    	// get purchase token
        String itemPurchaseToken = json.optString(KEY_PURCHASE_TOKEN);
        
        // consume this item
		int result = inAppBillingService.consumePurchase(IN_APP_BILLING_API_VERSION, packageName, itemPurchaseToken);
		if(result != RESULT_OK)
			{
			return(errorMessage(PLAY_STORE_CONSUME_PRODUCT_FAILED, result));
			}
		}
	
	catch(Exception e)
		{
    	return(exceptionMessage(PLAY_STORE_CONSUME_PRODUCT_FAILED, e));
    	}

	// the item was consumed but return with already owned 
	return(RESULT_TEXT_ITEM_ALREADY_OWNED);
	}

/////////////////////////////////////////////////////////////////////
// get response code from a bundle
// for intent response, call it with intent.getExtras()
/////////////////////////////////////////////////////////////////////

private int getResponseCodeFromBundle(Bundle bundle)
	{
    Object responseCode = bundle.get(RESPONSE_CODE);
    if(responseCode == null) return(RESULT_OK);
    if(responseCode instanceof Integer) return(((Integer) responseCode).intValue());
    if(responseCode instanceof Long) return((int)((Long) responseCode).longValue());
    return(RESULT_INVALID_RESPONSE);
    }

/////////////////////////////////////////////////////////////////////
// Verifies that the data was signed with the given signature.
/////////////////////////////////////////////////////////////////////

private boolean verifySignature(String signedData, String signature)
	{
	try
	 	{
		// do it only once
		if(appPublicKey == null)
			{
			// decode application public key from base64 to binary	
			byte[] decodedKey = decodeBase64(appPublicKeyStr);
			if(decodedKey == null) return(false);

			// convert public key from binary to PublicKey object
			appPublicKey = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM).generatePublic(new X509EncodedKeySpec(decodedKey));
			}
		
	 	// decode signature
		byte[] decodedSig = decodeBase64(signature);
		if(decodedSig == null) return(false);

		// verify signature
		Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
		sig.initVerify(appPublicKey);
		sig.update(signedData.getBytes());
		return(sig.verify(decodedSig));
	 	}
	 catch (Exception e)
	 	{
	 	return(false);
	 	}
	}

/////////////////////////////////////////////////////////////////////
// Decodes Base64 string into decoded byte array
/////////////////////////////////////////////////////////////////////

private byte[] decodeBase64(String sourceStr)
	{
	// source string length
	int len = sourceStr.length();
	
	// output buffer with worst case length
	// every 4 input makes 3 output plus 2 extra for last partial buffer
	 byte[] outBuf = new byte[2 + len * 3 / 4];
	 
	 // loop initialization
	 int outBufPos = 0;
	 int bytesBuf = 0;
	 int bytesBufLen = 0;
	 int index;
	 for(index = 0; index < len; index++)
			{
	 	// decode next character
			int binChar = DecodeArray[(byte) sourceStr.charAt(index)];
			
			// valid character
			if(binChar < PD)
				{
				// add to bytes buffer
				bytesBuf = (bytesBuf << 6) | binChar;
				bytesBufLen++;
				
				// we have 4 valid input characters 
				if(bytesBufLen == 4)
					{
					// save three binary bytes
				    outBuf[outBufPos++] = (byte) (bytesBuf >>> 16);
				    outBuf[outBufPos++] = (byte) (bytesBuf >>> 8);
				    outBuf[outBufPos++] = (byte) bytesBuf;
				    
				    // reset bytes buffer
				    bytesBuf = 0;
				    bytesBufLen = 0;
					}
			    continue;
				}
	
			// padding character (must be at end of buffer)
			if(binChar == PD) break;
			
			// white space is ignored
			if(binChar == WS) continue;
			
			// error
			return(null);
			}
	
	 // we have a final partial buffer
	 if(index < len || bytesBufLen != 0)
	 	{
	 	// last partial buffer must be at least two characters
	 	if(bytesBufLen < 2) return(null);
	 	
	 	// make sure we have no more than two equal signs, white space is ok, all other error
	 	int equalCount = 0;
	 	for(; index < len; index++)
	 		{
	 		int binChar = DecodeArray[(byte) sourceStr.charAt(index)];
	 		if(binChar == PD)
	 			{
	 			equalCount++;
	 	    	if(equalCount > 2) return(null);
	 			}
	 		else if(binChar != WS) return(null);
	 		}
	 	
	 	// last partial buffer has 2 input bytes translated to 12 output bits
	 	if(bytesBufLen == 2)
	 		{
	 		// take the most significant 8 bits
			outBuf[outBufPos++] = (byte) (bytesBuf >>> 4);
	 		}
	
	 	// last buffer has 3 input bytes translated to 18 output bits
	 	else
	 		{
	 		// take the most significant 16 bits
			outBuf[outBufPos++] = (byte) (bytesBuf >>> 10);
			outBuf[outBufPos++] = (byte) (bytesBuf >>> 2);
	 		}
	 	}
	
	// create final output array
	byte[] out = new byte[outBufPos];
	System.arraycopy(outBuf, 0, out, 0, outBufPos);
	return(out);
	}

/////////////////////////////////////////////////////////////////////
// compose error message with error location
/////////////////////////////////////////////////////////////////////

private String errorMessage(String message)
	{
	return(message + "\n" + getErrorLocation(1));
	}

/////////////////////////////////////////////////////////////////////
// compose error message with error location
/////////////////////////////////////////////////////////////////////

private String errorMessage(String message, int result)
	{
	return(message + "\n" + resultMessage(result) + "\n" + getErrorLocation(1));
	}


/////////////////////////////////////////////////////////////////////
// compose exception error message with error location
/////////////////////////////////////////////////////////////////////

private String exceptionMessage(String message, Exception e)
	{
	return(message + "\n" + e.getMessage() + "\n" + getErrorLocation(1));
	}

/////////////////////////////////////////////////////////////////////
// error location based on stack frame
/////////////////////////////////////////////////////////////////////

private String getErrorLocation(int stackFrame)
	{
	StackTraceElement element = new Throwable().getStackTrace()[stackFrame + 1];
	String className = element.getClassName();
	if(className.startsWith(packageName + ".")) className = className.substring(packageName.length() + 1);
	return("Error location: " + element.getFileName() +
			" (" + Integer.toString(element.getLineNumber()) + ")\n" +
			className + "." + element.getMethodName());
	}


	public void showToast(CharSequence toaststring){


		//Toast toast = Toast.makeText(RunQuiz.this, toaststring, Toast.LENGTH_LONG);
		//toast.show();
		//Log.v("QUIZREC", " toast text: " + toaststring);

	}
}
