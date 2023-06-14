package com.paakhealth.al_zaini_converting_industries;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.paakhealth.al_zaini_converting_industries.utils.Conts;
import com.paakhealth.al_zaini_converting_industries.utils.DeviceReceiver;
import com.paakhealth.al_zaini_converting_industries.utils.StringUtils;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos76;
import net.posprinter.utils.DataForSendToPrinterPos80;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

    //IMyBinder interface，All methods that can be invoked to connect and send data are encapsulated within this interface
    public IMyBinder binder;
    BluetoothAdapter bluetoothAdapter;
    private View dialogView;
    private ArrayAdapter<String> adapter1, adapter2, adapter3;

    private ListView lv1, lv2;
    private ArrayList<String> deviceList_bonded = new ArrayList<String>();//bonded list
    private ArrayList<String> deviceList_found = new ArrayList<String>();//found list
    public static boolean ISCONNECT;
    private Button btn_scan; //scan button
    private LinearLayout LLlayout;
    AlertDialog dialog;
    String mac = "";
    int pos;

    boolean isProcessing = false;

    // todo main
//    String device_mac = "";
    String printText = "AL ZAINI CONVERTING INDUSTRIES";
    String printTextArabic = "الزيني للصناعات التحويلية";

    //bindService connection
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //Bind successfully
            binder = (IMyBinder) iBinder;
            Log.e("binder", "connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("disbinder", "disconnected");
        }
    };

    private static final String CHANNEL = "com.paakhealth.sdk_base";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //bind service，get ImyBinder object
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);



    }


    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            switch (call.method) {
                                case "searchBle": {
//                                    setBinder();
                                    Log.d("isprocessing", String.valueOf(isProcessing));
                                    if (!isProcessing){
                                        isProcessing = true;
                                        setBluetooth();
                                        result.success("Selected Mac: " + mac);
                                    }
                                }
                                case "connectBle": {
                                    Log.d("isprocessing", String.valueOf(isProcessing));

                                    if (!isProcessing) {
                                        isProcessing = true;
                                        setBinder();
                                        connetBle();
                                        result.success("Bluetooth Connected: " + mac);
                                    }
                                }
                                case "disconnectBle": {
                                    Log.d("isprocessing", String.valueOf(isProcessing));

                                    if (!isProcessing) {
                                        isProcessing = true;
                                        setBinder();
                                        disconnectBle();
                                        result.success("Bluetooth Disconnected: " + mac);
                                    }
                                }
                                case "testPrint": {
                                    Log.d("isprocessing", String.valueOf(isProcessing));

                                    if (!isProcessing) {
                                        isProcessing = true;
                                        setBinder();
                                        printTextt();
                                        result.success("Text Printed: " + mac);
                                    }
                                }
                                case "receiptPrint": {
                                    Log.d("isprocessing", String.valueOf(isProcessing));

                                    if (!isProcessing) {
                                        isProcessing = true;
                                        setBinder();

                                        String phone = call.argument("phone");
                                        String date = call.argument("date");
                                        String bill = call.argument("bill");
                                        String mobile = call.argument("mobile");
                                        String discount = call.argument("discount");
                                        String total = call.argument("total");
                                        ArrayList<HashMap<String, String>> list = call.argument("list");
                                        //(String ph, String date, String bill, String mobile, String discount, String total,  ArrayList<HashMap<String, String>> listt)
                                        printReciept(phone, date, bill, mobile, discount, total, list);
                                        result.success("Receipt Printed");
                                    }
                                }case "printDebug": {
                                    Log.d("isprocessing", String.valueOf(isProcessing));

                                    if (!isProcessing) {
                                        isProcessing = true;
                                        setBinder();

                                        String phone = call.argument("phone");
                                        String date = call.argument("date");
                                        String bill = call.argument("bill");
                                        String mobile = call.argument("mobile");
                                        String discount = call.argument("discount");
                                        String total = call.argument("total");
                                        ArrayList<HashMap<String, String>> list = call.argument("list");
                                        // (String ph, String date, String bill, String mobile)
                                        printDebug(phone, date, bill, mobile, discount, total, list);
                                        result.success("Receipt Printed");
                                    }
                                }
                                default:
                                    result.notImplemented();
                            }
                        }
                );
    }

    private void setBinder() {
        if (binder == null) {
            //bind service，get ImyBinder object
            Log.e("binder", "binder is null");
            Intent intent = new Intent(this, PosprinterService.class);
            bindService(intent, conn, BIND_AUTO_CREATE);
            if (binder == null) {
                Log.e("binder", "binder is null2");
            }
        }
    }


    //    bluetooth connecttion
    private void connetBle() {
        // todo get the bluetooth mac address
        // String bleAdrress=showET.getText().toString();
        String bleAdrress = mac;
        if (mac.equals(null) || mac.equals("")) {
            showSnackbar(getString(R.string.bleselect));
        } else {
            if (binder == null){
                showSnackbar("Binder is null");
            }
            binder.connectBtPort(bleAdrress, new UiExecute() {
                @Override
                public void onsucess() {
                    ISCONNECT = true;
                    showSnackbar(getString(R.string.con_success));
                    // todo handle button text
                    // BTCon.setText(getString(R.string.con_success));
                    showSnackbar("connect success");


                    binder.write(DataForSendToPrinterPos80.openOrCloseAutoReturnPrintState(0x1f), new UiExecute() {
                        @Override
                        public void onsucess() {
                            binder.acceptdatafromprinter(new UiExecute() {
                                @Override
                                public void onsucess() {

                                }

                                @Override
                                public void onfailed() {
                                    ISCONNECT = false;
                                    showSnackbar(getString(R.string.con_has_discon));
                                }
                            });
                        }

                        @Override
                        public void onfailed() {

                        }
                    });


                }

                @Override
                public void onfailed() {

                    ISCONNECT = false;
                    showSnackbar(getString(R.string.con_failed));
                }
            });
        }

        isProcessing = false;

    }

    // discount bluetooth
    private void disconnectBle() {
        if (ISCONNECT) {
            binder.disconnectCurrentPort(new UiExecute() {
                @Override
                public void onsucess() {
                    showSnackbar(getString(R.string.toast_discon_success));
                    // showET.setText("");
//                    mac = "";
                    // todo btn text
                    // BTCon.setText(getString(R.string.connect));
                }

                @Override
                public void onfailed() {
                    showSnackbar(getString(R.string.toast_discon_faile));

                }
            });
        } else {
            showSnackbar(getString(R.string.toast_present_con));
        }

        isProcessing = false;
    }

    // select bluetooth
    // todo application process must start form thic function
    public void setBluetooth() {
        showSnackbar("setting bluetooth");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            //open bluetooth
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, Conts.ENABLE_BLUETOOTH);
        } else {

            showblueboothlist();

        }
    }

    private void showblueboothlist() {
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        dialogView = inflater.inflate(R.layout.printer_list, null);
        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList_bonded);
        lv1 = (ListView) dialogView.findViewById(R.id.listView1);
        btn_scan = (Button) dialogView.findViewById(R.id.btn_scan);
        LLlayout = (LinearLayout) dialogView.findViewById(R.id.ll1);
        lv2 = (ListView) dialogView.findViewById(R.id.listView2);
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList_found);
        lv1.setAdapter(adapter1);
        lv2.setAdapter(adapter2);
        dialog = new AlertDialog.Builder(this).setTitle("BLE").setView(dialogView).create();
        dialog.show();

        DeviceReceiver myDevice = new DeviceReceiver(deviceList_found, adapter2, lv2);

        //register the receiver
        IntentFilter filterStart = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(myDevice, filterStart);
        registerReceiver(myDevice, filterEnd);

        setDlistener();
        findAvalibleDevice();
    }

    private void setDlistener() {
        // TODO Auto-generated method stub
        btn_scan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LLlayout.setVisibility(View.VISIBLE);
                //btn_scan.setVisibility(View.GONE);
            }
        });
        //boned device connect
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                try {
                    if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();

                    }

                    String msg = deviceList_bonded.get(arg2);
                    mac = msg.substring(msg.length() - 17);
                    String name = msg.substring(0, msg.length() - 18);
                    //lv1.setSelection(arg2);
                    dialog.cancel();
                    // todo handle mac address
                    // showET.setText(mac);
