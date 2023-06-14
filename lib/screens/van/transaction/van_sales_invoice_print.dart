import 'dart:io';

import 'package:al_zaini_converting_industries/models/api_response.dart';
import 'package:al_zaini_converting_industries/models/van/van_sales_invoice_print.dart';
import 'package:al_zaini_converting_industries/services/van_services.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';


class VanSalesInvoicePrint extends StatefulWidget {
  final int VanSalesInvoiceID;

  const VanSalesInvoicePrint({Key key, @required this.VanSalesInvoiceID})
      : super(key: key);

  @override
  _VanSalesInvoicePrintState createState() => _VanSalesInvoicePrintState();
}

class _VanSalesInvoicePrintState extends State<VanSalesInvoicePrint> {
  bool _searching = true;
  VanSalesInvoicePrintModel vanSalesInvoicePrint =
      new VanSalesInvoicePrintModel();

  @override
  void initState() {
    print(widget.VanSalesInvoiceID.toString());
    getVanSalesInvoice();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
          'Print Invoice',
          style: TextStyle(color: Colors.white),
        ),
        centerTitle: true,
        actions: [
          PopupMenuButton<String>(
            onSelected: handleClick,
            itemBuilder: (BuildContext context) {
              return {
                'Search Available Devices',
                'Connect to Device',
                'Discount from Device'
              }.map((String choice) {
                return PopupMenuItem<String>(
                  value: choice,
                  child: Text(choice),
                );
              }).toList();
            },
          ),
        ],
      ),
      body: Container(
        padding: EdgeInsets.all(10),
        child: _searching
            ? Center(
                child: CircularProgressIndicator(),
              )
            : Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Center(
                    child: Image.asset(
                      'assets/slogo.jpg',
                      width: 98,
                      height: 98,
                      fit: BoxFit.contain,
                    ),
                  ),

                  personalDetailTile(
                      arblabel: 'هاتف',
                      englabel: 'Phone',
                      title: vanSalesInvoicePrint.Phone),
                  personalDetailTile(
                      englabel: 'Date',
                      title: vanSalesInvoicePrint.Date,
                      arblabel: 'تاريخ'),
                  personalDetailTile(
                      englabel: 'Invoice#',
                      title: vanSalesInvoicePrint.InvoiceNo,
                      arblabel: 'فاتورة'),
                  personalDetailTile(
                      englabel: 'Mobile',
                      title: vanSalesInvoicePrint.Mobile,
                      arblabel: 'جوال'),
                  // Text("Phone: "
                  //          + vanSalesInvoicePrint.Phone
                  //         + ' : هاتف'
                  //         ),
                  // Text("Date: " + vanSalesInvoicePrint.Date
                  //     // + " : تاريخ"
                  //     ),
                  // Text("Invoice# : " + vanSalesInvoicePrint.InvoiceNo
                  //     //+ " : #فاتورة"
                  //     ),
                  // Text("Mobile: " + vanSalesInvoicePrint.Mobile
                  //     //  + " : جوال"
                  //     ),
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: DataTable(
                      showBottomBorder: true,
                      columns: [
                        DataColumn(
                            label: Column(
                          children: [
                            Text('الصنف'),
                            Text('ITEM'),
                          ],
                        )),
                        DataColumn(
                            label: Column(
                          children: [
                            Text('الوحدة'),
                            Text('UNIT'),
                          ],
                        )),
                        DataColumn(
                            label: Column(
                          children: [
                            Text('الكمية'),
                            Text('QTY'),
                          ],
                        )),
                        DataColumn(
                            label: Column(
                          children: [
                            Text('السعر'),
                            Text('PRICE'),
                          ],
                        )),
                        DataColumn(
                            label: Column(
                          children: [
                            Text('المجموع'),
                            Text('AMOUNT'),
                          ],
                        )),
                      ],
                      rows: vanSalesInvoicePrint.VanSalesInvoiceDetailsList.map(
                          (model) => DataRow(cells: [
                                DataCell(Text(model.ItemName)),
                                DataCell(Text(model.UnitName)),
                                DataCell(Text(model.InvoiceQty.toString())),
                                DataCell(Text(model.UnitPrice.toString())),
                                DataCell(Text(model.ActualPrice.toString())),
                              ])).toList(),
                    ),
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      Text("Discount : " +
                          vanSalesInvoicePrint.Discount.toString() +
                          ':خصم'),
                    ],
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      Text("Total : " +
                          vanSalesInvoicePrint.TotalAmount.toString() +
                          ':مجموع'),
                    ],
                  ),
                  Text('شکرًالتسوفکم'),
                  Text("Thank you for shopping."),
                ],
              ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          if (vanSalesInvoicePrint.VanSalesInvoiceDetailsList.length > 0) {
            List<Map<String, String>> items = [];

            vanSalesInvoicePrint.VanSalesInvoiceDetailsList.forEach((element) {
              Map<String, String> elementStr = {
                "ItemName": element.ItemName + ' ' + element.ArabicName,
                "UnitName": element.UnitName + ' ' + element.ArabicUnit,
                "InvoiceQty": element.InvoiceQty.toString(),
                "UnitPrice": element.UnitPrice.toString(),
                "ActualPrice": element.ActualPrice.toString(),
              };

              items.add(elementStr);
            });

            var methodChannel = MethodChannel("com.paakhealth.sdk_base");

            await methodChannel.invokeMethod("receiptPrint", {
              // await methodChannel.invokeMethod("printDebug", {

              "phone": vanSalesInvoicePrint.Phone,
              "date": vanSalesInvoicePrint.Date,
              "bill": vanSalesInvoicePrint.InvoiceNo,
              "mobile": vanSalesInvoicePrint.Mobile,
              "discount": vanSalesInvoicePrint.Discount.toString(),
              "total": vanSalesInvoicePrint.TotalAmount.toString(),
              "list": items
            });
          } else {
            _showMsg('Receipt list is empty.');
          }
        },
        child: Icon(Icons.print),
      ),
    );
  }

  Widget personalDetailTile(
      {@required String englabel,
      @required String title,
      @required String arblabel}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          "$englabel : ", textAlign: TextAlign.left,
          //  + vanSalesInvoicePrint.Phone
          // + ' : هاتف'
        ),
        Text(title
            // + ' : هاتف'
            ),
        Text(
          ' : $arblabel',
          textAlign: TextAlign.right,
        ),
      ],
    );
  }

  void handleClick(String value) {
    switch (value) {
      case 'Search Available Devices':
        if (Platform.isAndroid) {
          var methodChannel = MethodChannel("com.paakhealth.sdk_base");
          methodChannel.invokeMethod("searchBle");
        }
        break;
      case 'Connect to Device':
        if (Platform.isAndroid) {
          var methodChannel = MethodChannel("com.paakhealth.sdk_base");
          methodChannel.invokeMethod("connectBle");
        }
        break;
      case 'Discount from Device':
        if (Platform.isAndroid) {
          var methodChannel = MethodChannel("com.paakhealth.sdk_base");
          methodChannel.invokeMethod("disconnectBle");
        }
        break;
    }
  }

  void getVanSalesInvoice() async {
    var vanServices = VanServices();
    VanSalesReturnAPIResponse response = await vanServices.VanSalesInvoiceView(
        VanSalesInvoiceID: widget.VanSalesInvoiceID);

    if (response != null) {
      if (response.data != null) {
        vanSalesInvoicePrint =
            VanSalesInvoicePrintModel.fromJson(response.data);
        _searching = false;
        setState(() {});
      } else {
        _showMsg('Oops! Server is Down');
      }
    } else {
      print('API response is null');
      _showMsg('Oops! Server is Down');
    }
  }

  void _showMsg(String message) {
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(message)));
  }
}