//                    device_mac = mac;
                    //Log.i("TAG", "mac="+mac);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        //found device and connect device
        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                try {
                    if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();

                    }
                    String msg = deviceList_found.get(arg2);
                    mac = msg.substring(msg.length() - 17);
                    String name = msg.substring(0, msg.length() - 18);
                    //lv2.setSelection(arg2);
                    dialog.cancel();
                    // todo settext
                    // showET.setText(mac);
//                    device_mac = mac;
                    Log.i("TAG", "mac=" + mac);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    find avaliable device
     */
    private void findAvalibleDevice() {
        // TODO Auto-generated method stub

        Set<BluetoothDevice> device = bluetoothAdapter.getBondedDevices();

        deviceList_bonded.clear();
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            adapter1.notifyDataSetChanged();
        }
        if (device.size() > 0) {
            //already
            for (Iterator<BluetoothDevice> it = device.iterator(); it.hasNext(); ) {
                BluetoothDevice btd = it.next();
                deviceList_bonded.add(btd.getName() + '\n' + btd.getAddress());
                adapter1.notifyDataSetChanged();
            }
        } else {
            deviceList_bonded.add("No can be matched to use bluetooth");
            adapter1.notifyDataSetChanged();
        }

        isProcessing = false;
    }

    private void showSnackbar(String showstring) {
//        Snackbar.make(findViewById(android.R.id.content), showstring, Snackbar.LENGTH_LONG)
//                .show();
        Toast.makeText(this, showstring, Toast.LENGTH_SHORT).show();
    }

    private void printTextt() {
        if (ISCONNECT) {
            binder.writeDataByYouself(
                    new UiExecute() {
                        @Override
                        public void onsucess() {

                        }

                        @Override
                        public void onfailed() {

                        }
                    }, new ProcessData() {
                        @Override
                        public List<byte[]> processDataBeforeSend() {

                            List<byte[]> list = new ArrayList<byte[]>();
                            //creat a text ,and make it to byte[],
                            // todo text
                            // String str=text.getText().toString();
                            String str = printText;
                            if (str.equals(null) || str.equals("")) {
                                showSnackbar(getString(R.string.text_for));
                            } else {
                                //initialize the printer
//                            list.add( DataForSendToPrinterPos58.initializePrinter());
                                list.add(DataForSendToPrinterPos80.initializePrinter());
                                byte[] azci = StringUtils.strTobytes(str);
                                list.add(azci);
                                //should add the command of print and feed line,because print only when one line is complete, not one line, no print
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                //cut pager
                                list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1));
                                return list;
                            }
                            return null;
                        }
                    });
        } else {
            showSnackbar(getString(R.string.toast_present_con));
        }

        isProcessing = false;
    }

    // todo print sales reciept
    private void printReciept(String ph, String date, String bill, String mobile, String discount, String total,  ArrayList<HashMap<String, String>> listt) {
        if (ISCONNECT) {
            binder.writeDataByYouself(
                    new UiExecute() {
                        @Override
                        public void onsucess() {

                        }

                        @Override
                        public void onfailed() {

                        }
                    }, new ProcessData() {
                        @Override
                        public List<byte[]> processDataBeforeSend() {

                            List<byte[]> list = new ArrayList<byte[]>();
                            //creat a text ,and make it to byte[],
                            // todo text
                            // String str=text.getText().toString();
                            String azci = printText;

                            if (azci.equals(null) || azci.equals("")) {
                                showSnackbar(getString(R.string.text_for));
                            } else {
                                //initialize the printer
//                            list.add( DataForSendToPrinterPos58.initializePrinter());
                                list.add(DataForSendToPrinterPos80.initializePrinter());


                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                
                                byte[] data1 = StringUtils.strTobytes(azci);
                                list.add(data1);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data2 = printTextArabic.getBytes(StandardCharsets.ISO_8859_1);
                                list.add(data2);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data3 = printTextArabic.getBytes(StandardCharsets.US_ASCII);
                                list.add(data3);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data4 = printTextArabic.getBytes(StandardCharsets.UTF_16);
                                list.add(data4);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data5 = printTextArabic.getBytes(StandardCharsets.UTF_16BE);
                                list.add(data5);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data6 = printTextArabic.getBytes(StandardCharsets.UTF_16LE);
                                list.add(data6);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data7 = StringUtils.strTobytes(printTextArabic);
                                list.add(data7);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());


                                byte[] data1ph = StringUtils.strTobytes("Phone: " + ph + " :" + "هاتف"   , "UTF-16LE");
                                list.add(data1ph);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data1date = StringUtils.strTobytes("Date: " + date + " :" + "تاريخ"  , "UTF-8");
                                list.add(data1date);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data1bill = StringUtils.strTobytes("Invoice #: " + bill + " :" + "فاتورة", "UTF-8");
                                list.add(data1bill);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data1mobile = StringUtils.strTobytes("Mobile: " + mobile + " :" + "جوال" , "UTF-8") ;
                                list.add(data1mobile);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                String arabicHeading = "م" + "  " + "ألصنف" + "           " + "الواحدة" + "   " + "الكمية" + "    " + "ألمجموع";
                                byte[] data1heading = arabicHeading.getBytes();
                                list.add(data1heading);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data2heading = StringUtils.strTobytes("#  ITEM           UNIT   QTY    PRICE   AMOUNT");
                                list.add(data2heading);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data1ine = StringUtils.strTobytes("----------------------------------------------");
                                list.add(data1ine);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                for(HashMap<String, String> map : listt){
                                    String itemName = map.get("ItemName");
                                    String invoiceQty = map.get("InvoiceQty");
                                    String unitPrice = map.get("UnitPrice");

                                    StringBuilder line = new StringBuilder("");

                                    line.append(listt.indexOf(map)+1).append("  ");

                                    if (itemName.length() < 16){
                                        line.append(map.get("ItemName"));

                                        int y = 15 - itemName.length();

                                        for (int i=0; i < y; i++){
                                            line.append(" ");
                                        }

                                    }
                                    if (itemName.length() > 15){


                                        String s = itemName.substring(0, 11);
                                        line.append(s).append("... ");

                                    }

                                    line.append(map.get("UnitName")).append("   ");

                                    if (invoiceQty.length() < 8){
                                        line.append(map.get("InvoiceQty"));

                                        int y = 7 - invoiceQty.length();

                                        for (int i=0; i < y; i++){
                                            line.append(" ");
                                        }

                                    }

                                    if (unitPrice.length() < 9){
                                        line.append(map.get("UnitPrice"));

                                        int y = 8 - unitPrice.length();

                                        for (int i=0; i < y; i++){
                                            line.append(" ");
                                        }

                                    }

                                    line.append(map.get("ActualPrice"));
                                    list.add(StringUtils.strTobytes(line.toString(), "UTF-8"));
                                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                }


                                byte[] data1dis = StringUtils.strTobytes("                           Discount-خصم: "+ discount, "UTF-8");
                                list.add(data1dis);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                byte[] data1total = StringUtils.strTobytes("                         Total-مجموع:   "+ total, "UTF-8");
                                list.add(data1total);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                // second way to get bytes
                                String str = "شكرا لتسوقكم";
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                try {
                                    outputStream.write(str.getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                byte[] data01thank = outputStream.toByteArray();

                                list.add(data01thank);

                                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                                // third way to get bytes
                                String arabicThanksText = "شكرا لتسوقكم";
                                Charset charset = StandardCharsets.UTF_8;
                                CharsetEncoder encoder = charset.newEncoder();
                                ByteBuffer buffer = null;
                                try {
                                    buffer = encoder.encode(CharBuffer.wrap(arabicThanksText));
                                } catch (CharacterCodingException e) {
                                    e.printStackTrace();
                                }
                                byte[] data1thank = buffer.array();
                                list.add(data1thank);

                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                
                                byte[] data2thank = StringUtils.strTobytes("Thank you for shopping.");
                                list.add(data2thank);


                                
                                byte[] test = StringUtils.strTobytes( "فاتورة", "UTF-8");
                                list.add(test);
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());


                                //should add the command of print and feed line,because print only when one line is complete, not one line, no print
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                                //cut pager
                                list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1));
                                return list;
                            }
                            return null;
                        }
                    });
        } else {
            showSnackbar(getString(R.string.toast_present_con));
        }


        isProcessing = false;
    }

    // todo debug print test data received through platform channel
    private void printDebug(String ph, String date, String bill, String mobile, String discount, String total, ArrayList<HashMap<String, String>> list){

        Log.d("debugPrint", "---------------------------------------------");
        Log.d("debugPrint", "---------------------------------------------");
        Log.d("debugPrint", "Phone : " + ph);
        Log.d("debugPrint", "Date : " + date);
        Log.d("debugPrint", "Bill # : " + bill);
        Log.d("debugPrint", "Mobile : " + mobile);
        Log.d("debugPrint", "#  ITEM           UNIT   QTY    PRICE   AMOUNT");
        for(HashMap<String, String> map : list){
            String itemName = map.get("ItemName");
            String invoiceQty = map.get("InvoiceQty");
            String unitPrice = map.get("UnitPrice");

            StringBuilder line = new StringBuilder("");
            line.append(list.indexOf(map)+1).append("   ");

            if (itemName.length() < 16){
                line.append(map.get("ItemName"));

                int y = 15 - itemName.length();

                for (int i=0; i < y; i++){
                    line.append(" ");
                }

            }
            if (itemName.length() > 15){


                String s = itemName.substring(0, 11);
                line.append(s).append("... ");

            }

            line.append(map.get("UnitName")).append("   ");

            if (invoiceQty.length() < 8){
                line.append(map.get("InvoiceQty"));

                int y = 7 - invoiceQty.length();

                for (int i=0; i < y; i++){
                    line.append(" ");
                }

            }
            if (unitPrice.length() < 9){
                line.append(map.get("UnitPrice"));

                int y = 8 - unitPrice.length();

                for (int i=0; i < y; i++){
                    line.append(" ");
                }

            }

            line.append(map.get("ActualPrice"));
            Log.d("debugPrint", line.toString());
        }

        String disLine = "                                Discount "+ discount;
        Log.d("debugPrint", disLine);
        String totalLine = "                                Total    "+ total;
        Log.d("debugPrint", totalLine);
        Log.d("debugPrint", "---------------------------------------------");
        Log.d("debugPrint", "---------------------------------------------");

        isProcessing = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binder.disconnectCurrentPort(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {

            }
        });
        unbindService(conn);
    }
}