// class VanSalesInvoicePrint extends StatefulWidget {
//   final int VanSalesInvoiceID;

//   const VanSalesInvoicePrint({Key key, @required this.VanSalesInvoiceID})
//       : super(key: key);

//   @override
//   _VanSalesInvoicePrintState createState() => _VanSalesInvoicePrintState();
// }

// class _VanSalesInvoicePrintState extends State<VanSalesInvoicePrint> {
//   bool _searching = true;
//   VanSalesInvoicePrintModel vanSalesInvoicePrint =
//       new VanSalesInvoicePrintModel();

//   @override
//   void initState() {
//     print(widget.VanSalesInvoiceID.toString());
//     getVanSalesInvoice();
//     super.initState();
//   }

//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       appBar: AppBar(
//         title: Text(
//           'Print Invoice',
//           style: TextStyle(color: Colors.white),
//         ),
//         centerTitle: true,
//         actions: [
//           PopupMenuButton<String>(
//             onSelected: handleClick,
//             itemBuilder: (BuildContext context) {
//               return {
//                 'Search Available Devices',
//                 'Connect to Device',
//                 'Discount from Device'
//               }.map((String choice) {
//                 return PopupMenuItem<String>(
//                   value: choice,
//                   child: Text(choice),
//                 );
//               }).toList();
//             },
//           ),
//         ],
//       ),
//       body: Container(
//         padding: EdgeInsets.all(10),
//         child: _searching
//             ? Center(
//                 child: CircularProgressIndicator(),
//               )
//             : Column(
//                 crossAxisAlignment: CrossAxisAlignment.start,
//                 children: [
//                   Center(
//                     child: Image.asset(
//                       'assets/slogo.jpg',
//                       width: 98,
//                       height: 98,
//                       fit: BoxFit.contain,
//                     ),
//                   ),
//                   Text("Phone: " + vanSalesInvoicePrint.Phone),
//                   Text("Date: " + vanSalesInvoicePrint.Date),
//                   Text("Invoice# : " + vanSalesInvoicePrint.InvoiceNo),
//                   Text("Mobile: " + vanSalesInvoicePrint.Mobile),
//                   SingleChildScrollView(
//                     scrollDirection: Axis.horizontal,
//                     child: DataTable(
//                       showBottomBorder: true,
//                       columns: [
//                         DataColumn(label: Text('ITEM')),
//                         DataColumn(label: Text('UNIT')),
//                         DataColumn(label: Text('QTY')),
//                         DataColumn(label: Text('PRICE')),
//                         DataColumn(label: Text('AMOUNT')),
//                       ],
//                       rows: vanSalesInvoicePrint.VanSalesInvoiceDetailsList.map(
//                           (model) => DataRow(cells: [
//                                 DataCell(Text(model.ItemName)),
//                                 DataCell(Text(model.UnitName)),
//                                 DataCell(Text(model.InvoiceQty.toString())),
//                                 DataCell(Text(model.UnitPrice.toString())),
//                                 DataCell(Text(model.ActualPrice.toString())),
//                               ])).toList(),
//                     ),
//                   ),
//                   Row(
//                     mainAxisAlignment: MainAxisAlignment.end,
//                     children: [
//                       Text("Discount : " +
//                           vanSalesInvoicePrint.Discount.toString()),
//                     ],
//                   ),
//                   Row(
//                     mainAxisAlignment: MainAxisAlignment.end,
//                     children: [
//                       Text("Total : " +
//                           vanSalesInvoicePrint.TotalAmount.toString()),
//                     ],
//                   ),
//                   Text("Thank you for shopping."),
//                 ],
//               ),
//       ),
//       floatingActionButton: FloatingActionButton(
//         onPressed: () async {
//           if (vanSalesInvoicePrint.VanSalesInvoiceDetailsList.length > 0) {
//             List<Map<String, String>> items = [];

//             vanSalesInvoicePrint.VanSalesInvoiceDetailsList.forEach((element) {
//               Map<String, String> elementStr = {
//                 "ItemName": element.ItemName + ' ' + element.ArabicName,
//                 "UnitName": element.UnitName + ' ' + element.ArabicUnit,
//                 "InvoiceQty": element.InvoiceQty.toString(),
//                 "UnitPrice": element.UnitPrice.toString(),
//                 "ActualPrice": element.ActualPrice.toString(),
//               };

//               items.add(elementStr);
//             });

//             var methodChannel = MethodChannel("com.paakhealth.sdk_base");

//             await methodChannel.invokeMethod("receiptPrint", {
//               // await methodChannel.invokeMethod("printDebug", {

//               "phone": vanSalesInvoicePrint.Phone,
//               "date": vanSalesInvoicePrint.Date,
//               "bill": vanSalesInvoicePrint.InvoiceNo,
//               "mobile": vanSalesInvoicePrint.Mobile,
//               "discount": vanSalesInvoicePrint.Discount.toString(),
//               "total": vanSalesInvoicePrint.TotalAmount.toString(),
//               "list": items
//             });
//           } else {
//             _showMsg('Receipt list is empty.');
//           }
//         },
//         child: Icon(Icons.print),
//       ),
//     );
//   }

//   void handleClick(String value) {
//     switch (value) {
//       case 'Search Available Devices':
//         if (Platform.isAndroid) {
//           var methodChannel = MethodChannel("com.paakhealth.sdk_base");
//           methodChannel.invokeMethod("searchBle");
//         }
//         break;
//       case 'Connect to Device':
//         if (Platform.isAndroid) {
//           var methodChannel = MethodChannel("com.paakhealth.sdk_base");
//           methodChannel.invokeMethod("connectBle");
//         }
//         break;
//       case 'Discount from Device':
//         if (Platform.isAndroid) {
//           var methodChannel = MethodChannel("com.paakhealth.sdk_base");
//           methodChannel.invokeMethod("disconnectBle");
//         }
//         break;
//     }
//   }

//   void getVanSalesInvoice() async {
//     var vanServices = VanServices();
//     VanSalesReturnAPIResponse response = await vanServices.VanSalesInvoiceView(
//         VanSalesInvoiceID: widget.VanSalesInvoiceID);

//     if (response != null) {
//       if (response.data != null) {
//         vanSalesInvoicePrint =
//             VanSalesInvoicePrintModel.fromJson(response.data);
//         _searching = false;
//         setState(() {});
//       } else {
//         _showMsg('Oops! Server is Down');
//       }
//     } else {
//       print('API response is null');
//       _showMsg('Oops! Server is Down');
//     }
//   }

//   void _showMsg(String message) {
//     ScaffoldMessenger.of(context)
//         .showSnackBar(SnackBar(content: Text(message)));
//   }
// }
